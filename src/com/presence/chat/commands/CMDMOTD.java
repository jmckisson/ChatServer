//
//  CMDMOTD.java
//  ChatServer
//
//  Created by John McKisson on 4/24/08.
//  Copyright 2008 __MyCompanyName__. All rights reserved.
//
package com.presence.chat.commands;

import com.presence.chat.*;

import static com.presence.chat.ANSIColor.*;

public class CMDMOTD implements Command {

	public String help() {
		return "Show/Set the MOTD";
	}
	

	public String usage() {
		return String.format(ChatServer.USAGE_STRING, "motd [msg]");
	}


	public boolean execute(ChatClient sender, String[] args) {
	
		//If no arguments or level < 4 just show the motd
		if (args.length == 1 || sender.getAccount().getLevel() < 4) {
			sender.sendChat(ChatPrefs.getMOTD());
			return true;
		}
		
		String motd = args[1].replace("%n", "\n");
		motd = motd.replace("&k", BLK);
		motd = motd.replace("&r", RED);
		motd = motd.replace("&g", GRN);
		motd = motd.replace("&y", YEL);
		motd = motd.replace("&b", BLU);
		motd = motd.replace("&m", MAG);
		motd = motd.replace("&c", CYN);
		motd = motd.replace("&w", WHT);
		motd = motd.replace("&N", NRM);
		motd = motd.replace("&B", BLD);

		//Otherwise we're ok to set the motd
		ChatPrefs.setMOTD(motd);
		
		//Global
		ChatServer.echo(String.format("%s[%s%s%s] %s%s%s has updated the MOTD:\n%s%s%s",
			RED, WHT, ChatPrefs.getName(), RED, WHT, sender.getName(), RED, YEL, motd, RED));
	
		return true;
	}
}
