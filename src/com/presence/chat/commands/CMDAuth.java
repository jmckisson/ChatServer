//
//  CMDAuth.java
//  ChatServer
//
//  Created by John McKisson on 4/15/08.
//  Copyright 2008 __MyCompanyName__. All rights reserved.
//
package com.presence.chat.commands;

import java.awt.event.*;
import java.util.logging.*;
import javax.swing.Timer;

import com.presence.chat.*;

import static com.presence.chat.ANSIColor.*;

/**
 * Disables the authentication requirement for 2 minutes.
 * If called while autentication is already disabled, re-enable it.
 */
public class CMDAuth implements Command {
	
	transient static String TEMPLATE = String.format("%s[%s%s%s] Authentication %%s by %s%%s%s", RED, WHT, ChatPrefs.getName(), RED, WHT, RED);
	
	Timer authTimer;
	
	public String help() {
		return "Disable authentication for 2 minutes";
	}
	
	
	public String usage() {
		return String.format(ChatServer.USAGE_STRING, "auth");
	}

	public boolean execute(ChatClient sender, String[] args) {
	
		//Access restriction
		if (sender.getAccount().getLevel() < 3)
			return false;
			
		if (AccountManager.authEnabled()) {
			AccountManager.setAuth(false);
			
			authTimer = new Timer(1000 * 60 * 2, new ActionListener() {
				public void actionPerformed(ActionEvent e) {
				
					if (AccountManager.authEnabled())
						return;

					AccountManager.setAuth(true);
					
					//Send notification
					ChatServer.echo(String.format(TEMPLATE, "enabled", "System"));
				}
			});
			
			authTimer.setRepeats(false);
			authTimer.start();

			//Send notification
			ChatServer.echo(String.format(TEMPLATE, "disabled (2 mins)", sender.getName()));
			
		} else {
			AccountManager.setAuth(true);
			
			if (authTimer != null) {
				authTimer.stop();
				authTimer = null;
			}
				
			//Send notification
			ChatServer.echo(String.format(TEMPLATE, "enabled", sender.getName()));
		}
		
		return true;
	}
}
