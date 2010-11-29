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

	static final String HEADER = String.format( "%s[ %sName             %s][ %sIdle     %s][ %sAccount (Lvl)    %s][ %sRoom     %s][ %sAddress       %s][ %sVersion     %s]\n" +
												"%s ------------------  ----------  ------------------  ----------  ---------------  -------------\n",
												BLU, WHT, BLU, WHT, BLU, WHT, BLU, WHT, BLU, WHT, BLU, WHT, BLU, WHT);

	//static String TEMPLATE = ;

	public boolean execute(ChatClient sender, String[] args) {
		//Send client a list of all connected clients
			
		StringBuilder strBuf = new StringBuilder(BLD + HEADER);
		
		List<ChatClient> clients = ChatServer.getClients();
		
		synchronized (clients) {
		
			long timeNow = System.currentTimeMillis();
			
			for (Iterator<ChatClient> it = clients.iterator() ; it.hasNext() ;) {
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
				
				String version = c.getProtocol().getVersion();
				String verName = (version != null ? version.toString() : "null");
				long last = c.getLastActivity();
				float idle = (timeNow - last) / 60000.0f;
				String idleStr = " ";
								
				if (idle >= 10.0f) {
					if (idle > 90.0f)
						idleStr += String.format("%.1f hr", idle / 60.0f);
					else
						idleStr += String.format("%.1f min", idle);
				}
				
				strBuf.append(String.format("  %s%-18s %s%-12s %s%-18s  %s%-10s  %s%-15s  %-10s\n",
					GRN, c.getName(), RED, idleStr, GRN, String.format("%s (%s)", account.getName(), account.getLevel()), CYN, roomName, YEL, c.getAddr(), verName));
			}
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
