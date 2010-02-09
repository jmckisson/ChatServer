//
//  CMDDelete.java
//  ChatServer
//
//  Created by John McKisson on 4/15/08.
//  Copyright 2008 __MyCompanyName__. All rights reserved.
//
package com.presence.chat.commands;

import java.util.logging.*;

import com.presence.chat.*;

import static com.presence.chat.ANSIColor.*;

public class CMDDelete implements Command {
	
	public String help() {
		return "Delete a user account";
	}
	
	public String usage() {
		return String.format(ChatServer.USAGE_STRING, "del <name>");
	}

	public boolean execute(ChatClient sender, String[] args) {
	
		if (args.length < 2) {
			sender.sendChat(usage());
			return true;
		}
		
		//Check if account exists
		ChatAccount ac = AccountManager.getAccount(args[1]);
		
		if (ac == null) {
			sender.sendChat(String.format("Account for %s doesn't exist!", args[1]));
			return true;
		}
		
		//Make sure we're privileged enough to remove this person
		if (sender.getAccount().getLevel() < ac.getLevel()) {
			sender.sendChat("You cannot remove someone who has a higher level!");
			
			Logger.getLogger("global").info(String.format("%s attemped to delete account for %s", sender.getName(), args[1]));
			
			return true;
		}
		
		AccountManager.removeAccount(args[1]);
		
		AccountManager.saveAccounts();
		
		ChatServer.echo(String.format("%s[%s%s%s] Account %s%s%s deleted by %s%s%s",
			RED, WHT, ChatPrefs.getName(), RED, WHT, args[1], RED, WHT, sender.getName(), RED));
		
		//sender.sendChat(String.format("Ok, you just deleted %s's account", args[1]));
		
		Logger.getLogger("global").info(String.format("%s deleted account for %s", sender.getName(), args[1]));
		
		return true;
	}
}
