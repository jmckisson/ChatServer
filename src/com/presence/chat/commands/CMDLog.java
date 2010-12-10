//
//  CMDLog.java
//  ChatServer
//
//  Created by John McKisson on 4/16/08.
//  Copyright 2008 __MyCompanyName__. All rights reserved.
//
package com.presence.chat.commands;

import java.util.logging.*;

import com.presence.chat.*;
import com.presence.chat.log.*;

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
		
		String strBuf = roomLog.getHistory(sender, args);
		
		sender.sendChat(strBuf);
	
		return true;
	}
	
}
