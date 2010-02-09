//
//  CMDSilent.java
//  ChatServer
//
//  Created by John McKisson on 5/7/08.
//  Copyright 2008 __MyCompanyName__. All rights reserved.
//
package com.presence.chat.commands;

import java.util.logging.*;

import com.presence.chat.*;

import static com.presence.chat.ANSIColor.*;

public class CMDSilent implements Command {

	public String help() {
		return "Toggle silent mode for your current channel";
	}
	
	public String usage() {
		return "silent";
	}

	public boolean execute(ChatClient sender, String[] args) {
	
		//Get senders current room
		ChatRoom room = sender.getRoom();
		
		boolean silent = room.isSilent();
		
		silent = !silent;
		room.setSilent(silent);
		
		String status = (silent ? "silenced" : "unsilenced");
		
		room.echo(String.format("%s[%s%s%s] %s%s%s has %s%s%s the room!",
			RED, WHT, ChatPrefs.getName(), RED, WHT, sender.getName(), RED, GRN, status, RED), sender);
			
		Logger.getLogger("global").info(String.format("%s has %s room %s", sender.getName(), status, room.getName()));
		
		return true;
	}
}
