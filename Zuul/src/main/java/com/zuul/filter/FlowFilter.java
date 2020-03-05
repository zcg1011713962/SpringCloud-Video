package com.zuul.filter;

import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletResponse;

import org.bouncycastle.asn1.ocsp.ResponseData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import com.alibaba.fastjson.JSON;
import com.google.common.util.concurrent.RateLimiter;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;

import io.micrometer.core.instrument.util.JsonUtils;

public class FlowFilter extends ZuulFilter {
	@Autowired
	private RedisTemplate<String, Object> redistemplate;
	public static volatile RateLimiter rateLimiter = RateLimiter.create(1000.0);

	@Override
	public boolean shouldFilter() {
		//上个过滤器决定是否执行该过滤器
		RequestContext ctx = RequestContext.getCurrentContext();
		Object success = ctx.get("isSuccess");
		return success == null ? true : Boolean.parseBoolean(success.toString());
	}

	@Override
	public Object run() throws ZuulException {
		try {
			//redis集群限流
			RequestContext ctx = RequestContext.getCurrentContext();
			String url = ctx.getRequest().getRequestURL().toString();
			if(url.contains("/css/")||url.contains("/js/")||url.contains(".html")) {
				return null;
			}
			if (!redistemplate.hasKey(url)) {
				System.out.println(url);
				// 设置一秒钟超时
				redistemplate.opsForValue().set(url, 0L, 1, TimeUnit.SECONDS);
			}
			Long num=0L;
			if (redistemplate.hasKey(url)) {
				num = redistemplate.opsForValue().increment(url,1);
				System.out.println(num);
			}
			if (num> 5000) {
				//防止并发
				redistemplate.opsForValue().set(url, 0L, 1, TimeUnit.SECONDS);
				System.out.println(url+"每秒请求大于5000");
				ctx.setSendZuulResponse(false);
	            ctx.setResponseBody("{error:当前负载太高，请稍后重试}");
	            ctx.getResponse().setContentType("application/json;charset=utf-8");
			}
		}catch (Exception e) {
			System.out.println("redis集群限流异常，开启单节点限流"+e);
			//令牌桶单节点限流
			rateLimiter.acquire();
		}
		return null;
	}

	@Override
	public String filterType() {
		return "pre";
	}

	@Override
	public int filterOrder() {
		return 1;
	}

}
