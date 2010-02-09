//
//  CMDLog.java
//  ChatServer
//
//  Created by John McKisson on 4/16/08.
//  Copyright 2008 __MyCompanyName__. All rights reserved.
//
package com.presence.chat.commands;

import java.text.DateFormat;
import java.util.ListIterator;
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
		return String.format(ChatServer.USAGE_STRING, "log [<#>]");
	}


	public boolean execute(ChatClient sender, String[] args) {
		
		//Get log for the current room
		ChatLog roomLog = sender.getRoom().getLog();
		
		StringBuilder strBuf = new StringBuilder();
		
		int length;
		String grepStr = null;
		
		if (args.length == 2) {
			int val;
			try {
				val = Integer.parseInt(args[1]);
			} catch (NumberFormatException e) {
				val = 20;
				
				//Treat args[1] as a search string
				grepStr = args[1].toLowerCase();
			}
		
			length = Math.min(val, roomLog.size());
		} else
			length = Math.min(20, roomLog.size());
		
		
		//Matcher matcher = Pattern.compile(grepStr).matcher("");
		//System.out.printf("'%s'\n", matcher.pattern());
		
		ListIterator<ChatLogEntry> it = roomLog.entryIterator(length);
		
		while (it.hasPrevious()) {
			ChatLogEntry entry = it.previous();
			
			String msg = entry.getMessage();
			
			if (grepStr == null || (grepStr != null && msg.toLowerCase().contains(grepStr))) {
			
			//if (grepStr != null) {
				//matcher.reset(msg);
			
				//if (matcher.matches())
				//if (msg.toLowerCase().contains(grepStr))
					strBuf.append(String.format(TEMPLATE, df.format(entry.getDate()), msg));
				//else
				//	continue;
			
			} else
				continue;
				//strBuf.append(String.format(TEMPLATE, df.format(entry.getDate()), msg));
			
			//Break into multiple chats if the message gets long
			if (strBuf.length() >= 4000) {
				sender.sendChat(strBuf.toString());
				strBuf.setLength(0);
			}
		}
		
		if (strBuf.length() > 0)
			sender.sendChat(strBuf.toString());
		
		return true;
	}
}
