//
//  CMDChatAll.java
//  ChatServer
//
//  Created by John McKisson on 5/13/08.
//  Copyright 2008 __MyCompanyName__. All rights reserved.
//
package com.presence.chat.commands;

import com.presence.chat.*;

public class CMDChatAll implements Command {
	/**
	 * Show a paragraph or so about what this command does
	 */
	public String help() {
		return "Send a chat to your current channel";
	}
	
	/**
	 * Show a one liner explaining the syntax of the command.
	 */
	public String usage() {
		return "ca <msg>";
	}

	/**
	 */
	public boolean execute(ChatClient sender, String[] args) {
		if (args.length < 2) {
			sender.serverChat(usage());
			return true;
		}
		
		//Ok encapsulate the string
		String msg = String.format("%s chats, '%s'", sender.getName(), args[1]);
		
		sender.sendChat(String.format("You chat, '%s'", args[1]));
		
		sender.forwardToRoom(msg);
		
		return true;
	}
}
