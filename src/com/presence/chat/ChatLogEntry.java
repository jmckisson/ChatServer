//
//  ChatLogEntry.java
//  ChatServer
//
//  Created by John McKisson on 4/16/08.
//  Copyright 2008 __MyCompanyName__. All rights reserved.
//
package com.presence.chat;

import java.util.Date;

public class ChatLogEntry {
	Date date;
	String message;

	
	public ChatLogEntry(String msg) {
		date = new Date(System.currentTimeMillis());
		
		message = msg;
	}
	
	public Date getDate() {
		return date;
	}
	
	public String getMessage() {
		return message;
	}
	
	public String getStrippedMessage() {
		return message.replaceAll("\u001b\\[[0-9;]+m", "");
	}
}
