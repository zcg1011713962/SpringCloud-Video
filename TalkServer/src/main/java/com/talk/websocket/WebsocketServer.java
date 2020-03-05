package com.talk.websocket;

import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
@Component
public class WebsocketServer {
	private static Logger logger = LoggerFactory.getLogger(WebsocketServer.class);
	private Channel channel;
	private NioEventLoopGroup bossGroup;
	private NioEventLoopGroup workGroup;
	private ChannelFuture channelFuture;

	public ChannelFuture start(int port) {
		// 接收连接线程组
		bossGroup = new NioEventLoopGroup();
		// 工作线程组
		workGroup = new NioEventLoopGroup();
		ServerBootstrap serverBootstrap = new ServerBootstrap();
		// 指定使用的channel
		serverBootstrap.group(bossGroup, workGroup).channel(NioServerSocketChannel.class)
				.handler(new LoggingHandler(LogLevel.INFO)).childHandler(new WebsocketChannelInitalizer())
				.option(ChannelOption.SO_BACKLOG, 2048)// serverSocketchannel的设置，链接缓冲池的大小
				.childOption(ChannelOption.SO_KEEPALIVE, true)// socketchannel的设置,维持链接的活跃，清除死链接
				.childOption(ChannelOption.TCP_NODELAY, true);// socketchannel的设置,关闭延迟发送
		try {
			channelFuture = serverBootstrap.bind(new InetSocketAddress(port)).sync();
			channel = channelFuture.channel();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			if (channelFuture != null && channelFuture.isSuccess()) {
				logger.info("netty聊天服务器启动成功");
			} else {
				logger.error("netty聊天服务器启动失败");
			}
		}
		return channelFuture;
	}

	/**
	 * 停止server
	 */
	public void destroy() {
		if (channel != null) {
			channel.close();
		}
		bossGroup.shutdownGracefully();
		workGroup.shutdownGracefully();
		logger.info("netty聊天服务器关闭成功");
	}

}
