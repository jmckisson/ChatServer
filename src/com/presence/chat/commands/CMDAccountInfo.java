//
//  CMDAccountInfo.java
//  ChatServer
//
//  Created by John McKisson on 12/14/10.
//  Copyright 2010 Jefferson Lab. All rights reserved.
//
package com.presence.chat.commands;

import com.presence.chat.*;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import static com.presence.chat.ANSIColor.*;

public class CMDAccountInfo implements Command {

	static final DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM);

	public String help() {
		return "Show user account information";
	}
	
	public String usage() {
		return String.format(ChatServer.USAGE_STRING, "acinfo <name>");
	}

	public boolean execute(ChatClient sender, String[] args) {
	
		if (args.length < 2) {
			sender.serverChat(usage());
			return true;
		}
			
		ChatAccount account = AccountManager.getAccount(args[1]);
		
		if (account == null) {
			sender.serverChat("There is no account by that name");
		}
	
		StringBuilder strBuf = new StringBuilder(BLD);
		
		strBuf.append(String.format("%sAccount Name: %s%s\n", BLU, CYN, account.getName()));
		strBuf.append(String.format("%sLevel: %s%d\n", BLU, CYN, account.getLevel()));
		
		if (ChatServer.checkOnlineAccount(account.getName(), ""))
			strBuf.append(WHT + account.getName() + " is online right now!");
		else {
			Date lastLogin = account.lastLogin();
			strBuf.append(String.format("%s%s last online %s", WHT, account.getName(), (lastLogin != null ? df.format(lastLogin) : "Never!")));
		}
			
		if (sender.getAccount().getLevel() == 5) {
			
			List<String> gags = account.gagList();
			if (gags != null) {
				strBuf.append(BLU + "\nGags: " + CYN);
				for (String s : gags) {
					strBuf.append(s + " ");
				}
			}
			
			List<String> ips = account.getKnownAddresses();
			if (ips != null) {
				strBuf.append(BLU + "\nKnown Addresses:" + CYN);
				for (String s : ips) {
					strBuf.append("\n" + s);
				}
			}
		} 
		
		sender.sendChat(strBuf.toString());
		
		return true;
	}
}
