//
//  ServerStats.java
//  ChatServer
//
//  Created by John McKisson on 5/13/08.
//  Copyright 2008 __MyCompanyName__. All rights reserved.
//
package com.presence.chat;

import java.util.Date;

public class ServerStats {

	public long startTime;
	
	public int connects;
	public int namechanges;
	public int kicks;
	public int chats;
	public int pchats;
	public int rooms;
	public int cmds;
	public int exceptions;
	
	public Date startDate;
	
	public ServerStats() {
		startTime = System.currentTimeMillis();
		
		startDate = new Date(startTime);
		
		connects = 0;
		namechanges = 0;
		kicks = 0;
		chats = 0;
		pchats = 0;
		rooms = 0;
		cmds = 0;
		exceptions = 0;
	}

}
