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
import java.util.Iterator;
import java.util.ListIterator;
import java.util.logging.*;

import static com.presence.chat.ANSIColor.*;

public class ChatLog {
	
	static final String TEMPLATE = String.format("%s%s[%s%%-12s%s] %s%%s\n", BLD, WHT, CYN, WHT, RED);
	static final DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM);
	
	int maxSize;
	String name;
	
	static final int MAX_SIZE = 10000;

	LinkedList<ChatLogEntry> entries;
	
	public ChatLog(int maxSize, String name) {
		this(name);
		
		this.maxSize = maxSize;
	}

	public ChatLog(String name) {
		this.name = name;
		entries = new LinkedList<ChatLogEntry>();
		
		maxSize = MAX_SIZE;
	}
	
	/**
	 * Returns ANSI stripped message
	 */
	public String addEntry(String msg) {
		ChatLogEntry entry = new ChatLogEntry(msg);
		
		synchronized (entries) {
			entries.addFirst(entry);
		}
		
		String stripped = entry.getStrippedMessage();
		
		Logger.getLogger("global").info("["+name+"] " + stripped);

		if (entries.size() > maxSize)
			entries.removeLast();
			
		return stripped;
	}
	
	public int size() {
		return entries.size();
	}
	
	public LinkedList<ChatLogEntry> getEntries() {
		return entries;
	}
	
	public ListIterator<ChatLogEntry> entryIterator(int idx) {
		return entries.listIterator(idx);
	}
	
	public String getLog(ChatClient sender, String[] args) {
		StringBuilder strBuf = new StringBuilder();
		
		int count;
		String grepStr = null;
		
		if (args.length == 2) {
			String[] logArgs = args[1].split(" ", 2);
			
			int val = 20;
			
			if (logArgs.length == 2) {
				grepStr = logArgs[0].toLowerCase();
				try {
					val = Integer.parseInt(logArgs[1]);
				} catch (NumberFormatException e) {}
			} else {
				try {
					val = Integer.parseInt(logArgs[0]);
				} catch (NumberFormatException e) {
					grepStr = logArgs[0];
				}
			}
		
			count = Math.min(val, entries.size());
		} else
			count = Math.min(20, entries.size());
		
		synchronized (entries) {
			Iterator<ChatLogEntry> it = entries.iterator();
			
			boolean compact = sender.getAccount().isCompact();
					
			while (it.hasNext() && count > 0) {
				ChatLogEntry entry = it.next();
				
				if (entry == null) {
					Logger.getLogger("global").warning("Entry iterator returned null entry!");
					break;
				}
				
				String msg = entry.getStrippedMessage();
				
				
				if (grepStr == null || (grepStr != null && msg.toLowerCase().contains(grepStr))) {

					strBuf.insert(0, String.format(TEMPLATE + "%s", df.format(entry.getDate()), entry.getMessage(), (compact ? "" : "\n")));
				
					count--;
							
				} else
					continue;
				
			}
		}
		
		return strBuf.toString();
	}

}
