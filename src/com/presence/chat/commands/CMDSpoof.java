//
//  CMDSpoof.java
//  ChatServer
//
//  Created by John McKisson on 11/24/10.
//  Copyright 2010 Jefferson Lab. All rights reserved.
//
package com.presence.chat.commands;

import com.presence.chat.*;

public class CMDSpoof implements Command {

	public String help() {
		return "Spoof =)";
	}
	
	public String usage() {
		return "spoof <target> <msg>";
	}

	public boolean execute(ChatClient sender, String[] args) {
		if (args.length < 2) {
			sender.sendChat(usage());
			return true;
		}
		
		String[] spoofArgs = args[1].split(" ", 2);
		
		if (spoofArgs.length < 2) {
			return true;
		}
		
		String target = spoofArgs[0].toLowerCase();
		
		if (target.compareTo("all") == 0) {
			ChatServer.echo(ChatServer.makeANSI(spoofArgs[1]));
		
		} else {
			ChatClient cl = ChatServer.getClientByName(spoofArgs[0]);
			
			if (cl == null) {
				sender.sendChat("Noone by that name online");
			} else {
				cl.sendChat(ChatServer.makeANSI(spoofArgs[1]));
			}
		}
		
		return true;
	}
}
