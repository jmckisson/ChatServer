//
//  CMDLog.java
//  ChatServer
//
//  Created by John McKisson on 4/16/08.
//  Copyright 2008 __MyCompanyName__. All rights reserved.
//
package com.presence.chat.commands;

import java.text.DateFormat;
import java.util.Iterator;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import com.presence.chat.*;

import static com.presence.chat.ANSIColor.*;

public class CMDLog implements Command {

	static final String TEMPLATE = String.format("%s%s[%s%%-12s%s] %s%%s\n", BLD, WHT, CYN, WHT, RED);

	static final DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM);

	public String help() {
		return "Shows a log of activity in your current channel";
	}
	
	public String usage() {
		return String.format(ChatServer.USAGE_STRING, "log [<#>|<grep> [<#>]]");
	}


	public boolean execute(ChatClient sender, String[] args) {
		
		//Get log for the current room
		ChatLog roomLog = sender.getRoom().getLog();
		
		String strBuf = getLog(roomLog, sender, args);
		
		if (strBuf.length() > 0)
			sender.sendChat(strBuf);
		
		return true;
	}
	
	protected String getLog(ChatLog theLog, ChatClient sender, String[] args) {
		StringBuilder strBuf = new StringBuilder();
		
		int length;
		String grepStr = null;
		
		if (args.length == 2) {
			String[] logArgs = args[1].split(" ", 2);
			
			int val = 20;
			
			if (logArgs.length == 2)
				grepStr = logArgs[0].toLowerCase();
				
			try { val = Integer.parseInt(logArgs[0]); } catch (NumberFormatException e) {}
		
			length = Math.min(val, theLog.size());
		} else
			length = Math.min(20, theLog.size());
		
		Iterator<ChatLogEntry> it = theLog.getEntries().descendingIterator();
		
		boolean compact = sender.getAccount().isCompact();
				
		while (it.hasNext() && length > 0) {
			ChatLogEntry entry = it.next();
			
			String msg = entry.getStrippedMessage();
			
			//System.out.println("checking for: '" + grepStr + "' in: '" + msg + "'");
			
			if (grepStr == null || (grepStr != null && msg.toLowerCase().contains(grepStr))) {
				strBuf.insert(0, String.format(TEMPLATE, df.format(entry.getDate()), entry.getMessage()) + (compact ? "" : "\n"));
				
				length--;
			
			} else
				continue;
			
			//Break into multiple chats if the message gets long
			if (strBuf.length() >= 4000) {
				sender.sendChat(strBuf.toString());
				strBuf.setLength(0);
			}
		}
		
		return strBuf.toString();
	}
}
