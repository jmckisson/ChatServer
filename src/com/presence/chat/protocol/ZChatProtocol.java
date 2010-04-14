//
//  ZChatProtocol.java
//  ChatServer
//
//  Created by John McKisson on 3/31/08.
//  Copyright 2008 __MyCompanyName__. All rights reserved.
//
package com.presence.chat.protocol;

import java.nio.channels.SocketChannel;

import java.nio.ByteBuffer;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import com.presence.chat.*;

public class ZChatProtocol extends ChatProtocol {
	
	public static final String PREFIX = "ZCHAT";

	public ZChatProtocol(ChatClient client) {
		super(client);
	}
	
	public String toString() {
		return "ZChat";
	}
	
	public void sendRaw(String msg) {
	}
	
	@Deprecated
	public void processHandshake(String str) {
	
		//ZCHAT:<ChatName>\t<ChatID>\lf<ClientIPAddress><ClientPort>\lf<SecurityInfo>
		//^^ this is wrong
		Pattern pat = Pattern.compile("^(\\w+)");
		
		System.out.printf("'%s'", str);
		
		//System.out.println(pat.toString());
		
		Matcher matcher = pat.matcher(str);
		
		for (int grp = 1; grp <= matcher.groupCount(); grp++) {
			if (matcher.find()) {
				
				String name = matcher.group(grp);
				
				//System.out.printf("Found match '%s'\n", name);
				
				if (myClient == null)
					System.out.println("Client is null! about to spew exception!");
					
				System.out.printf("name = %s\n", matcher.group(grp));
				
				//myClient.connect(matcher.group(grp));
			}
		}
	}

}
