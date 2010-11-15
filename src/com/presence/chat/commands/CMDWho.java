//
//  CMDWho.java
//  ChatServer
//
//  Created by John McKisson on 4/2/08.
//  Copyright 2008 __MyCompanyName__. All rights reserved.
//
package com.presence.chat.commands;

import java.util.*;
import java.util.logging.*;

import com.presence.chat.*;
import com.presence.chat.protocol.*;

import static com.presence.chat.ANSIColor.*;

public class CMDWho implements Command {

	static final String HEADER = String.format( "%s[ %sName           %s][ %sAccount (Lvl)    %s][ %sRoom     %s][ %sAddress       %s][ %sProtocol %s]\n" +
												"%s ----------------  ------------------  ----------  ---------------  ----------\n",
												BLU, WHT, BLU, WHT, BLU, WHT, BLU, WHT, BLU, WHT, BLU, WHT);

	static String TEMPLATE = "  %s%-16s  %-18s  %s%-10s  %s%-15s  %-10s\n";

	public boolean execute(ChatClient sender, String[] args) {
		//Send client a list of all connected clients
			
		StringBuilder strBuf = new StringBuilder(BLD + HEADER);
		
		for (Iterator<ChatClient> it = ChatServer.getClients().iterator() ; it.hasNext() ;) {
			ChatClient c = it.next();
			
			ChatAccount account = c.getAccount();
			if (account == null) {
				//A client can have a null account if it has not yet been authenticated
				Logger.getLogger("global").warning("Client object has null Account, still authenticating?");
				continue;
			}
			
			ChatRoom room = c.getRoom();
			String roomName = "null";
			if (room != null) {
				int lvl = room.getMinLevel();
				String lvlStr = lvl > 0 ? "(" + lvl + ")" : "";
				roomName = String.format("%s%s%s", (room.getPassword() != null ? "*" : ""), lvlStr, room.getName());
			}
			
			ChatProtocol prot = c.getProtocol();
			String protName = (prot != null ? prot.toString() : "null");
			
			strBuf.append(String.format(TEMPLATE, GRN, c.getName(), String.format("%s (%s)", account.getName(), account.getLevel()), CYN, roomName, YEL, c.getAddr(), protName));
		}
		
		sender.sendChat(strBuf.toString());
		
		return true;
	}
	
	
	public String help() {
		return "Lists online players";
	}
	
	public String usage() {
		return String.format(ChatServer.USAGE_STRING, "who");
	}
}
