//
//  CMDCreateRoom.java
//  ChatServer
//
//  Created by John McKisson on 4/2/08.
//  Copyright 2008 __MyCompanyName__. All rights reserved.
//
package com.presence.chat.commands;

import com.presence.chat.*;

public class CMDCreateRoom implements Command {

	public String help() {
		return "Create a room";
	}
	
	public String usage() {
		return String.format(ChatServer.USAGE_STRING, "create <room> [<password>]");
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
		
		//Ok now make sure that room doesnt already exist
		ChatRoom newRoom = ChatServer.getRoom(args[1]);
		
		if (newRoom != null) {
			sender.sendChat(String.format("%s chats to you, 'Room %s already exists!'", ChatPrefs.getName(), roomArgs[0]));
			return true;
		}
		
		//Finally we can create the room
		newRoom = new ChatRoom(roomArgs[0]);
		
		//Check if a password has been supplied
		String password = null;
		if (roomArgs.length == 2)
			password = roomArgs[1];
			
		newRoom.setPassword(password);
		
		//Add this room to the room list
		ChatServer.getRooms().put(roomArgs[0].toLowerCase(), newRoom);
		
		//Join new room
		sender.toRoom(newRoom, password, true);
		
		return true;
	}
}
