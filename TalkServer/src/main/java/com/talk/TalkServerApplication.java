package com.talk;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import com.talk.websocket.WebsocketServer;

import io.netty.channel.ChannelFuture;

@SpringBootApplication
@EnableDiscoveryClient
public class TalkServerApplication implements CommandLineRunner{
    @Autowired
    WebsocketServer talkServer;
    
	public static void main(String[] args) {
		SpringApplication.run(TalkServerApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		ChannelFuture future = talkServer.start(9998);
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run() {
            	talkServer.destroy();
            }
        });
        //服务端管道关闭的监听器并同步阻塞,直到channel关闭,线程才会往下执行,结束进程
        future.channel().closeFuture().syncUninterruptibly();
	}

}
