package com.login;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.cloud.netflix.hystrix.dashboard.EnableHystrixDashboard;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;

import com.login.netty.NettyClient;
import com.netflix.hystrix.contrib.metrics.eventstream.HystrixMetricsStreamServlet;
import com.spring4all.swagger.EnableSwagger2Doc;

import io.netty.channel.ChannelFuture;
@EnableSwagger2Doc
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.login.feign")
@EnableHystrix 
@EnableHystrixDashboard
@MapperScan(basePackages = "com.login.mapper")
public class LoginServiceApplication implements CommandLineRunner{
	@Autowired
	private NettyClient nettyClient;
	public static void main(String[] args) {
		SpringApplication.run(LoginServiceApplication.class, args);
	}
	@Bean
    public ServletRegistrationBean<HystrixMetricsStreamServlet> getServlet() {
        HystrixMetricsStreamServlet streamServlet = new HystrixMetricsStreamServlet();
        ServletRegistrationBean<HystrixMetricsStreamServlet> registrationBean = new ServletRegistrationBean<HystrixMetricsStreamServlet>(streamServlet);
        registrationBean.setLoadOnStartup(1);
        registrationBean.addUrlMappings("/actuator/hystrix.stream");
        registrationBean.setName("HystrixMetricsStreamServlet");
        return registrationBean;
    }
	//spring管理nettyClient
	@Override
	public void run(String... args) throws Exception {
		/*ChannelFuture future = nettyClient.start("127.0.0.1",9999);
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run() {
            	nettyClient.destroy();
            }
        });
        //服务端管道关闭的监听器并同步阻塞,直到channel关闭,线程才会往下执行,结束进程
        future.channel().closeFuture().syncUninterruptibly();*/
	}
}