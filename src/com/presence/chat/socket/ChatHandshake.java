//
//  ChatHandshake.java
//  ChatServer
//
//  Created by John McKisson on 1/7/10.
//  Copyright 2010 Jefferson Lab. All rights reserved.
//
package com.presence.chat.socket;

import java.util.logging.*;

import org.jboss.netty.channel.*;

import com.presence.chat.ChatClient;

@ChannelPipelineCoverage("one")
public class ChatHandshake extends SimpleChannelUpstreamHandler {

	@Override
	public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
		if (e instanceof ChannelStateEvent) {
			Logger.getLogger("global").info(e.toString());
		}
		super.handleUpstream(ctx, e);
	}

	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		
		Logger.getLogger("global").info(e.toString());
	}
	
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
		
		ChannelPipeline pipeline = ctx.getPipeline();
		
		String str = (String)e.getMessage();
		
		if (str.contains(":")) {
			String[] result = str.split(":");
			
			if (result[0].equals("CHAT")) {
			
				//Insert MudMaster protocol handler into the pipeline
				pipeline.replace("decoder", "protocol", new ChatProtocolDecoder());
				
			} else if (result[0].equals("ZCHAT")) {
			
				//Insert ZChat protocol handler into the pipeline
			
			} else {
				System.out.println("ChatProtocolFactory: Unknown Incoming Chat Protocol!");
				return;
			}
			
			pipeline.replace("handshake", "client", new ChatClient());
			
			ctx.setAttachment(new String(result[1]));

		}
		
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
		Logger.getLogger("global").log(Level.WARNING, "Unexpected downstream exception", e.getCause());
		e.getChannel().close();
		
		//disconnect();
	}
}
