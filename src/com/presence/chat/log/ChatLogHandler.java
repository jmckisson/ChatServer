//
//  ChatLogHandler.java
//  ChatServer
//
//  Created by John McKisson on 12/9/10.
//  Copyright 2010 Jefferson Lab. All rights reserved.
//
package com.presence.chat.log;

import java.util.*;
import java.util.logging.*;

import com.presence.chat.ANSIColor;
import com.presence.chat.ChatClient;
import com.presence.chat.ChatPrefs;

public class ChatLogHandler extends Handler {

	int sizeThreshold;
	
	LinkedList<LogRecord> records;

	public ChatLogHandler(int size) {
		super();
		
		sizeThreshold = size;
		
		records = new LinkedList<LogRecord>();
	}
	
	public int size() {
		return records.size();
	}
	
	@Override
	public void publish(LogRecord record) {
	
		//Logger.getLogger("global").info("publishing record in handler: " + this);
	
		//Maintain circular buffer of records
		records.addFirst(record);
		
		if (records.size() > sizeThreshold)
			records.removeLast();
			
		//String logMsg = getFormatter().format(record);
	}
	
	
	public String getFormattedHistory(ChatClient sender, String[] args) {
		if (records.size() == 0)
			return String.format("%s chats to you, 'There are no messages'", ChatPrefs.getName());
	
		int count;
		String grepStr = null;
		
		java.util.logging.Formatter fmt = getFormatter();
		
		//System.out.println("using formatter: " + fmt);
		
		StringBuilder strBuf = new StringBuilder();
		
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
		
			count = Math.min(val, records.size());
		} else
			count = Math.min(20, records.size());
		
		//System.out.println("count: " + count + "  grepStr: " + grepStr);
		
		boolean compact = sender.getAccount().isCompact();
		
		Iterator<LogRecord> it = records.iterator();
		
		while (it.hasNext() && count > 0) {
			LogRecord rec = it.next();
			
			String recString = fmt.format(rec);
			
			if (grepStr == null || (grepStr != null && ANSIColor.strip(recString).toLowerCase().contains(grepStr))) {
			
				strBuf.insert(0, recString + (compact ? "" : "\n"));
				
				--count;
			} else
				continue;
		}
		
		return strBuf.toString();
	}


	
	@Override
	public void close() {
	}
	
	
	@Override
	public void flush() {
	}

}
