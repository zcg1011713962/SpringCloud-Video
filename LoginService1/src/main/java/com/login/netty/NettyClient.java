package com.login.netty;

import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

@Component
public class NettyClient {
	private static Logger logger = LoggerFactory.getLogger(NettyClient.class);
	private NioEventLoopGroup group;
	private Bootstrap bootstrap;
	private Channel channel;

	// 启动netty client
	public ChannelFuture start(String ip,int port) {
		// 工作线程组
		group = new NioEventLoopGroup();
		ChannelFuture f = null;
		try {
			bootstrap = new Bootstrap(); // (1)引导
			bootstrap.group(group); // (2)
			bootstrap.channel(NioSocketChannel.class); // (3)
			bootstrap.option(ChannelOption.SO_KEEPALIVE, true); // (4)
			bootstrap.handler(new ChannelInitializer<SocketChannel>() {
				@Override
				public void initChannel(SocketChannel ch) throws Exception {
					ch.pipeline().addLast("decoder",
							new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.cacheDisabled(null)));// 解码
					ch.pipeline().addLast("encoder", new ObjectEncoder());// 编码

					ch.pipeline().addLast(new NettyClientHandler());// 适配器
				}
			});
			// Start the client
			f = bootstrap.connect(new InetSocketAddress(ip, port)).sync();
			channel=f.channel();
		} catch (InterruptedException e) {
			try {
				group.shutdownGracefully().sync();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		} finally {
			if (f != null && f.isSuccess()) {
				logger.info("Netty client listening ");
			} else {
				logger.error("Netty client start up Error!");
			}
		}
		return f;
	}
	
	/**
     * 停止client
     */
    public void destroy() {
        if(channel != null) { channel.close();}
        group.shutdownGracefully();
        logger.info("Shutdown Netty client Success!");
    }
}
