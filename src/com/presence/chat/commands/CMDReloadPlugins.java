//
//  CMDReloadPlugins.java
//  ChatServer
//
//  Created by John McKisson on 11/30/10.
//  Copyright 2010 Jefferson Lab. All rights reserved.
//
package com.presence.chat.commands;

import com.presence.chat.*;

public class CMDReloadPlugins implements Command {
	public String help() {
		return "Reload Plugins";
	}
	
	public String usage() {
		return String.format(ChatServer.USAGE_STRING, "reload");
	}

	public boolean execute(ChatClient sender, String[] args) {
		ChatServer.getServer().reloadPlugins();
		
		return true;
	}

}
