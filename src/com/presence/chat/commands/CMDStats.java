//
//  CMDStats.java
//  ChatServer
//
//  Created by John McKisson on 5/13/08.
//  Copyright 2008 __MyCompanyName__. All rights reserved.
//
package com.presence.chat.commands;

import com.presence.chat.*;

public class CMDStats implements Command {
	/**
	 * Show a paragraph or so about what this command does
	 */
	public String help() {
		return "Show Server Statistics";
	}
	
	/**
	 * Show a one liner explaining the syntax of the command.
	 */
	public String usage() {
		return "stats";
	}

	/**
	 * @param sender Client sending the command
	 * @param args Command arguments, args[0] is the command sent, args[1] is the remainder of the string, if any.
	 */
	public boolean execute(ChatClient sender, String[] args) {
		StringBuilder strBuf = new StringBuilder();
		
		ServerStats stats = ChatServer.getStats();
		
		float uptimeHrs = (System.currentTimeMillis() - stats.startTime) / 1000.0f / 60.0f / 60.0f;
		
		strBuf.append(String.format("Uptime: %f hrs\n" +
									"Commands: %d\n" + 
									"Connects: %d\n" + 
									"Chats: %d\n" + 
									"PChats: %d\n" + 
									"Rooms: %d\n" + 
									"Name Changes: %d\n" + 
									"Kicks: %d",
									uptimeHrs, stats.cmds, stats.connects, stats.chats, stats.pchats, stats.rooms, stats.namechanges, stats.kicks));
		

		
		sender.sendChat(strBuf.toString());
		
		return true;
	}
}
