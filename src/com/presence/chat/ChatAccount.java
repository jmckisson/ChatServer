//
//  ChatAccount.java
//  ChatServer
//
//  Created by John McKisson on 4/3/08.
//  Copyright 2008 __MyCompanyName__. All rights reserved.
//
package com.presence.chat;

import java.io.Serializable;

import java.util.logging.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class ChatAccount implements Serializable {

	String name;
	String password;
	int level;
	Date lastLogin;
	boolean compact;
	
	Vector<String> knownAddresses = null;
	
	Vector<String> gagList = null;
	
	//Fields that can be used to store custom information from plugins etc...
	Map<String, String> customFields = null;
	
	public ChatAccount(String name) {
		this(name, "", 0);
	}
	
	public ChatAccount(String name, String password) {
		this(name, password, 0);
	}
	
	public ChatAccount(String name, String password, int level) {
		this.name = name.toLowerCase();
		this.password = password;
		this.level = level;
		
		Logger.getLogger("global").info("New account for " + name);
	}
	
	private Object readResolve() {
		if (customFields == null)
			customFields = new HashMap<String, String>();
		
		return this;
	}
	
	
	public void updateLastLogin() {
		lastLogin = new Date(System.currentTimeMillis());
	}
	
	
	public Date lastLogin() {
		return lastLogin;
	}
	
	
	/**
	 * Adds an address to this accounts known addresses list
	 * @param addr Address to be added
	 */
	public void addAddress(String addr) {
		if (knownAddresses == null)
			knownAddresses = new Vector<String>();
			
		knownAddresses.add(addr);
		
		Logger.getLogger("global").info(String.format("Added %s for account %s", addr, name));
	}
	
	
	/**
	 * Check if an address is in the known addresses list
	 * @param addr Address to be checked
	 * @return True if address is known, False otherwise
	 */
	public boolean addressIsKnown(String addr) {
		if (knownAddresses == null)
			return false;
			
		return knownAddresses.contains(addr);
	}
	
	
	public void addGag(String name) {
		if (gagList == null)
			gagList = new Vector<String>();
			
		gagList.add(name.toLowerCase());
		
		AccountManager.saveAccounts();
	}
	
	public Vector<String> gagList() {
		return gagList;
	}
	
	public void removeGag(String name) {
		if (gagList == null)
			return;
			
		gagList.remove(name.toLowerCase());
		
		AccountManager.saveAccounts();
	}
	
	
	/**
	 * Auto lower cases name
	 */
	public boolean hasGagged(String name) {
		if (gagList == null)
			return false;
			
		return gagList.contains(name.toLowerCase());
	}
	
	
	public String getName() {
		return name;
	}
	
	
	public String getPassword() {
		return password;
	}
	
	
	/**
	 * Sets a new password for this account.  For security reasons, this also clears the knownAddresses.
	 * @param newPass New password
	 */
	public void setPassword(String newPass) {
		password = newPass;
		
		knownAddresses = new Vector<String>();
		
		AccountManager.saveAccounts();
	}
	
	public int getLevel() {
		return level;
	}
	
	public void setLevel(int lvl) {
		level = lvl;
	}
	
	public boolean isCompact() {
		return compact;
	}
	
	public void setCompact(boolean val) {
		compact = val;
	}
	
	public String getField(String field) {
		return customFields.get(field);
	}
	
	public String setField(String field, String value) {
		return customFields.put(field, value);
	}
}
