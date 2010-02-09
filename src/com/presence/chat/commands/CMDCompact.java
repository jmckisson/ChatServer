//
//  CMDCompact.java
//  ChatServer
//
//  Created by John McKisson on 5/5/08.
//  Copyright 2008 John McKisson. All rights reserved.
//
package com.presence.chat.commands;

import com.presence.chat.*;

public class CMDCompact implements Command {

	public String help() {
		return "Toggle compact mode";
	}
	

	public String usage() {
		return "";
	}


	public boolean execute(ChatClient sender, String[] args) {
		ChatAccount ac = sender.getAccount();
		
		if (ac.isCompact()) {
			ac.setCompact(false);
			
			sender.sendChat("Ok, compact mode is DISABLED");
		} else {
			ac.setCompact(true);
			
			sender.sendChat("Ok, compact mode is ENABLED");
		}
		
		AccountManager.saveAccounts();
		
		return true;
	}
}
