//
//  ChatCommand.java
//  ChatServer
//
//  Created by John McKisson on 1/8/10.
//  Copyright 2010 Jefferson Lab. All rights reserved.
//
package com.presence.chat.protocol;

public enum ChatCommand {
	NAME_CHANGE(1),
	TEXT_ALL(4),
	TEXT_ONE(5),
	VERSION(19),
	PING_REQUEST(26),
	PING_RESPONSE(27),
	SNOOP(30),
	SNOOP_DATA(31),
	SNOOP_COLOR(32),
	END(255);
	
	public static ChatCommand getCommand(byte cmd) {
		if (cmd == 1)
			return NAME_CHANGE;
		else if (cmd == 4)
			return TEXT_ALL;
		else if (cmd == 5)
			return TEXT_ONE;
		else if (cmd == 19)
			return VERSION;
		else if (cmd == 26)
			return PING_REQUEST;
		else if (cmd == 27)
			return PING_RESPONSE;
		else if (cmd == 30)
			return SNOOP;
		else if (cmd == 31)
			return SNOOP_DATA;
		else if (cmd == 32)
			return SNOOP_COLOR;
			
		return END;
	}
	
	private byte cmd;
	
	private ChatCommand(int cmd) {
		this.cmd = (byte)cmd;
	}
	
	public byte commandByte() {
		return cmd;
	}
}
