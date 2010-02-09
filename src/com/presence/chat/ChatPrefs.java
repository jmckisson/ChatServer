//
//  ChatPrefs.java
//  ChatServer
//
//  Created by John McKisson on 4/16/08.
//  Copyright 2008 __MyCompanyName__. All rights reserved.
//
package com.presence.chat;

import java.util.prefs.*;

public class ChatPrefs {
	static Preferences prefs = Preferences.userNodeForPackage(ChatPrefs.class);
	
	static final String PREF_AUTHIP			= "IP Auth";
	static final String PREF_MOTD			= "Server MOTD";
	static final String PREF_NAME			= "Server Name";
	static final String PREF_PORT			= "Server Port";
	static final String PREF_SPAM_PROT		= "Spam Protection";
	static final String PREF_SPAM_THRESH	= "Spam Threshold";
	
	public static boolean getIPAuth() {
		return prefs.getBoolean(PREF_AUTHIP, true);
	}
	
	public static void setIPAuth(boolean val) {
		prefs.putBoolean(PREF_AUTHIP, val);
	}
	
	public static String getMOTD() {
		return prefs.get(PREF_MOTD, "");
	}
	
	public static void setMOTD(String msg) {
		prefs.put(PREF_MOTD, msg);
	}
	
	public static String getName() {
		return prefs.get(PREF_NAME, "jChatServ");
	}
	
	public static void setName(String name) {
		prefs.put(PREF_NAME, name);
	}
	
	public static int getPort() {
		return prefs.getInt(PREF_PORT, 4050);
	}
	
	public static void setPort(int port) {
		prefs.putInt(PREF_PORT, port);
	}
	
	public static int getSpamThreshold() {
		return prefs.getInt(PREF_SPAM_THRESH, 6);
	}
	
	public static void setSpamThreshold(int t) {
		prefs.putInt(PREF_SPAM_THRESH, t);
	}
	
	public static boolean spamProtection() {
		return prefs.getBoolean(PREF_SPAM_PROT, true);
	}
	
	public static void setSpamProtection(boolean v) {
		prefs.putBoolean(PREF_SPAM_PROT, v);
	}

}
