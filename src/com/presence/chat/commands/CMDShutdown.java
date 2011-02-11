//
//  CMDShutdown.java
//  ChatServer
//
//  Created by John McKisson on 4/30/08.
//  Copyright 2008 Jefferson Lab. All rights reserved.
//
package com.presence.chat.commands;

import java.awt.event.*;
import java.io.File;
import java.util.logging.*;
import javax.swing.Timer;

import com.presence.chat.*;

import static com.presence.chat.ANSIColor.*;

public class CMDShutdown implements Command {
	String typeStr;

	public String help() {
		return "Shutdown or Restart the server";
	}
	

	public String usage() {
		return String.format(ChatServer.USAGE_STRING, "shutdown [time | stop]");
	}


	public boolean execute(ChatClient sender, String[] args) {
		int seconds = 60;
		
		if (args.length > 1) {
			try {
				seconds = Integer.parseInt(args[1]);
			} catch (NumberFormatException e) {
				
				if (args[1].equalsIgnoreCase("stop")) {
					
					ChatServer.abortShutdown();
						
					return true;
				}
			}
			
			typeStr = "Restart";
		
			//Remove .killscript file
			File killscript = new File(".killscript");
			if (killscript.exists()) {
				Logger.getLogger("global").info("Removing killscript file");
				killscript.delete();
			}

		} else {
			typeStr = "Shutdown";
			
			//Remove .killscript file
			File killscript = new File(".killscript");
			if (!killscript.exists()) {
				Logger.getLogger("global").info("Failed to create killscript file");
			}
		}
		
			
		float mins = seconds / 60.0f;
			
		ChatServer.echo(String.format("%s[%s%s%s] Server will %s%s%s in %s%.1f%s minutes",
			RED, WHT, ChatPrefs.getName(), RED, YEL, typeStr, RED, GRN, mins, RED));
			
		ChatServer.shutdown(seconds);	
	
		return true;
	}
}
