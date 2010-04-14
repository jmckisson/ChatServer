//
//  MudMasterProtocol.java
//  ChatServer
//
//  Created by John McKisson on 3/31/08.
//  Copyright 2008 __MyCompanyName__. All rights reserved.
//
package com.presence.chat.protocol;

import java.nio.ByteBuffer;

import java.nio.channels.SocketChannel;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import com.presence.chat.*;

public class MudMasterProtocol extends ChatProtocol {

	public static final String PREFIX = "CHAT";
	

	public MudMasterProtocol(ChatClient client) {
		super(client);
	}
	
	public String toString() {
		return "MudMaster";
	}

}
