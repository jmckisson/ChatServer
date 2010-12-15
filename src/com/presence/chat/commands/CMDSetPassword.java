//
//  CMDSetPassword.java
//  ChatServer
//
//  Created by John McKisson on 4/23/08.
//  Copyright 2008 __MyCompanyName__. All rights reserved.
//
package com.presence.chat.commands;

import java.util.logging.*;

import com.presence.chat.*;

import static com.presence.chat.ANSIColor.*;

public class CMDSetPassword implements Command {

	public String help() {
		return "Set another users password";
	}
	

	public String usage() {
		return String.format(ChatServer.USAGE_STRING, "setpw <account> <password>");
	}


	public boolean execute(ChatClient sender, String[] args) {
	
		if (args.length < 2) {
			sender.serverChat(usage());
			return true;
		}
		
		String[] pwArgs = args[1].split(" ");
	
		if (pwArgs.length < 2) {
			sender.serverChat(usage());
			return true;
		}
		
		//Find an account by the specified name
		ChatAccount ac = AccountManager.getAccount(pwArgs[0]);
		
		if (ac == null) {
			sender.sendChat(String.format("There is no account by the name of %s.", pwArgs[0]));
			return true;
		}
		
		//Make sure we're not trying to set the pw of a user with higher level
		if (ac.getLevel() > sender.getAccount().getLevel()) {
			sender.sendChat("You cannot do that!");
			Logger.getLogger("global").info(String.format("%s(%d) attempted to change password for %s(%d)",
				sender.getName(), sender.getAccount().getLevel(), pwArgs[0], ac.getLevel()));
			return true;
		}
		
		ac.setPassword(JCrypt.crypt("", pwArgs[1]));
		
		ChatServer.echo(String.format("%s[%s%s%s] %s%s%s just changed the password for %s%s%s.",
			RED, WHT, ChatPrefs.getName(), RED, WHT, sender.getName(), RED, WHT, ac.getName(), RED));
			
		return true;
	}
}
