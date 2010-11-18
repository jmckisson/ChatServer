//
//  ChatLogEntry.java
//  ChatServer
//
//  Created by John McKisson on 4/16/08.
//  Copyright 2008 __MyCompanyName__. All rights reserved.
//
package com.presence.chat;

import java.util.Date;
import java.util.logging.*;

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
		if (message == null) {
			Logger.getLogger("global").warning("entry somehow null!");
			return "";
		} else
			return message.replaceAll("\u001b\\[[0-9;]+m", "");
	}
}
