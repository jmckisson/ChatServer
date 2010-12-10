//
//  MessageLog.java
//  ChatServer
//
//  Created by John McKisson on 12/10/10.
//  Copyright 2010 Jefferson Lab. All rights reserved.
//
package com.presence.chat.log;

import java.io.IOException;
import java.util.logging.*;

import com.presence.chat.*;

public class MessageLog {
	
	String name;
	
	static final int MAX_SIZE = 1000;
		
	ChatLogHandler clHandler;
	Logger msgLog;

	public MessageLog(String name) {
		this.name = name;
		
		//Setup logging
		msgLog = Logger.getLogger(name);
		msgLog.setUseParentHandlers(false);
		
		//File log uses ansi stripping formatter
		try {
			Handler fileHandler = new FileHandler("log/messages_" + name + "_%g.log", 1000000, 10, true);
			fileHandler.setFormatter(new ChatLogANSIStrippedFormatter());
			msgLog.addHandler(fileHandler);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		clHandler = new ChatLogHandler(MAX_SIZE);
		clHandler.setFormatter(new ChatLogFormatter());
		
		msgLog.addHandler(clHandler);
	}
	
	public String getHistory(ChatClient sender, String[] args) {
		if (clHandler.size() == 0)
			return String.format("%s chats to you, 'You don't have any messages!'", ChatPrefs.getName());
		
		return clHandler.getFormattedHistory(sender, args);
	}
	
}

