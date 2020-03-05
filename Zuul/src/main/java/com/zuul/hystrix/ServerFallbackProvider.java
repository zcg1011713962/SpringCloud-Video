package com.zuul.hystrix;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.cloud.netflix.zuul.filters.route.FallbackProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import com.netflix.hystrix.exception.HystrixTimeoutException;
@Component
public class ServerFallbackProvider implements FallbackProvider {

	@Override
	public String getRoute() {
        //微服务配了路由的话，就用配置的名称
		//如果要为所有路由提供默认回退
        return "*";
	}

	@Override
	public ClientHttpResponse fallbackResponse(String route, Throwable cause) {
		 if (cause instanceof HystrixTimeoutException) {
	            return response(HttpStatus.GATEWAY_TIMEOUT);
	        } else {
	            return response(HttpStatus.INTERNAL_SERVER_ERROR);
	        }
	}
	 private ClientHttpResponse response(final HttpStatus status) {
	        return new ClientHttpResponse() {

				@Override
				public InputStream getBody() throws IOException {
					//返回页面提示信息
					return new ByteArrayInputStream("Serverfallback".getBytes());
				}

				@Override
				public HttpHeaders getHeaders() {
					HttpHeaders headers = new HttpHeaders();
	                headers.setContentType(MediaType.APPLICATION_JSON);
	                return headers;
				}

				@Override
				public org.springframework.http.HttpStatus getStatusCode() throws IOException {
	                return HttpStatus.OK;
				}

				@Override
				public int getRawStatusCode() throws IOException {
					return 200;
				}

				@Override
				public String getStatusText() throws IOException {
					 return "OK";
				}

				@Override
				public void close() {
				}
	        };
	    }

}
