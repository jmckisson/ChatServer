//
//  ChatProtocolDecoder.java
//  ChatServer
//
//  Created by John McKisson on 1/6/10.
//  Copyright 2010 Jefferson Lab. All rights reserved.
//
package com.presence.chat.socket;

import java.nio.charset.Charset;
import java.util.*;
import java.util.logging.*;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
//import org.jboss.netty.channel.ChannelHandler.*;
import org.jboss.netty.handler.codec.frame.DelimiterBasedFrameDecoder;
import org.jboss.netty.handler.codec.frame.Delimiters;
import org.jboss.netty.handler.codec.frame.FrameDecoder;
import org.jboss.netty.handler.codec.oneone.OneToOneDecoder;

import com.presence.chat.protocol.ChatCommand;

//@ChannelPipelineCoverage("all")
//@org.jboss.netty.channel.ChannelHandler.Sharable
public class ChatProtocolDecoder extends OneToOneDecoder {

	private final String charsetName;
	private final Charset charset;
	
	private byte currentCommand;

	public ChatProtocolDecoder() {
		this(Charset.defaultCharset());
	}
	
	public ChatProtocolDecoder(Charset charset) {
		if (charset == null) {
			throw new NullPointerException("charset");
		}
		this.charset = charset;
		charsetName = charset.name();
	}

	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {
	
		if (!(msg instanceof ChannelBuffer)) {
			return msg;
		}
		
		ChannelBuffer buf = (ChannelBuffer)msg;
		
		//int bytes = buf.readableBytes();
		Object[] obj = new Object[3];
		obj[2] = buf.copy();
				
		//Grab command byte
		byte cmd = buf.readByte();
		
		obj[0] = ChatCommand.getCommand(cmd);
		
		int readable = buf.readableBytes();
		
		obj[1] = buf.toString(buf.readerIndex(), readable/* - 1*/, charset);
		
		//Logger.getLogger("global").info(String.format("bytes: %d, decoded cmd: %x, last %x", readable, cmd, buf.readByte()));
		
		return obj;
	}

}
