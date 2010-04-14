//
//  ChatProtocol.java
//  ChatServer
//
//  Created by John McKisson on 3/31/08.
//  Copyright 2008 __MyCompanyName__. All rights reserved.
//
package com.presence.chat.protocol;

import org.jboss.netty.buffer.*;
import org.jboss.netty.channel.Channel;

import com.presence.chat.*;

import static com.presence.chat.ANSIColor.*;

public abstract class ChatProtocol {	
	Channel sock;
	ChatClient myClient;
	String content;
	String version;
	
	static final byte CHAT_NAME_CHANGE = 1;
	static final byte CHAT_TEXT_EVERYBODY = 4;
	static final byte CHAT_TEXT_PERSONAL = 5;
	static final byte CHAT_VERSION = 19;
	
	static final byte CHAT_SNOOP = 30;
	static final byte CHAT_SNOOP_DATA = 31;
	static final byte CHAT_SNOOP_COLOR = 32;
	
	public static final byte CHAT_END_OF_COMMAND = (byte)255;
	
	public ChatProtocol(ChatClient client) {
		this.sock = client.getSocket();
		myClient = client;
		
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

	
	public void sendVersion() {
		chatCmdToSocket(CHAT_VERSION, ChatServer.VERSION);
	}
	
	void setVersion() {
		version = new String(content);		
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
	
	/*
	 */
	public void sendConnectResponse() {
		String str = String.format("YES:%s\n", ChatPrefs.getName());
		
		sendToSocket(str);
	}
}
