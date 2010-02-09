//
//  Command.java
//  ChatServer
//
//  Created by John McKisson on 4/2/08.
//  Copyright 2008 __MyCompanyName__. All rights reserved.
//
package com.presence.chat.commands;

import com.presence.chat.*;

public interface Command {

	/**
	 * Show a paragraph or so about what this command does
	 */
	public String help();
	
	/**
	 * Show a one liner explaining the syntax of the command.
	 */
	public String usage();

	/**
	 * @param sender Client sending the command
	 * @param args Command arguments, args[0] is the command sent, args[1] is the remainder of the string, if any.
	 */
	public boolean execute(ChatClient sender, String[] args);
}
