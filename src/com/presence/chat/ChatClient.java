//
//  ChatClient.java
//  ChatServer
//
//  Created by John McKisson on 3/31/08.
//  Copyright 2008 __MyCompanyName__. All rights reserved.
//
package com.presence.chat;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.logging.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import javax.swing.Timer;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.*;

import com.presence.chat.protocol.*;

import static com.presence.chat.ANSIColor.*;
import static com.presence.chat.protocol.ChatCommand.*;

public class ChatClient extends SimpleChannelHandler {
	private static final Logger log = Logger.getLogger("global");
	
	static final int STATE_DISCONNECTED = 0;
	static final int STATE_AUTHENTICATING = 1;
	static final int STATE_CONNECTED = 2;
	
	static final byte CHAT_NAME_CHANGE = 1;
	static final byte CHAT_TEXT_EVERYBODY = 4;
	static final byte CHAT_TEXT_PERSONAL = 5;
	static final byte CHAT_VERSION = 19;
	
	static final byte CHAT_SNOOP = 30;
	static final byte CHAT_SNOOP_DATA = 31;
	static final byte CHAT_SNOOP_COLOR = 32;
	
	static final byte CHAT_END_OF_COMMAND = (byte)255;

	int state;

	ChatProtocol protocol = null;
	Channel myChannel = null;
	ChatRoom myRoom = null;
	
	ChatAccount myAccount = null;
	
	ChatLog snoopLog = null;
	ChatLog messageLog = null;
	
	Timer authTimer = null;
	
	String myName;
	String address;
	String version;
	int port;
	
	boolean authenticated;
	boolean isGagged;
	
	public int spamThresh = 0;
	long gagTime;
	
	public long lastActivity;
	
	static Matcher matcher = Pattern.compile("^\n*[ ]*(.*)\n*$").matcher("");
	
	public ChatClient(String name, String ip) {
		state = STATE_DISCONNECTED;
		//Logger.getLogger("global").info("new ChatClient: " + name + "("+ip+")");
		myName = name;
		address = ip;
		
		//Add to global client list
		ChatServer.getClients().add(this);
	}
	
	
	@Override
	public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
		if (e instanceof ChannelStateEvent) {
			Logger.getLogger("global").info(e.toString());
		}
		super.handleUpstream(ctx, e);
	}

	
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
		lastActivity = System.currentTimeMillis();
		
		//Grab command byte and message	
		Object[] obj = (Object[])e.getMessage();	
		ChatCommand cmd = (ChatCommand)obj[0];
		String message = (String)obj[1];
		
		switch (cmd) {
			case TEXT_ONE:
				//Client needs to be able to chat their password while unauthenticated
				ChatServer.processCommand(this, message);
				
				break;
				
			case VERSION:
				setVersion(message);
				break;
		}
		
		if (!authenticated)
			checkAuth();
		
		switch (cmd) {
			case TEXT_ALL:
			
				if (authenticated)
					forwardToRoom(message);
					
				break;
				
			case NAME_CHANGE:
				
				if (authenticated) {
					setName(message);
					ChatServer.getStats().namechanges++;
				}
					
				break;
				
			case SNOOP_DATA:
			
				logSnoopedData(message);
				
				break;
		}
		
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
		Logger.getLogger("global").log(Level.WARNING, "Unexpected downstream exception", e.getCause());
		e.getCause().printStackTrace();
		e.getChannel().close();
		
		disconnect();
	}

	
	public boolean isAuthenticated() {
		return authenticated;
	}
	
	
	/**
	 * myAccount must not be null!
	 */
	public void setAuthenticated(boolean val, String reason) {
		authenticated = val;
		
		if (val == false) {
			//Kick them or something?
			log.info(myName + " un-authenticated");
			return;
		}
		
		//Add incoming IP address to known addresses list for this account
		String addr = getAddr();
		
		if (!myAccount.addressIsKnown(addr)) {
			myAccount.addAddress(addr);
			
			AccountManager.saveAccounts();
		}
		
		
		//Send Version
		sendChat(ChatServer.VERSION);
		
		//Send MOTD
		sendChat(ChatPrefs.getMOTD());
		
		//Global
		ChatServer.echo(String.format("%s[%s%s%s] %s%s%s has connected %s(%s%s%s)%s",
			RED, WHT, ChatPrefs.getName(), RED, WHT, myName, RED, YEL, RED, reason, YEL, RED));
			
		log.info(String.format("%s connected with %s", myName, reason));
				
		//Send person to room but dont echo
		toRoom(ChatServer.getRoom("main"), null, false);
		
		myAccount.updateLastLogin();
		
		ChatServer.getStats().connects++;
	}
	
	
	public Channel getSocket() {
		return myChannel;
	}
	
	
	void setVersion(String ver) {
		version = new String(ver);
 	}

	
	/*
	 * Handle data incoming from the socket
	 */
	public void processIncomingData(String msg) {
		if (protocol == null) {
			initProtocol(msg);
			
			//Start authentication process
			checkAuth();
			
			return;
		}
		
		//Protocol delegates back to self
		protocol.processIncomingData(msg);
	}
	
	
	/**
	 */
	void checkAuth() {
		//If this is the first client connecting, make them a super admin
		if (AccountManager.numAccounts() == 0) {
			ChatAccount ac = new ChatAccount(myName, "", 5);
			
			//Add Account
			if (AccountManager.addAccount(ac)) {
			
				//Save
				AccountManager.saveAccounts();
			
				myAccount = ac;
				
				setAuthenticated(true, "Super Admin Auth");
				
				sendChat(String.format("%s chats to you, 'Grats bro you're the first person to connect! I have made you a super admin... please change your password.'", ChatPrefs.getName()));
			} else {
				log.warning(String.format("Error trying to add initial super admin account for %s", myName));
			}
			
			return;
		}
		
		
		if (AccountManager.authEnabled()) {
		
			//Attempt to associate this client with an account
			ChatAccount account = AccountManager.getAccount(myName);
			
			//Check if account exists
			if (account == null) {
				sendChat(String.format("%s chats to you, 'I did not find an account for you, please contact an administrator.'", ChatPrefs.getName()));
				
				disconnect();
				return;
			}
			
			myAccount = account;	//FIX: required for setAuthenticated
		
			//Check if we can IP Auth
			//TODO: Add ip auth toggle in account settings
			if (ChatPrefs.getIPAuth()) {
				String incomingAddr = getAddr();
				
				if (myAccount.addressIsKnown(incomingAddr)) {
					//log.info(String.format("%s IP authenticated", myName));
				
					setAuthenticated(true, "IP Auth");
					
					return;
				}
			}
		
			//Send msg and set timer for password to be sent
			String name = ChatPrefs.getName();
			
			sendChat(String.format("%s chats to you, 'Password required, you have 15 seconds to chat me your password'", name));
			
			authTimer = new Timer(1000 * 15,
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						if (authenticated)
							return;
							
						sendChat(String.format("%s chats to you, 'Password Timeout Expired'", ChatPrefs.getName()));
						
						disconnect();
					}
				}
			);
			authTimer.setRepeats(false);
			authTimer.start();
			
		} else {
		
			//First make sure they arent trying to impersonate someone already online
			boolean found = ChatServer.checkOnlineName(myName, myName);
			
			if (found) {
				sendChat("That person is already online, connect with a different chatname.");
				
				disconnect();
				return;
			}
			
			
			//Create a temporary level 0 account for this person based on their chatname
			myAccount = new TemporaryChatAccount(myName);
		
		
			setAuthenticated(true, "Guest Auth");
		}
			
	}
	
	public void disconnect() {
		ChatServer.disconnectClient(this);
		
		if (authenticated) {
			//Let other ppl know someone disconnected
			ChatServer.echo(String.format("%s[%s%s%s] %s%s%s has left the server",
				RED, WHT, ChatPrefs.getName(), RED, WHT, myName, RED));
		}
		
		log.info(String.format("%s has disconnected", myName));
	}
	
	
	/*
	 * Create a new ChatProtocol instance based on the handshake received by the client.
	 * checkAuth is called directly after.
	 */
	void initProtocol(String str) {
		protocol = ChatProtocolFactory.initProtocol(this, str);
	
		//Auto accept for now
		protocol.sendConnectResponse();
		
		protocol.sendVersion();
	}
	
	public void setProtocol(ChatProtocol prot) {
		protocol = prot;
	}
	
	public void setSocket(Channel sock) {
		myChannel = sock;
	}
	
	
	void initSnoopLog() {
		snoopLog = new ChatLog(10000);
	}
	
	
	public ChatAccount getAccount() {
		return myAccount;
	}
	
	
	public String getAddr() {
		if (myChannel == null)
			return "null Channel";
			
		SocketAddress addr = myChannel.getRemoteAddress();
		
		if (addr == null)
			return "channel unconnected";
			
		InetSocketAddress sockAddr = (InetSocketAddress)addr;
		
		if (sockAddr == null)
			return "null InetAddress";
			
		return sockAddr.getAddress().getHostAddress();
	}
	
	public String getName() {
		return myName;
	}
	
	
	public ChatLog getMessageLog() {
		if (messageLog == null)
			messageLog = new ChatLog(50);
			
		return messageLog;
	}
	
	public ChatProtocol getProtocol() {
		return protocol;
	}
	
	public boolean isGagged() {
		return isGagged;
	}
	
	public void setGagged(boolean val) {
		isGagged = val;
	}
	
	/*
	public void setName(String name) {
		myName = name;
	}
	*/

	
	public void setName(String newName) {
	
		//Make sure we arent trying to set our chatname to someone else online OR to someones account name
		//This will return true if you are trying to change your name to yourself, who is still connected as a zombie
		boolean found = ChatServer.checkOnlineName(newName, myName);

		if (!found) {
			//Ok now look thru all the accounts
			Enumeration<ChatAccount> en = AccountManager.getAccounts().elements();
			
			while (en.hasMoreElements()) {
				ChatAccount ac = en.nextElement();
				
				//Skip own account and null account(?)
				if (ac == myAccount)
					continue;
				
				if (ac.getName().equalsIgnoreCase(newName)) {
					found = true;
					break;
				}
			}
		}
		
		if (found) {
			//Kick user for attempting to impersonate someone
			sendChat(String.format("%s chats to you, 'Nice try'", ChatPrefs.getName()));
			
			disconnect();
			
			ChatServer.echo(String.format("%s[%s%s%s] %s%s%s has been kicked for attempting to impersonate %s%s%s!",
				RED, WHT, ChatPrefs.getName(), RED, WHT, myName, RED, WHT, newName, RED));
			
			return;
		}
	
		//Finally we're ok to change chatname
		ChatServer.echo(String.format("%s[%s%s%s] %s%s%s is now known as %s%s%s",
			RED, WHT, ChatPrefs.getName(), RED, WHT, myName, RED, WHT, newName, RED));
	
		myName = newName;
	}
	
	/*
	 */
	public ChatRoom getRoom() {
		return myRoom;
	}
	
	
	/**
	 * Move the user to a new room, first removing them from their current room.
	 * All room password checks must be done before this is called
	 * @param room Room to be moved to
	 */
	public boolean toRoom(ChatRoom room, String password, boolean echo) {
	
		String roomPass = room.getPassword();
	
		if (roomPass != null && !roomPass.equals(password))
			return false;
	
		if (myRoom != null)
			//This does a room echo that the person has left
			myRoom.removePerson(this);
	
	
		//This takes care of all notifications
		room.addPerson(this, echo);
		
		myRoom = room;
		
		return true;
	}
	
	
	/*
	 * Set current room
	 */
	@Deprecated
	public void setRoom(ChatRoom room) {
		myRoom = room;
	}
	
	
	/*
	 * Forward the received string to everyone in my room except myself
	 */
	public void forwardToRoom(String msg) {
		if (myRoom == null)
			return;
			
		//Ungag person if they are past their gag time
		if (System.currentTimeMillis() >= gagTime)
			isGagged = false;
			
		if (isGagged) {
			sendChat("You are gagged, your message was not heard.");
			return;
		}
			
		//If this chat is above threshold then we're spamming
		if (getAccount().getLevel() < 2 && ChatPrefs.spamProtection() && ++spamThresh >= ChatPrefs.getSpamThreshold()) {
			//Don't spam everyone else if we're already gagged
			if (!isGagged) {
				isGagged = true;
				ChatServer.echo(String.format("%s[%s%s%s] %s%s%s Auto-Gagged for spamming",
					RED, WHT, ChatPrefs.getName(), RED, WHT, myName, RED));
			}
			
			//Reset gag time to 15 seconds
			gagTime = System.currentTimeMillis() + 15 * 1000;
			
			sendChat("You have been gagged for 15 seconds");
		
			return;
		}
			
		//String msg = protocol.getContent();
		
		//Trim carriage returns
		matcher.reset(msg);
		
		if (matcher.find())
			msg = matcher.group(1);
			
		//printStringBytes(msg);
		
		//Prepend name tag if we're trying to spoof someone
		//WOW, this code is retarded!
		int toffset = 0;
		
		String test = msg.toLowerCase().trim();
	
		//printStringBytes(test);
		
		while (toffset < test.length()) {
			
			//System.out.println("testing toffset " + toffset);
			//Trim off a possible remaining color codes
			char testChar = test.charAt(toffset);
			if (testChar == '[' || testChar == (char)27) {
				//System.out.println("found [");
				int mIdx = test.indexOf('m', toffset);
				//System.out.println("m idx = " + mIdx);
				toffset = mIdx + 1;
				//test = test.substring(mIdx + 1);

			} else {
				break;
			}
		}
		
		//printStringBytes(test);

		if (!test.startsWith(myName.toLowerCase(), toffset))
			msg = String.format("%s(%s%s%s)%s%s", YEL, RED, myName, YEL, RED, msg);
		
		
		myRoom.echo(msg, this, this);
		
		ChatServer.getStats().chats++;
	}
	
	
	public void sendChatAll(String str) {
		protocol.sendChatAll(str);
	}
	
	public void sendChat(String str) {
		if (protocol != null)
			protocol.sendChat(str);
		else
			log.warning(String.format("Protocol null when trying to chat to %s", myName));
	}
	
	
	public void logSnoopedData(String str) {
		
	}
}