//
//  CMDSetLevel.java
//  ChatServer
//
//  Created by John McKisson on 5/7/08.
//  Copyright 2008 __MyCompanyName__. All rights reserved.
//
package com.presence.chat.commands;

import java.util.logging.*;

import com.presence.chat.*;

import static com.presence.chat.ANSIColor.*;

public class CMDSetLevel implements Command {

	public String help() {
		return "Set another users permission level";
	}
	

	public String usage() {
		return String.format(ChatServer.USAGE_STRING, "setlvl <account> <#>");
	}


	public boolean execute(ChatClient sender, String[] args) {
		
		if (args.length < 2) {
			sender.sendChat(usage());
			return true;
		}
		
		String[] lvlArgs = args[1].split(" ");
	
		if (lvlArgs.length < 2) {
			sender.sendChat(usage());
			return true;
		}
		
		//Find an account by the specified name
		ChatAccount ac = AccountManager.getAccount(lvlArgs[0]);
		
		if (ac == null) {
			sender.sendChat(String.format("There is no account by the name of %s.", lvlArgs[0]));
			return true;
		}
		
		int newLvl = -1;
		
		try {
			newLvl = Integer.parseInt(lvlArgs[1]);
		} catch (NumberFormatException e) {}
		
		int senderLvl = sender.getAccount().getLevel();
		
		//Make sure we're not trying to set the lvl of a user with higher level
		if (newLvl < 0 || newLvl > 5 || ac.getLevel() > senderLvl || newLvl >= senderLvl) {
			sender.sendChat("You cannot do that!");
			Logger.getLogger("global").info(String.format("%s(%d) attempted to change level for %s(%d) to %d",
				sender.getName(), senderLvl, lvlArgs[0], ac.getLevel(), newLvl));
			return true;
		}
			
		//We're good to go
		ac.setLevel(newLvl);
		
		AccountManager.saveAccounts();
	
		ChatServer.echo(String.format("%s[%s%s%s] %s%s%s just changed %s%s's%s level to %s%d%s.",
			RED, WHT, ChatPrefs.getName(), RED, WHT, sender.getName(), RED, WHT, ac.getName(), RED, WHT, newLvl, RED));

		return true;
	}
}