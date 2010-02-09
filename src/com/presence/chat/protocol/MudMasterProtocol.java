//
//  MudMasterProtocol.java
//  ChatServer
//
//  Created by John McKisson on 3/31/08.
//  Copyright 2008 __MyCompanyName__. All rights reserved.
//
package com.presence.chat.protocol;

import java.nio.ByteBuffer;

import java.nio.channels.SocketChannel;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import com.presence.chat.*;

public class MudMasterProtocol extends ChatProtocol {

	public static final String PREFIX = "CHAT";
	

	public MudMasterProtocol(ChatClient client) {
		super(client);
		
		//System.out.println("new MM");
	}
	
	public String toString() {
		return "MudMaster";
	}

	/*
	 */
	public void processHandshake(String str) {
		
		//Pattern pat = Pattern.compile("CHAT:(\w+)\s(\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3})?(\d+)");
		Pattern pat = Pattern.compile("^(\\w+)");
		
		//System.out.println(pat.toString());
		
		Matcher matcher = pat.matcher(str);
		
		for (int grp = 1; grp <= matcher.groupCount(); grp++) {
			if (matcher.find()) {
				
				String name = matcher.group(grp);
				
				//System.out.printf("Found match '%s'\n", name);
				
				if (myClient == null)
					System.out.println("Client is null! about to spew exception!");
				
				myClient.connect(matcher.group(grp));
			}
		}
	}
	
	
	//public void sendChat(String msg) {
	//	chatCmdToSocket(CHAT_TEXT_PERSONAL, msg);
	//}
	
	
	/*
	 *
	 */
	//public void sendChatAll(String msg) {
		//String outString = String.format("%c%s%c", 4, msg, 255);
		
	//	chatCmdToSocket(CHAT_TEXT_EVERYBODY, msg);
	//}
	

	
	
	/*
	 */
	public void sendConnectResponse() {
		String str = String.format("YES:%s\n", ChatPrefs.getName());
		
		sendToSocket(str);
		
		state = STATE_CONNECTED;
	}

}
