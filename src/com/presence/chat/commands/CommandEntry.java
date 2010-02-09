//
//  CommandEntry.java
//  ChatServer
//
//  Created by John McKisson on 4/18/08.
//  Copyright 2008 __MyCompanyName__. All rights reserved.
//
package com.presence.chat.commands;

public class CommandEntry {
	public int level;
	public Command cmd;
	
	public CommandEntry(Command cmd, int level) {
		this.cmd = cmd;
		
		this.level = level;
	}
}
