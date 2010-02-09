//
//  CMDAccounts.java
//  ChatServer
//
//  Created by John McKisson on 4/16/08.
//  Copyright 2008 __MyCompanyName__. All rights reserved.
//
package com.presence.chat.commands;

import java.text.DateFormat;
import java.util.Vector;

import com.presence.chat.*;

import static com.presence.chat.ANSIColor.*;

public class CMDAccounts implements Command {

	static final String TEMPLATE = String.format("%s[%s%%03d%s][%s%%-12s%s][%s%%-13s%s][%s%%d%s]\n", WHT, YEL, WHT, CYN, WHT, GRN, WHT, CYN, WHT);

	static final DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM);

	public String help() {
		return "List user accounts";
	}
	
	public String usage() {
		return String.format(ChatServer.USAGE_STRING, "accounts");
	}

	public boolean execute(ChatClient sender, String[] args) {
			
		Vector<ChatAccount> accounts = AccountManager.getAccounts();
			
		StringBuilder strBuf = new StringBuilder();
			
		for (ChatAccount account : accounts) {
			String lastOn = (account.lastLogin() != null ? df.format(account.lastLogin()) : "Never");
		
			strBuf.append(String.format(TEMPLATE, accounts.indexOf(account) + 1, lastOn, account.getName(), account.getLevel()));
		}
		
		sender.sendChat(strBuf.toString());
		
		return true;
	}

}
