//
//  ChatProtocol.java
//  ChatServer
//
//  Created by John McKisson on 3/31/08.
//  Copyright 2008 __MyCompanyName__. All rights reserved.
//
package com.presence.chat.protocol;

import io.netty.buffer.*;
import io.netty.channel.Channel;

import java.util.logging.*;

import com.presence.chat.*;

import static com.presence.chat.ANSIColor.*;
import static com.presence.chat.protocol.ChatCommand.*;

public abstract class ChatProtocol {	
	Channel sock;
	ChatClient myClient;
	String content;
	String version;
	
	ByteBuf lastBuffer;
	
	static final int MAX_LEN = 3000;
	
	boolean bufLimited = false;
		
	//Socket needs to be set first
	public ChatProtocol(ChatClient client) {
		this.sock = client.getSocket();
		myClient = client;
		
	}
	
	public String toString() {
		return "Generic";
	}
	
	public ByteBuf getLastBuffer() {
		return lastBuffer;
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
		sock.writeAndFlush(str);
	}
	
	private void bufToSocket(byte cmd, String str) {
		byte[] bytes = str.getBytes();
		
		ByteBuf buf = Unpooled.buffer(bytes.length + 4);
		
		buf.writeByte(cmd);
		buf.writeBytes(bytes);
		buf.writeByte(END.commandByte());
		
		if (!sock.isOpen())
			return;
			
		lastBuffer = Unpooled.copiedBuffer(buf);
			
		try {
			sock.writeAndFlush(buf);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * Sends a string to the socket encapsulating it in a chat command
	 */
	public void chatCmdToSocket(byte cmd, String str) {
		ChatAccount ac = myClient.getAccount();
		
		if (ac != null && !ac.isCompact())
			str += "\n";
			
		if (!bufLimited || str.length() < MAX_LEN) {
			bufToSocket(cmd, str);
			
		} else {
			//Break up the message into smaller packets so other clients can handle them
			int idx = MAX_LEN; //Start at some index close to the limit
			int loc = 0;
			
			//Logger.getLogger("global").info("Splitting up long message");
			
			boolean needBold = false;
			
			while (idx != -1) {
				//System.out.println("loc " + loc + " idx " + idx);
			
				while (true) {
					idx = str.indexOf("\n", idx);
					//System.out.println("idx " + idx);
					if (idx == -1 || idx - loc > MAX_LEN)	//Hit end of string or exceeded max buf length
						break;
					
					idx++;	//Keep searching
				}
				
				int end = idx != -1 ? idx : str.length();
				
				String s = str.substring(loc, end);
				
				//System.out.println("substring from " + loc + " to " + end);
				bufToSocket(cmd, (needBold ? BLD : "") + s);
				if (idx == -1)
					break;
					
				loc = idx;
				idx += MAX_LEN;
				
				
				//Apparently MM and Zmud reset back to NRM for each incoming chat
				//So here we find out if the last character is supposed to be bold
				//and prepend a bold to the next buffer... (stupid, yes)
				int boldIdx = s.lastIndexOf("[1m");
				int nrmIdx = s.lastIndexOf("[0m");
				needBold = (boldIdx > nrmIdx);
				
			}
		}
	}

	
	public void sendVersion() {
		bufToSocket(VERSION.commandByte(), ChatServer.VERSION);
	}
	
	public String getVersion() {
		return version;
	}
	
	public void setVersion(String data) {
		version = data;
		String lowVersion = version.toLowerCase();
		if (lowVersion.contains("master") || lowVersion.contains("zchat"))
			bufLimited = true;
	}
	
	
	/*
	 * Return the content of the last received command
	 */
	public String getContent() {
		return content;
	}
	
	
	public void sendChat(String msg) {
		chatCmdToSocket(TEXT_ONE.commandByte(), msg);
	}
	
	/*
	 *
	 */
	public void sendChatAll(String msg) {
		//String outString = String.format("%c%s%c", 4, msg, 255);
		
		chatCmdToSocket(TEXT_ALL.commandByte(), msg);
	}
	
	public void sendNameChange(String name) {
		bufToSocket(NAME_CHANGE.commandByte(), name);
	}
	
	/*
	 */
	public void sendConnectResponse() {
		String str = String.format("YES:%s\n", ChatPrefs.getName());
		
		sendToSocket(str);
	}
	
	public void sendPingResponse(String msg) {
		bufToSocket(PING_RESPONSE.commandByte(), msg);
	}
	
	public void startSnoop() {
		bufToSocket(SNOOP.commandByte(), "");
	}
}
