//
//  ChatLog.java
//  ChatServer
//
//  Created by John McKisson on 4/16/08.
//  Copyright 2008 __MyCompanyName__. All rights reserved.
//
package com.presence.chat.log;

import java.io.IOException;
import java.util.logging.*;

import com.presence.chat.*;

public class ChatLog {
	
	String name;
	
	static final int MAX_SIZE = 10000;
		
	ChatLogHandler clHandler;
	Logger roomLog;	//Strong reference to this rooms log to prevent it from being garbage collected

	public ChatLog(String name) {
		this.name = name;
		
		//Setup logging
		roomLog = Logger.getLogger(name);
		roomLog.setUseParentHandlers(false);
		
		//File log uses ansi stripping formatter
		try {
			Handler fileHandler = new FileHandler("log/room_" + name + "_%g.log", 1000000, 10, true);
			fileHandler.setFormatter(new ChatLogANSIStrippedFormatter());
			roomLog.addHandler(fileHandler);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		clHandler = new ChatLogHandler(MAX_SIZE);
		clHandler.setFormatter(new ChatLogFormatter());
		
		roomLog.addHandler(clHandler);
		
		//Logger.getLogger("global").info("Setting up room log for: " + name + "  handler: " + clHandler + "  logger: " + roomLog);
	}
	
	public String getHistory(ChatClient sender, String[] args) {
		if (clHandler.size() == 0)
			return String.format("%s chats to you, 'This room has no messages!'", ChatPrefs.getName());
	
		return clHandler.getFormattedHistory(sender, args);
	}
	
}
