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
		
		//System.out.println("new ZCHAT");
	}
	
	public String toString() {
		return "ZChat";
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
	
	public void sendRaw(String msg) {
	}
	
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
				
				myClient.connect(matcher.group(grp));
			}
		}
	}
	
	
	public void sendConnectResponse() {
		String str = String.format("YES:%s\n", ChatPrefs.getName());
		
		sendToSocket(str);
		
		state = STATE_CONNECTED;
	}
	
	/*
	 * Process an incoming command from this client
	 */
	void handleConnectedState(String data) {
	
		myClient.lastActivity = System.currentTimeMillis();
	
		int stamp;
		/*
		//Read chat cmd
		short cmd = buf.getShort();
		
		//Read length
		short len = buf.getShort();
		
		switch (cmd) {
			case CHAT_TEXT_EVERYBODY:
			
				if (!myClient.isAuthenticated())
					break;
			
				stamp = buf.getInt();
			
				content = ChatServer.bufToString();
				//content = content.substring(0, content.length() - 1);

				myClient.forwardToRoom(content);
					
				break;
				
			case CHAT_TEXT_PERSONAL:
			
				stamp = buf.getInt();
				
				content = ChatServer.bufToString();
				//content = content.substring(0, content.length() - 1);

				ChatServer.processCommand(myClient, content);
				
				break;
				
			case CHAT_VERSION:
			
				setVersion();
				break;
		}
		*/
	}


}
