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

import com.presence.chat.ChatClient;

public class ChatLogHandler extends Handler {

	int sizeThreshold;
	
	LinkedList<LogRecord> records;

	public ChatLogHandler(int size) {
		super();
		
		sizeThreshold = size;
		
		records = new LinkedList<LogRecord>();
	}
	
	@Override
	public void publish(LogRecord record) {
	
		//Maintain circular buffer of records
		records.addFirst(record);
		
		if (records.size() > sizeThreshold)
			records.removeLast();
			
		String logMsg = getFormatter().format(record);
			
		//for (Iterator<BaseObject> it = World.getAccounts().iterator(); it.hasNext(); ) {
		//	((MudAccount)it.next()).getClient().send(logMsg);
		//}
		
	}
	
	public String getFormattedHistory(ChatClient sender, int count, String grepStr) {
		if (records.size() == 0)
			return "";
			
		java.util.logging.Formatter fmt = getFormatter();
	
		StringBuilder strBuf = new StringBuilder();
		
		count = Math.min(count, records.size());
		
		boolean compact = sender.getAccount().isCompact();
		
		ListIterator<LogRecord> it = records.listIterator();
		
		while (it.hasPrevious() && count > 0) {
			LogRecord rec = it.previous();
			
			String logMsg = fmt.format(rec);
			
			if (grepStr == null || (grepStr != null && logMsg.toLowerCase().contains(grepStr))) {
			
				strBuf.append((compact ? "" : "\n") + logMsg);
				
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
