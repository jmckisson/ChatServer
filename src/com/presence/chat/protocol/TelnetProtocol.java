//
//  TelnetProtocol.java
//  ChatServer
//
//  Created by John McKisson on 5/13/08.
//  Copyright 2008 __MyCompanyName__. All rights reserved.
//
package com.presence.chat.protocol;

import java.nio.ByteBuffer;

import com.presence.chat.*;

import static com.presence.chat.ANSIColor.*;

public class TelnetProtocol extends ChatProtocol {

	public TelnetProtocol(ChatClient client) {
		super(client);
	}
	
	public String toString() {
		return "Telnet";
	}
	
	
	/**
	 * @param str Chatname of the incoming telnet client
	 */
	public void processHandshake(String str) {
	
		//This just sets the chatname
		myClient.setName(str.trim());
	}
	

	public void sendConnectResponse() {
	}
	
	public void sendVersion() {}
	
	
	/**
	 * Process an incoming command from this client
	 */
	void handleConnectedState(String data) {
	
		myClient.lastActivity = System.currentTimeMillis();
		
		//Ok, telnet is simple, just extract content and process the command
		//We need to encapsulate the content as well
		
		content = String.format("%s chats to you, '%s'", myClient.getName(), data);
		
		//System.out.println("telnet processing " + content);
		
		//Treat everything as a personal chat
		ChatServer.processCommand(myClient, content);
	}

	
	
	/**
	 * Sends a string to the socket.
	 * @param cmd 
	 */
	public void chatCmdToSocket(byte cmd, String str) {
		
		ChatAccount ac = myClient.getAccount();
		
		if (ac != null && !ac.isCompact())
			str += "\n";
			
		//Prevent color bleeding
		str += NRM;
		
		if (!sock.isOpen())
			return;
			
		try {
			sock.write(str);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
