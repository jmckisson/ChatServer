//
//  CMDMessageLog.java
//  ChatServer
//
//  Created by John McKisson on 11/11/10.
//  Copyright 2008 __MyCompanyName__. All rights reserved.
//
package com.presence.chat.commands;

import com.presence.chat.*;

import static com.presence.chat.ANSIColor.*;

public class CMDMessageLog extends CMDLog implements Command {

	public String help() {
		return "Shows your private message log";
	}
	
	public String usage() {
		return String.format(ChatServer.USAGE_STRING, "plog [<#>|<grep> [<#>]]");
	}


	public boolean execute(ChatClient sender, String[] args) {
		
		//Get log for the current room
		ChatLog messageLog = sender.getMessageLog();
		
		if (messageLog.size() == 0) {
			sender.sendChat(String.format("%s chats to you, 'You don't have any messages!'", ChatPrefs.getName()));
			return true;
		}
		
		String strBuf = messageLog.getLog(sender, args);
		
		if (strBuf.length() > 0)
			sender.sendChat(strBuf);
		else
			sender.sendChat(String.format("%s chats to you, 'There are no messages'", ChatPrefs.getName()));
		

		return true;
	}
	
}
