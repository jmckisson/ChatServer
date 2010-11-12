//
//  ChatHandshake.java
//  ChatServer
//
//  Created by John McKisson on 1/7/10.
//  Copyright 2010 Jefferson Lab. All rights reserved.
//
package com.presence.chat.socket;

import java.util.logging.*;

import org.jboss.netty.buffer.*;
import org.jboss.netty.channel.*;

import org.jboss.netty.handler.codec.frame.DelimiterBasedFrameDecoder;

import com.presence.chat.ChatClient;
import com.presence.chat.protocol.*;

import static com.presence.chat.protocol.ChatCommand.*;

//@ChannelPipelineCoverage("one")
public class ChatHandshake extends SimpleChannelUpstreamHandler {

	/*
	@Override
	public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
		if (e instanceof ChannelStateEvent) {
			Logger.getLogger("global").info(e.toString());
		}
		super.handleUpstream(ctx, e);
	}
	*/

	/*
	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		
		Logger.getLogger("global").info(e.toString());
	}
	*/
	
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
		
		ChannelPipeline pipeline = ctx.getPipeline();
		
		String str = (String)e.getMessage();
				
		String[] nameAndIP = null;
		
		if (str.contains(":")) {
			String[] result = str.split(":");
			
			if (result[0].equals("CHAT")) {
						
				//Insert MudMaster protocol handler into the pipeline
				pipeline.replace("decoder", "protocol", new ChatProtocolDecoder());
				
				nameAndIP = result[1].split("\n");
				
			} else if (result[0].equals("ZCHAT")) {
			
				//Insert ZChat protocol handler into the pipeline
			
			} else {
				System.out.println("Unknown Incoming Chat Protocol!");
				return;
			}
			
			ChannelBuffer endOfCommand = ChannelBuffers.buffer(1);
			endOfCommand.writeByte(END.commandByte());
			
			pipeline.addLast("framer", new DelimiterBasedFrameDecoder(8192, endOfCommand));
			
			if (nameAndIP == null) {
				nameAndIP = new String[2];
				nameAndIP[0] = "Woops";
				nameAndIP[1] = "Something broke";
			}
			
			ChatClient client = new ChatClient(nameAndIP[0], nameAndIP[1]);
			
			pipeline.replace("handshake", "client", client);
						
			client.setSocket(e.getChannel());
			
			ChatProtocol protocol = new MudMasterProtocol(client);
			
			client.setProtocol(protocol);
			
			protocol.sendConnectResponse();
			protocol.sendVersion();
			
		} else {
			//Use Telnet protocol
		}
		
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
		Logger.getLogger("global").log(Level.WARNING, "Unexpected downstream exception", e.getCause());
		e.getChannel().close();
		
		e.getCause().printStackTrace();
		
		//disconnect();
	}
}
