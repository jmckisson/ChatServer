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
		return "fjoin <person> <room> <password>";
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
			sender.serverChat(String.format("%s is already in %s", joinArgs[0], joinArgs[1]));
			return true;
		}
		
		String[] roomArgs = new String[] {joinArgs[1], joinArgs[2], (joinArgs.length >= 3 ? joinArgs[2] : null)};

		//Ok now make sure that room exists
		ChatRoom targetRoom = ChatServer.getRoom(roomArgs);
		
		//Join new room
		boolean moved = client.toRoom(targetRoom, (roomArgs.length > 1 ? roomArgs[1] : null), true, true);
		
		if (!moved)
			sender.serverChat(joinArgs[0] + " was unable to join that room");
		
		return true;
	}
}
