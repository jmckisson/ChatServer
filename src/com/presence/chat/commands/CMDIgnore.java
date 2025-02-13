//
//  CMDGag.java
//  ChatServer
//
//  Created by John McKisson on 4/23/08.
//  Copyright 2008 __MyCompanyName__. All rights reserved.
//
package com.presence.chat.commands;

import java.util.List;
import java.util.Vector;

import com.presence.chat.*;

public class CMDIgnore implements Command {

	public String help() {
		return "Ignore all input from a person";
	}


	public String usage() {
		return String.format(ChatServer.USAGE_STRING, "ignore [person]");
	}


	public boolean execute(ChatClient sender, String[] args) {
		StringBuilder strBuf = new StringBuilder();
	
		if (args.length == 1) {
			//Just show a list of current gags and we're done
			
			List<String> gags = sender.getAccount().gagList();
			
			if (gags == null || gags.size() == 0) {
				strBuf.append("You are not ignoring anyone.");
				
			} else {
				strBuf.append("You are ignoring:\n");
				
				for (String str : gags)
					strBuf.append(str + '\n');

			}
			
		} else {
			//Gag or ungag someone
			List<String> gags = sender.getAccount().gagList();
			
			ChatAccount ac = sender.getAccount();
			
			String[] gagArgs = args[1].split(" ");
			
			for (String str : gagArgs) {
			
				if (str.toLowerCase().equals(sender.getAccount().getName().toLowerCase())) {
					strBuf.append("You cannot ignore yourself!");
					continue;
				}
			
				if (ac.hasGagged(str)) {
					ac.removeGag(str);
					
					strBuf.append(String.format("You unignore %s\n", str));
					
				} else {
					ac.addGag(str);
					
					strBuf.append(String.format("You ignore %s\n", str));
				}
			}
			
		}
		
		sender.sendChat(strBuf.toString());
		
		return true;
	}
}
