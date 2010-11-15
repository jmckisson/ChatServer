//
//  CMDLog.java
//  ChatServer
//
//  Created by John McKisson on 4/16/08.
//  Copyright 2008 __MyCompanyName__. All rights reserved.
//
package com.presence.chat.commands;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import com.presence.chat.*;

import static com.presence.chat.ANSIColor.*;

public class CMDLog implements Command {

	public String help() {
		return "Shows a log of activity in your current channel";
	}
	
	public String usage() {
		return String.format(ChatServer.USAGE_STRING, "log [<#>|<grep> [<#>]]");
	}


	public boolean execute(ChatClient sender, String[] args) {
		
		//Get log for the current room
		ChatLog roomLog = sender.getRoom().getLog();
		
		if (roomLog.size() == 0) {
			sender.sendChat(String.format("%s chats to you, 'This room has no messages!'", ChatPrefs.getName()));
			return true;
		}
		
		String strBuf = roomLog.getLog(sender, args);
		
		if (strBuf.length() > 0)
			sender.sendChat(strBuf);
		else
			sender.sendChat(String.format("%s chats to you, 'There are no messages'", ChatPrefs.getName()));
		
		return true;
	}
	
}
