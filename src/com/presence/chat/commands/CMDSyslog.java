//
//  CMDSyslog.java
//  ChatServer
//
//  Created by John McKisson on 12/10/10.
//  Copyright 2010 Jefferson Lab. All rights reserved.
//
package com.presence.chat.commands;

import com.presence.chat.*;
import com.presence.chat.log.*;

public class CMDSyslog implements Command {

	public String help() {
		return "Search the system log";
	}
	
	public String usage() {
		return String.format(ChatServer.USAGE_STRING, "syslog [<#>|<grep> [<#>]]");
	}

	public boolean execute(ChatClient sender, String[] args) {
		
		String strBuf = ChatServer.getSystemLog().getFormattedHistory(sender, args);
		
		sender.sendChat(strBuf);
	
		return true;
	}
}
