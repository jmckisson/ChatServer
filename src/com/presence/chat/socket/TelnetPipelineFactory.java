//
//  TelnetPipelineFactory.java
//  jMudServer
//
//  Created by John McKisson on 9/22/09.
//  Copyright 2009 Jefferson Lab. All rights reserved.
//
package com.presence.chat.socket;

import com.presence.chat.ChatClient;

import org.jboss.netty.channel.*;
import org.jboss.netty.handler.codec.frame.DelimiterBasedFrameDecoder;
import org.jboss.netty.handler.codec.frame.Delimiters;
import org.jboss.netty.handler.codec.string.*;

import static org.jboss.netty.channel.Channels.*;

public class TelnetPipelineFactory implements ChannelPipelineFactory {
	
	public ChannelPipeline getPipeline() throws Exception {
		ChannelPipeline pipeline = pipeline();
		
		pipeline.addLast("framer", new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
		pipeline.addLast("decoder", new StringDecoder());
		pipeline.addLast("encoder", new StringEncoder());
		pipeline.addLast("handshake", new ChatHandshake());
		
		return pipeline;
	}

}
