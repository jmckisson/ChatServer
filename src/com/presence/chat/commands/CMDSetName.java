//
//  CMDSetName.java
//  ChatServer
//
//  Created by John McKisson on 11/11/10.
//  Copyright 2010 Jefferson Lab. All rights reserved.
//
package com.presence.chat.commands;

import java.util.logging.*;

import com.presence.chat.*;

import static com.presence.chat.ANSIColor.*;

public class CMDSetName implements Command {
	public String help() {
		return "Set the server's chat name";
	}

	public String usage() {
		return String.format(ChatServer.USAGE_STRING, "setname <name>");
	}


	public boolean execute(ChatClient sender, String[] args) {
		
		if (args.length < 2) {
			sender.sendChat(usage());
			return true;
		}
		
		String oldName = ChatPrefs.getName();
		
		if (args[1].compareTo(oldName) == 0) {
			sender.sendChat("That is already my name!");
			return true;
		}
		
		ChatPrefs.setName(args[1]);
		
		for (ChatClient cl : ChatServer.getClients()) {
			cl.sendNameChange(args[1]);
		}

		return true;
	}

}