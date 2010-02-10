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
import org.jboss.netty.channel.ChannelPipelineCoverage;
import org.jboss.netty.handler.codec.frame.DelimiterBasedFrameDecoder;
import org.jboss.netty.handler.codec.frame.Delimiters;
import org.jboss.netty.handler.codec.frame.FrameDecoder;
import org.jboss.netty.handler.codec.oneone.OneToOneDecoder;

import com.presence.chat.protocol.ChatCommand;

@ChannelPipelineCoverage("all")
public class ChatProtocolDecoder extends OneToOneDecoder {

	private final String charsetName;
	
	private byte currentCommand;

	public ChatProtocolDecoder() {
		this(Charset.defaultCharset());
	}
	
	public ChatProtocolDecoder(Charset charset) {
		if (charset == null) {
			throw new NullPointerException("charset");
		}
		charsetName = charset.name();
	}

	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {
	
		if (!(msg instanceof ChannelBuffer)) {
			return msg;
		}
		
		ChannelBuffer buf = (ChannelBuffer)msg;
		
		//int bytes = buf.readableBytes();
				
		//Grab command byte
		byte cmd = buf.readByte();
		
		//Logger.getLogger("global").info(String.format("bytes: %d, decoded cmd: %x", bytes, cmd));
		
		Object[] obj = new Object[2];
		
		obj[0] = ChatCommand.getCommand(cmd);
		obj[1] = buf.slice(buf.readerIndex(), buf.readableBytes() - 1).toString(charsetName);
		
		return obj;
	}

}
