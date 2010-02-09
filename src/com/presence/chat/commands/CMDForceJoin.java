//
//  CMDForceJoin.java
//  ChatServer
//
//  Created by John McKisson on 5/15/08.
//  Copyright 2008 __MyCompanyName__. All rights reserved.
//
package com.presence.chat.commands;

import com.presence.chat.*;

public class CMDForceJoin implements Command {
	/**
	 * Show a paragraph or so about what this command does
	 */
	public String help() {
		return "Force someone to join a room";
	}
	
	/**
	 * Show a one liner explaining the syntax of the command.
	 */
	public String usage() {
		return "fjoin <person> <room>";
	}

	/**
	 * @param sender Client sending the command
	 * @param args Command arguments, args[0] is the command sent, args[1] is the remainder of the string, if any.
	 */
	public boolean execute(ChatClient sender, String[] args) {
		if (args.length < 2) {
			sender.sendChat(usage());
			return true;
		}
		
		String[] joinArgs = args[1].split(" ");
		
		if (joinArgs.length < 2) {
			sender.sendChat(usage());
			return true;
		}
		
		//Find person by specified name
		ChatClient client = ChatServer.getClientByName(joinArgs[0]);
		
		if (client == null) {
			sender.sendChat("Noone is online with that name.");
			return true;
		}
		

		
		ChatRoom currentRoom = client.getRoom();
		
		//Now check if the sender is already in the specified room
		if (client.getRoom().getName().equalsIgnoreCase(joinArgs[1])) {
			sender.sendChat(String.format("%s chats to you, '%s is already in %s'", ChatPrefs.getName(), joinArgs[0], joinArgs[1]));
			return true;
		}

		//Ok now make sure that room exists
		ChatRoom targetRoom = ChatServer.getRoom(joinArgs[1]);
		
		if (targetRoom == null) {
			sender.sendChat(String.format("%s chats to you, 'Room %s doesn't exist!'", ChatPrefs.getName(), joinArgs[1]));
			return true;
		}
		
		String password = null;
		//Check if a password was supplied
		if (joinArgs.length == 3)
			password = joinArgs[3];
		
		
		//Join new room
		boolean moved = client.toRoom(targetRoom, password, true);
		
		if (!moved)
			sender.sendChat(joinArgs[0] + " was unable to join that room");
		
		return true;
	}
}
