//
//  TelnetServer.java
//  jMudServer
//
//  Created by John McKisson on 9/22/09.
//  Copyright 2009 Jefferson Lab. All rights reserved.
//
package com.presence.chat.socket;

import com.presence.chat.ChatServer;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.logging.*;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFactory;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.Version;

public class TelnetServer {

	EventLoopGroup bossGroup;
	EventLoopGroup workerGroup;

	private Channel serverChannel;

	public TelnetServer(int port) {
	
		Logger.getLogger("global").info("Starting... " + ChatServer.VERSION + " on port " + port);

		bossGroup = new NioEventLoopGroup(1);
		workerGroup = new NioEventLoopGroup();

		try {
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup)
			 .channel(NioServerSocketChannel.class)
			 .handler(new LoggingHandler(LogLevel.INFO))
			 .childHandler(new ChannelInitializer<SocketChannel>() {
				 @Override
				 public void initChannel(SocketChannel ch) throws Exception {
					 ch.pipeline().addLast("decoder", new StringDecoder());
					 ch.pipeline().addLast("encoder", new StringEncoder());
					 ch.pipeline().addLast("handshake", new ChatHandshake());
				 }
			 });

			serverChannel = b.bind(port).sync().channel();

		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}
	
	public void shutdown() {
		Logger.getGlobal().info("shutdown");

		try {
			serverChannel.closeFuture().sync();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		bossGroup.shutdownGracefully();
		workerGroup.shutdownGracefully();
	}

}
