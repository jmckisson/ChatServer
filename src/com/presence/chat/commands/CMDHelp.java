//
//  CMDHelp.java
//  ChatServer
//
//  Created by John McKisson on 4/2/08.
//  Copyright 2008 __MyCompanyName__. All rights reserved.
//
package com.presence.chat.commands;

import java.util.Enumeration;

import com.presence.chat.*;

public class CMDHelp implements Command {

	public boolean execute(ChatClient sender, String[] args) {
	
		if (args.length < 2) {
			
			StringBuilder[] cmds = new StringBuilder[6];
			
			for (int i = 0; i < 6; i++)
				cmds[i] = new StringBuilder(String.format("\nLevel %d:\n", i));

			
			Enumeration<String> keys = ChatServer.getCommands().keys();
			Enumeration<CommandEntry> values = ChatServer.getCommands().elements();
			
			while (keys.hasMoreElements()) {
				String key = keys.nextElement();
				CommandEntry cmdEntry = values.nextElement();
			
				cmds[cmdEntry.level].append(String.format("%10s\t-\t%s\n", key, cmdEntry.cmd.help()));
			}
			
			
			int level = sender.getAccount().getLevel();
			
			StringBuilder strBuf = new StringBuilder(String.format("%s\n", ChatServer.VERSION));
			
			
			for (int i = 0; i < 6 && level >= i; i++)
				strBuf.append(cmds[i]);
			
			
			sender.sendChat(strBuf.toString());
			
			return true;
		}
	
		CommandEntry cmdEntry = ChatServer.getCommand(args[1]);
		
		if (cmdEntry == null) {
			sender.sendChat(String.format("%s chats to you, 'You idiot!  There is no <%s> command!'", ChatPrefs.getName(), args[1]));
			return true;
		}
		
		sender.sendChat(cmdEntry.cmd.help());
		
		return true;
	}
	
	
	public String usage() {
		return String.format(ChatServer.USAGE_STRING, "help <command>");
	}
	
	
	public String help() {
		return "";
	}
}
