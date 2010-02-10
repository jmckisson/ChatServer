//
//  ChatProtocol.java
//  ChatServer
//
//  Created by John McKisson on 3/31/08.
//  Copyright 2008 __MyCompanyName__. All rights reserved.
//
package com.presence.chat.protocol;

import java.util.logging.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.jboss.netty.buffer.*;
import org.jboss.netty.channel.Channel;

import com.presence.chat.*;

import static com.presence.chat.ANSIColor.*;

public abstract class ChatProtocol {
	private static final Logger log = Logger.getLogger("global");
	
	Channel sock;
	ChatClient myClient;
	String content;
	String version;
	
	static final int STATE_DISCONNECTED = 0;
	static final int STATE_AUTHENTICATING = 1;
	static final int STATE_CONNECTED = 2;
	
	static final byte CHAT_NAME_CHANGE = 1;
	static final byte CHAT_TEXT_EVERYBODY = 4;
	static final byte CHAT_TEXT_PERSONAL = 5;
	static final byte CHAT_VERSION = 19;
	
	static final byte CHAT_SNOOP = 30;
	static final byte CHAT_SNOOP_DATA = 31;
	static final byte CHAT_SNOOP_COLOR = 32;
	
	public static final byte CHAT_END_OF_COMMAND = (byte)255;

	int state;
	
	public ChatProtocol(ChatClient client) {
		this.sock = client.getSocket();
		myClient = client;
		
		state = STATE_DISCONNECTED;
	}
	
	public String toString() {
		return "Generic";
	}
	
	
	public void closeSocket() {
		try {
			sock.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public void setClient(ChatClient client) {
		myClient = client;
		
		//System.out.println("ChatProtocol:: setClient");
	}
	
	
	public void sendToSocket(String str) {
		sock.write(str);
	}
	
	
	/*
	 * Sends a string to the socket encapsulating it in a chat command
	 */
	public void chatCmdToSocket(byte cmd, String str) {

		
		ChatAccount ac = myClient.getAccount();
		
		if (ac != null && !ac.isCompact())
			str += "\n";
		
		byte[] bytes = str.getBytes();
		
			
		ChannelBuffer buf = ChannelBuffers.buffer(bytes.length + 4);
		
		buf.writeByte(cmd);
		buf.writeBytes(bytes);
		buf.writeByte(CHAT_END_OF_COMMAND);
		
		if (!sock.isOpen())
			return;
			
		try {
			sock.write(buf);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public void processIncomingData(String data) {
	
		switch (state) {
			case STATE_DISCONNECTED:
				break;
				
			case STATE_CONNECTED:
			
				handleConnectedState(data);
				break;
		}
	}
	
	/**
	 * Match 1 command byte along with its content
	 */
	static Matcher matcher = Pattern.compile("(.{1})\n*(.*)\n*(\\u02c7?)").matcher("");
	
	int findByte(byte t, byte[] b, int offset) {
		for (int i = offset; i < b.length; i++)
			if (b[i] == t)
				return i;
				
		return -1;
	}

	
	
	/**
	 * Process an incoming command from this client
	 */
	void handleConnectedState(String data) {
	
		myClient.lastActivity = System.currentTimeMillis();
		
		//Ok we need to loop thru and process each chat command one by one
		//String bufStr = ChatServer.bufToString();
		byte[] b = data.getBytes();
		int cmdIdx = 0;
		int cmdStopIdx = 0;
		int stackCount = 0;
		
		while (cmdIdx < b.length) {
			//Read command byte
			byte cmd = b[cmdIdx];
			
			stackCount++;
			
			//Find command end byte
			cmdStopIdx = findByte((byte)0xff, b, cmdIdx + 1);
			if (cmdStopIdx == -1)
				cmdStopIdx = b.length;
				
			//Extract command contents
			content = new String(b, cmdIdx + 1, cmdStopIdx - cmdIdx - 1);
						
			cmdIdx = cmdStopIdx + 1;
			
			switch (cmd) {
				case CHAT_TEXT_EVERYBODY:
				
					if (myClient.isAuthenticated())
						myClient.forwardToRoom(content);
						
					break;
					
				case CHAT_TEXT_PERSONAL:

					//Client needs to be able to chat their password while unauthenticated
					ChatServer.processCommand(myClient, content);
					
					break;
					
				case CHAT_VERSION:
				
					setVersion();
					break;
					
				case CHAT_NAME_CHANGE:
					
					if (myClient.isAuthenticated()) {
						myClient.setName(content);
						ChatServer.getStats().namechanges++;
					}
						
					break;
					
				case CHAT_SNOOP_DATA:
				
					myClient.logSnoopedData(content);
					
					break;
			}
		}

		
		if (stackCount > 1)
			log.warning(String.format("%s sent %d stacked commands", myClient.getName(), stackCount));
	}

	public void sendVersion() {
		chatCmdToSocket(CHAT_VERSION, ChatServer.VERSION);
	}
	
	void setVersion() {
		version = new String(content);
		
		//System.out.printf("ChatProtocol:: version: %s\n", version);
	}
	
	
	/*
	 * Return the content of the last received command
	 */
	public String getContent() {
		return content;
	}
	
	
	public void sendChat(String msg) {
		chatCmdToSocket(CHAT_TEXT_PERSONAL, msg);
	}
	
	/*
	 *
	 */
	public void sendChatAll(String msg) {
		//String outString = String.format("%c%s%c", 4, msg, 255);
		
		chatCmdToSocket(CHAT_TEXT_EVERYBODY, msg);
	}
	
	
	public abstract void processHandshake(String str);
	public abstract void sendConnectResponse();
	
	//do these really need to be abstract?
	//public abstract void sendChatAll(String str);
	//public abstract void sendChat(String str);
}
