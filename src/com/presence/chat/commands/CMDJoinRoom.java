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
		
		String[] roomArgs = args[1].split(" ", 3);
		
		//Now check if the sender is already in the specified room
		if (sender.getRoom().getName().equalsIgnoreCase(args[1])) {
			sender.serverChat(String.format("You are already in %s", roomArgs[0]));
			return true;
		}
		
		//Find or create the room
		ChatRoom targetRoom = ChatServer.getRoom(roomArgs);
		
		//Join new room
		boolean moved = sender.toRoom(targetRoom, (roomArgs.length > 1 ? roomArgs[1] : null), true, false);
		
		if (!moved)
			sender.serverChat("You were unable to join that room");
		
		return true;
	}
}
/*
		if (args.length < 2) {
			sender.sendChat(usage());
			return true;
		}
		
		String[] roomArgs = args[1].split(" ", 3);
		
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
		
		int minLevel = 0;
		
		//Check if a password has been supplied
		String password = null;
		if (roomArgs.length == 2) {
			//roomArgs[1] is either the password or minLevel
			try {
				minLevel = Integer.parseInt(roomArgs[1]);
			} catch (NumberFormatException e) {
				//Wasnt a number, so its the password
				password = roomArgs[1];
			}
		} else if (roomArgs.length == 3) {
			//Password is second argument
			password = roomArgs[1];
			
			//MinLevel is 3rd argument
			try {
				minLevel = Integer.parseInt(roomArgs[2]);
			} catch (NumberFormatException e) {}
		}
		
		if (minLevel > sender.getAccount().getLevel())
			minLevel = sender.getAccount().getLevel();
		else if (minLevel < 0)
			minLevel = 0;
			
		//Finally we can create the room
		newRoom = new ChatRoom(roomArgs[0], password, minLevel);
			
		//newRoom.setPassword(password);
		//newRoom.setMinLevel(minLevel);
		
		//Add this room to the room list
		ChatServer.getRooms().put(roomArgs[0].toLowerCase(), newRoom);
		
		//Join new room
		sender.toRoom(newRoom, password, true, false);
*/
