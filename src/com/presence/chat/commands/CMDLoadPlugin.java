//
//  CMDLoadPlugin.java
//  ChatServer
//
//  Created by John McKisson on 11/30/10.
//  Copyright 2010 Jefferson Lab. All rights reserved.
//
package com.presence.chat.commands;

import com.presence.chat.*;

public class CMDLoadPlugin implements Command {

	public String help() {
		return "Load a Plugin";
	}
	
	public String usage() {
		return String.format(ChatServer.USAGE_STRING, "load <plugin>");
	}

	public boolean execute(ChatClient sender, String[] args) {
		if (args.length < 2) {
			sender.sendChat(usage());
			return true;
		}
		
		String plugins = ChatPrefs.getPref("PLUGINS", "");
		
		String pName = args[1];
		
		if (!plugins.contains(pName)) {
			ChatServer.getServer().loadPlugin(pName);
		}
		
		return true;
	}

}

