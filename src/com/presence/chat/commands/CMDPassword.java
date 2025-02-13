//
//  CMDPassword.java
//  ChatServer
//
//  Created by John McKisson on 4/14/08.
//  Copyright 2008 __MyCompanyName__. All rights reserved.
//
package com.presence.chat.commands;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.presence.chat.*;

public class CMDPassword implements Command {

	public String help() {
		return "Change your password";
	}
	
	public String usage() {
		return String.format(ChatServer.USAGE_STRING, "chpw <password>");
	}


	public boolean execute(ChatClient sender, String[] args) {
	
		if (args.length < 2) {
			sender.serverChat(usage());
			return true;
		}
		
		//Ok set the new password
		//sender.getAccount().setPassword(JCrypt.crypt("", args[1]));

		String bcryptHashString = BCrypt.withDefaults().hashToString(12, args[1].toCharArray());

		BCrypt.Result result = BCrypt.verifyer().verify(args[1].toCharArray(), bcryptHashString);

		if (result.verified) {
			sender.getAccount().setPassword(bcryptHashString);

			//Save
			AccountManager.saveAccounts();

			sender.serverChat("Ok, your new password has been set");
		} else {
			sender.serverChat("Could not verify hash of password");
		}

		return true;
	}

}
