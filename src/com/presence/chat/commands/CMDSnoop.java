//
//  CMDSnoop.java
//  ChatServer
//
//  Created by John McKisson on 11/12/10.
//  Copyright 2010 Jefferson Lab. All rights reserved.
//
package com.presence.chat.commands;

import java.util.logging.*;

import com.presence.chat.*;

public class CMDSnoop implements Command {
	public String help() {
		return "Snoop someone";
	}
	
	public String usage() {
		return String.format(ChatServer.USAGE_STRING, "snoop start|stop <person>");
	}

	public boolean execute(ChatClient sender, String[] args) {
			
		//args[1] is the argument after the actual command
		
		if (args.length < 2) {
			sender.sendChat(usage());
			return true;
		}
			
		String[] snoopArgs = args[1].split(" ");
	
		if (snoopArgs.length < 2) {
			sender.sendChat(usage());
			return true;
		}
		
		String command = snoopArgs[0].toLowerCase();
				
		ChatClient target = ChatServer.getClientByName(snoopArgs[1]);
		
		if (target == null) {
			sender.sendChat("I could not locate anyone by that name");
		}
		
		if (command.compareTo("start") == 0) {
			target.startSnoop(sender);
			
		} else if (command.compareTo("stop") == 0) {
			target.stopSnoop(sender);
		//} else if (command.compareTo("enable") == 0) {
			
		} else
			sender.sendChat(usage());
		
		return true;
	}

}
