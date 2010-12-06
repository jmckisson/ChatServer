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

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.util.Version;

public class TelnetServer {
	ChannelFactory factory;

	public TelnetServer(int port) {
	
		Logger.getLogger("global").info("Starting... " + ChatServer.class.getPackage().getImplementationVersion() + " on port " + port);
		Logger.getLogger("global").info("Socket Library: " + Version.ID);
	
		factory = new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool());
		
		ServerBootstrap bootstrap = new ServerBootstrap(factory);
		
		bootstrap.setPipelineFactory(new TelnetPipelineFactory());
		bootstrap.setOption("child.tcpNoDelay", true);
		bootstrap.setOption("child.keepAlive", true);
		
		bootstrap.bind(new InetSocketAddress(port));
	}
	
	public void shutdown() {
		factory.releaseExternalResources();
	}

}
