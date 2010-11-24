//
//  CMDShutdown.java
//  ChatServer
//
//  Created by John McKisson on 4/30/08.
//  Copyright 2008 __MyCompanyName__. All rights reserved.
//
package com.presence.chat.commands;

import java.awt.event.*;
import java.io.File;
import java.util.logging.*;
import javax.swing.Timer;

import com.presence.chat.*;

import static com.presence.chat.ANSIColor.*;

public class CMDShutdown implements Command {
	//Timer shutdownTimer = null;
	String typeStr;

	public String help() {
		return "Shutdown or Restart the server";
	}
	

	public String usage() {
		return String.format(ChatServer.USAGE_STRING, "shutdown [time]");
	}


	public boolean execute(ChatClient sender, String[] args) {
		int seconds = 60;
		
		if (args.length > 1) {
			try {
				seconds = Integer.parseInt(args[1]);
			} catch (NumberFormatException e) {
				/*
				if (args[1].equalsIgnoreCase("stop")) {
					if (shutdownTimer != null) {
						shutdownTimer.stop();
						shutdownTimer = null;
						
						ChatServer.echo(String.format("%s[%s%s%s] Server %s%s%s aborted",
							RED, WHT, ChatPrefs.getName(), RED, YEL, typeStr, RED));
					}
					
				}
				*/
			}
		}
		
		typeStr = "Restart";
		
		//Remove .killscript file
		File killscript = new File(".killscript");
		if (killscript.exists()) {
			Logger.getLogger("global").info("Removing killscript file");
			killscript.delete();
		}
	
		float mins = seconds / 60.0f;
			
		ChatServer.echo(String.format("%s[%s%s%s] Server will %s%s%s in %s%.1f%s minutes",
			RED, WHT, ChatPrefs.getName(), RED, YEL, typeStr, RED, GRN, mins, RED));
			
		ChatServer.shutdown(seconds);	
	
		return true;
	}
	
	/*
	void setTimer(int seconds) {
		if (shutdownTimer != null)
			shutdownTimer = null;
			
		shutdownTimer = new Timer(seconds * 1000, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ChatServer.echo(String.format("%s[%s%s%s] Server %s%s%s",
					RED, WHT, ChatPrefs.getName(), RED, YEL, typeStr, RED));
			
				ChatServer.getServer().shutdown();
			}
		});
		
		shutdownTimer.setRepeats(false);
		shutdownTimer.start();
	}
	*/
}
