//
//  ChatLog.java
//  ChatServer
//
//  Created by John McKisson on 4/16/08.
//  Copyright 2008 __MyCompanyName__. All rights reserved.
//
package com.presence.chat;

import java.text.DateFormat;
import java.util.LinkedList;
import java.util.ListIterator;
//import java.util.logging.*;

public class ChatLog {
	//private static final Logger log = Logger.getLogger("global");
	
	int maxSize;
	
	static final int MAX_SIZE = 10000;

	LinkedList<ChatLogEntry> entries;
	
	public ChatLog(int maxSize) {
		this();
		
		this.maxSize = maxSize;
	}

	public ChatLog() {
		entries = new LinkedList<ChatLogEntry>();
		
		maxSize = MAX_SIZE;
	}
	
	public void addEntry(String msg) {
		entries.addFirst(new ChatLogEntry(msg));

		if (entries.size() > maxSize)
			entries.removeLast();
	}
	
	public int size() {
		return entries.size();
	}
	
	public ListIterator<ChatLogEntry> entryIterator(int idx) {
		return entries.listIterator(idx);
	}

}
