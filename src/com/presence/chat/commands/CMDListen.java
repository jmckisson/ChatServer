//
//  CMDListen.java
//  ChatServer
//
//  Created by John McKisson on 11/11/10.
//  Copyright 2010 Jefferson Lab. All rights reserved.
//
package com.presence.chat.commands;

import java.util.logging.*;

import com.presence.chat.*;

import static com.presence.chat.ANSIColor.*;

public class CMDListen implements Command {
	public String help() {
		return "Listen to another room";
	}
	
	public String usage() {
		return String.format(ChatServer.USAGE_STRING, "listen <room> [password]");
	}

	public boolean execute(ChatClient sender, String[] args) {
		if (args.length < 2) {
			sender.serverChat(usage());
			return true;
		}
		
		String[] roomArgs = args[1].split(" ", 2);
		
		//Now check if the sender is already in the specified room
		if (sender.getRoom().getName().equalsIgnoreCase(args[1])) {
			sender.serverChat(String.format("You are already in %s", roomArgs[0]));
			return true;
		}

		//Ok now make sure that room exists
		ChatRoom targetRoom = ChatServer.getRoom(args[1]);
		
		if (targetRoom == null) {
			sender.serverChat(String.format("Room %s doesn't exist!", roomArgs[0]));
			return true;
		}
		
		if (targetRoom.getListeners().contains(sender)) {
			//Stop listening to that room
			targetRoom.removeListener(sender, true);
			return true;
		}
		
		String password = null;
		//Check if a password was supplied
		if (roomArgs.length == 2)
			password = args[2];
			
		String roomPass = targetRoom.getPassword();
			
		if (roomPass != null && !roomPass.equals(password)) {
			sender.serverChat(String.format("Incorrect password for Room %s!", roomArgs[0]));
		} else if (targetRoom.getMinLevel() > sender.getAccount().getLevel()) {
			sender.serverChat(String.format("You do not meet the level requirements for Room %s!", roomArgs[0]));
		} else {
			targetRoom.addListener(sender, true);
		}
		
		return true;
	}

}
