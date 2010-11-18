//
//  CMDJoinRoom.java
//  ChatServer
//
//  Created by John McKisson on 4/3/08.
//  Copyright 2008 __MyCompanyName__. All rights reserved.
//
package com.presence.chat.commands;

import com.presence.chat.*;

public class CMDJoinRoom implements Command {

	public String help() {
		return "Join an existing room";
	}
	
	public String usage() {
		return String.format(ChatServer.USAGE_STRING, "join <room> [password]");
	}

	public boolean execute(ChatClient sender, String[] args) {
		if (args.length < 2) {
			sender.sendChat(usage());
			return true;
		}
		
		String[] roomArgs = args[1].split(" ", 2);
		
		//Now check if the sender is already in the specified room
		if (sender.getRoom().getName().equalsIgnoreCase(args[1])) {
			sender.sendChat(String.format("%s chats to you, 'You are already in %s'", ChatPrefs.getName(), roomArgs[0]));
			return true;
		}

		//Ok now make sure that room exists
		ChatRoom targetRoom = ChatServer.getRoom(args[1]);
		
		if (targetRoom == null) {
			sender.sendChat(String.format("%s chats to you, 'Room %s doesn't exist!'", ChatPrefs.getName(), roomArgs[0]));
			return true;
		}
		
		String password = null;
		//Check if a password was supplied
		if (roomArgs.length == 2)
			password = args[2];
			
		
		//Join new room
		boolean moved = sender.toRoom(targetRoom, password, true, false);
		
		if (!moved)
			sender.sendChat("You were unable to join that room");
		
		return true;
	}
}
