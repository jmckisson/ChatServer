//
//  CMDPlugins.java
//  ChatServer
//
//  Created by John McKisson on 4/18/08.
//  Copyright 2008 John McKisson. All rights reserved.
//
package com.presence.chat.commands;

import java.util.*;

import com.presence.chat.*;
import com.presence.chat.plugin.PluginManager;

public class CMDPlugins implements Command {
	/**
	 * Show a paragraph or so about what this command does
	 */
	public String help() {
		return "Shows a list of installed plugins";
	}
	
	/**
	 * Show a one liner explaining the syntax of the command.
	 */
	public String usage() {
		return String.format(ChatServer.USAGE_STRING, "plugins");
	}

	/**
	 */
	public boolean execute(ChatClient sender, String[] args) {
	
		Hashtable<String, String> plugins = PluginManager.getPlugins();
		
		if (plugins.size() == 0) {
			sender.sendChat("There are no plugins installed.");
			return true;
		}
	
		StringBuilder strBuf = new StringBuilder("Installed Plugins:\n");
		
		Enumeration<String> en = plugins.keys();
		
		while (en.hasMoreElements()) {
			strBuf.append(String.format("%s\n", en.nextElement()));
		}
		
		sender.sendChat(strBuf.toString());
	
		return true;
	}
}
