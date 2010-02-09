//
//  CMDPassword.java
//  ChatServer
//
//  Created by John McKisson on 4/14/08.
//  Copyright 2008 __MyCompanyName__. All rights reserved.
//
package com.presence.chat.commands;

import com.presence.chat.*;

public class CMDPassword implements Command {

	public String help() {
		return "Change your password";
	}
	
	public String usage() {
		return String.format(ChatServer.USAGE_STRING, "chpw <password>");
	}


	public boolean execute(ChatClient sender, String[] args) {
	
		if (args.length < 2) {
			sender.sendChat(usage());
			return true;
		}
		
		//Ok set the new password
		sender.getAccount().setPassword(JCrypt.crypt("", args[1]));
		
		//Save
		AccountManager.saveAccounts();
		
		sender.sendChat(String.format("%s chats to you, 'Ok, your new password has been set'", ChatPrefs.getName()));

		return true;
	}

}
