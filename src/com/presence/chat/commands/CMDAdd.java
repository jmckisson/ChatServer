//
//  CMDAdd.java
//  ChatServer
//
//  Created by John McKisson on 4/14/08.
//  Copyright 2008 __MyCompanyName__. All rights reserved.
//
package com.presence.chat.commands;

import java.lang.Integer;
import java.util.logging.*;

import com.presence.chat.*;

import static com.presence.chat.ANSIColor.*;

public class CMDAdd implements Command {

	public String help() {
		return "Add a new user account";
	}
	
	public String usage() {
		return String.format(ChatServer.USAGE_STRING, "add <name> <password> [lvl]");
	}

	public boolean execute(ChatClient sender, String[] args) {
			
		//args[1] is the argument after the actual command
		
		if (args.length < 2) {
			sender.serverChat(usage());
			return true;
		}
			
		String[] addArgs = args[1].split(" ");
	
		if (addArgs.length < 2) {
			sender.serverChat(usage());
			return true;
		}
		
		String accountName = addArgs[0].toLowerCase();
		
		//Check if account already exists
		ChatAccount ac = AccountManager.getAccount(accountName);
		
		if (ac != null) {
			sender.sendChat(String.format("Account for %s already exists!", accountName));
			return true;
		}
		
		//Ok account doesnt exist, create it
		ac = new ChatAccount(accountName, JCrypt.crypt("", addArgs[1]));
	
		//Default level is 0 unless otherwise specified
		int level = 0;
		if (addArgs.length == 3) {
			try {
				int l = Integer.parseInt(addArgs[2]);
				if (l >= 0 && l <= 6)
					level = l;
			} catch (NumberFormatException e) {}
		}
		
		ac.setLevel(level);
		
		//Add account to AccountManager
		if (AccountManager.addAccount(ac)) {	
			
			//Save
			AccountManager.saveAccounts();
			
			ChatServer.echo(String.format("%s[%s%s%s] Account %s%s%s(%s%d%s)%s added by %s%s%s",
				RED, WHT, ChatPrefs.getName(), RED, WHT, accountName, YEL, WHT, level, YEL, RED, WHT, sender.getName(), RED));
			
			//sender.sendChat(String.format("Ok, you just added a level %d account for %s", level, accountName));
			
			Logger.getLogger("global").info(String.format("%s added account for %s (level %d)", sender.getName(), accountName, level));
			
		} else {
			sender.sendChat(String.format("Oops, there was an error adding an account for %s", accountName));
			
			Logger.getLogger("global").info(String.format("Error attempting to add account %s by %s", accountName, sender.getName()));
		}
		
		return true;
	}
}
