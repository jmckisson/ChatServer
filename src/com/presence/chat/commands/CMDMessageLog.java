//
//  CMDMessageLog.java
//  ChatServer
//
//  Created by John McKisson on 11/11/10.
//  Copyright 2008 __MyCompanyName__. All rights reserved.
//
package com.presence.chat.commands;

import com.presence.chat.*;
import com.presence.chat.log.*;

public class CMDMessageLog extends CMDLog implements Command {

	public String help() {
		return "Shows your private message log";
	}
	
	public String usage() {
		return String.format(ChatServer.USAGE_STRING, "plog [<#>|<grep> [<#>]]");
	}


	public boolean execute(ChatClient sender, String[] args) {
		
		//Get log for the current room
		MessageLog messageLog = sender.getMessageLog();
		
		String strBuf = messageLog.getHistory(sender, args);
		
		if (strBuf != null) {
			if (strBuf.length() > 0)
				sender.sendChat(strBuf);
			else
				sender.serverChat("There are no messages");
		}

		return true;
	}
	
}
