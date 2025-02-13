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

import io.netty.buffer.ByteBuf;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.channel.ChannelPipeline;
//import org.jboss.netty.channel.ChannelHandler.*;


import com.presence.chat.protocol.ChatCommand;


public class ChatProtocolDecoder extends ByteToMessageDecoder {

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
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

		DecodedMsg decMsg = new DecodedMsg();

		//Grab command byte
		byte cmd = in.readByte();

		ChatCommand chatCmd = ChatCommand.getCommand(cmd);

		decMsg.setCmd(chatCmd);
		ByteBuf strBuf = in.readBytes(in.readableBytes() - 1);
		decMsg.setMessage(strBuf.toString(charset));
		decMsg.setEndByte(in.readByte());

		// What are we doing here?
		//Object[] obj = new Object[3];
		//obj[2] = in.copy();
				
		//Grab command byte
		//byte cmd = in.readByte();
		
		//obj[0] = ChatCommand.getCommand(cmd);
		
		//int readable = in.readableBytes();
		
		//obj[1] = in.toString(in.readerIndex(), readable/* - 1*/, charset);

		//Logger.getLogger("global").info(String.format("bytes: %d, decoded cmd: %x, last %x", readable, cmd, in.readByte()));
		
		out.add(decMsg);
	}

}
