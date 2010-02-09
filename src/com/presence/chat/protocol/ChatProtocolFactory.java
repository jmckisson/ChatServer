//
//  ChatProtocolFactory.java
//  ChatServer
//
//  Created by John McKisson on 3/31/08.
//  Copyright 2008 __MyCompanyName__. All rights reserved.
//
package com.presence.chat.protocol;

import java.nio.channels.SocketChannel;

import com.presence.chat.*;

public class ChatProtocolFactory {

	public static ChatProtocol initProtocol(ChatClient client, String str) {
		ChatProtocol prot = null;
	
		if (str.contains(":")) {
	
			String[] result = str.split(":");
			
			if (result[0].equals(MudMasterProtocol.PREFIX)) {
			
				prot = new MudMasterProtocol(client);

			} else if (result[0].equals(ZChatProtocol.PREFIX)) {
			
				prot =  new ZChatProtocol(client);
			
			} else {
				System.out.println("ChatProtocolFactory: Unknown Incoming Chat Protocol!");
			}
			
			if (prot != null) {
				prot.processHandshake(result[1]);
			}
			
		} else {
			prot = new TelnetProtocol(client);
			
			prot.processHandshake(str);
		}
		

		
		return prot;
	}

}
