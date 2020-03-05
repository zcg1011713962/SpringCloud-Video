package com.video;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.video.websocket.WebsocketServer;

import io.netty.channel.ChannelFuture;

@SpringBootApplication
public class VideoServer1Application implements CommandLineRunner{
	@Autowired
	WebsocketServer videoServer;
	
	public static void main(String[] args) {
		SpringApplication.run(VideoServer1Application.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		ChannelFuture future = videoServer.start(9997);
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run() {
            	videoServer.destroy();
            }
        });
        //服务端管道关闭的监听器并同步阻塞,直到channel关闭,线程才会往下执行,结束进程
        future.channel().closeFuture().syncUninterruptibly();
	}

}
