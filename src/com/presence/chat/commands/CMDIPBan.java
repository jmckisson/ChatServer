//
//  CMDIPBan.java
//  ChatServer
//
//  Created by John McKisson on 12/5/10.
//  Copyright 2010 Jefferson Lab. All rights reserved.
//
package com.presence.chat.commands;

import com.presence.chat.*;

import java.util.*;

public class CMDIPBan implements Command {
	public String help() {
		return "Ban or Unban an IP";
	}
	
	public String usage() {
		return String.format(ChatServer.USAGE_STRING, "ipban <list|unban IPADDR|IPADDR>");
	}

	public boolean execute(ChatClient sender, String[] args) {
		
		if (args.length < 1) {
			sender.sendChat(usage());
			return true;
		}
		
		String banArgs[] = args[1].split(" ");
		
		if (banArgs.length < 1) {
			sender.sendChat(usage());
			return true;
		}
		
				
		if (banArgs[0].equalsIgnoreCase("list")) {
		
			StringBuilder strBuf = new StringBuilder("Banned IPS:");
			
			for (String str : ChatServer.banList()) {
				strBuf.append("\n" + str);
			}
			
			sender.sendChat(strBuf.toString());
			
		} else if (banArgs[0].equalsIgnoreCase("unban")) {
		
			if (banArgs.length != 2) {
				sender.sendChat(usage());
			} else {
		
				if (ChatServer.banList().contains(banArgs[1])) {
					ChatServer.removeBan(banArgs[1]);
					sender.sendChat("Ok, " + banArgs[1] + " is now unbanned.");
				} else {
					sender.sendChat(banArgs[1] + " is not banned.");
				}
				
			}
		
		} else {
			//IP Address was specified
			//Maybe make sure its an actual ip address first!
			if (!ChatServer.banList().contains(banArgs[0])) {
				ChatServer.addBan(banArgs[0]);
				sender.sendChat("Ok, " + banArgs[0] + " is now banned.");
			} else {
				sender.sendChat(banArgs[0] + " is already banned.");
			}
			
		}
		
		return true;
	}

}
