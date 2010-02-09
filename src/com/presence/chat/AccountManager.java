//
//  AccountManager.java
//  ChatServer
//
//  Created by John McKisson on 4/3/08.
//  Copyright 2008 __MyCompanyName__. All rights reserved.
//
package com.presence.chat;

import java.io.*;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.*;

import com.thoughtworks.xstream.*;
import com.thoughtworks.xstream.converters.*;

public class AccountManager {
	private static final Logger log = Logger.getLogger("global");
	
	private static final String FILEPATH = "accounts.xml";
	
	//Setup XStream
	private static final XStream xstream;
	static {
		xstream = new XStream();
		
		xstream.alias("account", ChatAccount.class);
		
		//xstream.addImplicitCollection(Vector.class, "vector");
		
		xstream.useAttributeFor(ChatAccount.class, "name");
		xstream.useAttributeFor(ChatAccount.class, "level");
	}


	static Vector<ChatAccount> accounts;
	
	static boolean authEnabled = true;

	public static boolean authEnabled() {
		return authEnabled;
	}
	
	public static void setAuth(boolean val) {
		authEnabled = val;
	}
	
	
	public static Vector<ChatAccount> getAccounts() {
		return accounts;
	}
	
	
	public static void loadAccounts() throws FileNotFoundException {
	
		boolean exists = new File(FILEPATH).exists();
		
		if (!exists) {
			log.info("No accounts file found, first person to connect will be made super admin");
		
			accounts = new Vector<ChatAccount>();
			return;
		}
			
		BufferedReader in = new BufferedReader(new FileReader(FILEPATH));
		StringBuilder xml = new StringBuilder();
		
		String str;
		try {
			while ((str = in.readLine()) != null) {
				xml.append(str);
			}
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		accounts = (Vector<ChatAccount>)xstream.fromXML(xml.toString());
		
		log.info("Account information loaded");
	}
	

	public static void saveAccounts() {
	
		//new SaveAccounts(accounts);
		
		Vector<ChatAccount> data = (Vector<ChatAccount>)accounts.clone();
		
		//Remove all instances of TemporaryChatAccounts
		Iterator<ChatAccount> it = data.iterator();
		
		while (it.hasNext()) {
			if (it.next() instanceof TemporaryChatAccount)
				it.remove();
		}

		String xml = xstream.toXML(data);

		try {
			FileOutputStream fos = new FileOutputStream(FILEPATH);

			fos.write(xml.getBytes());

			fos.flush();
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		log.info("Account information saved");

	}
	
	public static int numAccounts() {
		return accounts.size();
	}
	
	
	/**
	 * Get account by name
	 * @param name Name of the chat account, case insensitive
	 * @return ChatAccount of the specified name, null if it does not exist
	 */
	public static ChatAccount getAccount(String name) {
		//log.info("looking for account " + name);
		
		if (name == null)
			return null;
			
		String n = name.toLowerCase();
		
		
		for (ChatAccount ac : accounts) {
			if (ac.getName().equals(n))
				return ac;
		}
	
		return null;
	}
	
	
	/**
	 * Adds a new account.
	 * @param account Account to be added
	 * @return True if account was added successfully, False if the account already exists or other error
	 */
	public static boolean addAccount(ChatAccount account) {
	
		if (account == null)
			return false;
	
		//Fail if an account with this name already exists
		if (getAccount(account.getName()) != null)
			return false;
		
		return accounts.add(account);
	}
	
	
	/**
	 * Removes an existing account.
	 * @param account Account to be removed
	 * @return True if account was removed successfully, False otherwise
	 */
	public static boolean removeAccount(ChatAccount account) {
		return accounts.remove(account);
	}
	
	
	/**
	 * Removes an existing account, by name.
	 * @param acconutName Name of the account to be removed
	 * @return True if account was removed successfully, False otherwise
	 */
	public static boolean removeAccount(String accountName) {
		ChatAccount account = getAccount(accountName);
			
		return accounts.remove(account);
	}
	
	public class SaveAccounts extends Thread {
         Vector<ChatAccount> data;
		 
         SaveAccounts(Vector<ChatAccount> ac) {
             data = (Vector<ChatAccount>)ac.clone();
			 
			 start();
         }
 
         public void run() {
		 
			//Remove all instances of TemporaryChatAccounts
			Iterator<ChatAccount> it = data.iterator();
			
			while (it.hasNext()) {
				if (it.next() instanceof TemporaryChatAccount)
					it.remove();
			}

			String xml = xstream.toXML(data);

			try {
				FileOutputStream fos = new FileOutputStream(FILEPATH);

				fos.write(xml.getBytes());

				fos.flush();
				fos.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			log.info("Account information saved");
         }
     }
}
