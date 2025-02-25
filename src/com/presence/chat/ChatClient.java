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
import java.util.*;
import java.util.logging.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import javax.swing.Timer;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.presence.chat.commands.CommandEntry;
import com.presence.chat.event.NotificationEvent;
import com.presence.chat.socket.DecodedMsg;
import io.netty.buffer.*;
import io.netty.channel.*;

import com.presence.chat.log.*;
import com.presence.chat.protocol.*;

import static com.presence.chat.ANSIColor.*;
import static com.presence.chat.protocol.ChatCommand.*;

public class ChatClient extends SimpleChannelInboundHandler<DecodedMsg> {
//public class ChatClient extends SimpleChannelUpstreamHandler {
	
	ChatProtocol protocol = null;
	Channel myChannel = null;
	ChatRoom myRoom = null;
	
	ChatAccount myAccount = null;
	
	MessageLog messageLog = null;
	StringBuilder snoopLog = null;
	
	boolean snooping = false;
	List<ChatClient> snoopers = null;
	
	Timer authTimer = null;
	
	DecodedMsg lastEvent = null;
	Throwable exception = null;
	
	String myName;
	String address;
	String version;
	int port;
	
	boolean authenticated;
	boolean isGagged;
	
	public int spamThresh = 0;
	long gagTime;
	
	public long lastActivity;
	
	public ChatClient(String name, String ip) {
		//Logger.getLogger("global").info("new ChatClient: " + name + "("+ip+")");
		myName = name;
		address = ip;
	}
	
	/*
	@Override
	public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
		if (e instanceof ChannelStateEvent) {
			Logger.getLogger("global").info(e.toString());
		}
		super.handleUpstream(ctx, e);
	}
	
	@Override
	public void handleDownstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {

		if (e instanceof MessageEvent) {
			Logger.getLogger("global").info("Writing:: " + e);
		}

		super.handleDownstream(ctx, e);
	}
	*/
	
	
	/*
	@Override
	public void writeRequested(ChannelHandlerContext ctx, MessageEvent e) {
		lastEvent = e;
	}
	*/

	
	@Override
	public void channelRead0(ChannelHandlerContext ctx, DecodedMsg msg) {
		lastEvent = msg;
		lastActivity = System.currentTimeMillis();
		
		//Grab command byte and message	
		//Object[] obj = (Object[])e.getMessage();
		ChatCommand cmd = msg.getCmd();
		String message = msg.getMessage();
		
		switch (cmd) {
			case TEXT_ONE:
				//Client needs to be able to chat their password while unauthenticated
				//ChatServer.processCommand(this, message);
				processCommand(message);
				return;

			case VERSION:
				protocol.setVersion(message);
				//setVersion(message);
				return;

		}

		//Logger.getGlobal().info("channelRead0");
		//Logger.getGlobal().info(ByteBufUtil.hexDump((ByteBuf)obj[2]));
		
		if (!authenticated)
			//checkAuth();
			return;
		
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
				handleSnoopData(message);
				break;
				
			case PING_REQUEST:
				handlePingRequest(message);
				break;
		}
		
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable e) {
		Logger.getLogger("global").log(Level.WARNING, "Unexpected downstream exception", e.getCause());
		e.getCause().printStackTrace();
		exception = e;
		
		DecodedMsg msg = lastEvent;
		String str = msg.getMessage();
		System.out.println(str);
		//System.out.println(ByteBufUtil.hexDump((ByteBuf)obj[2]));
		//System.out.println("=======");
		
		System.out.println(ByteBufUtil.hexDump(protocol.getLastBuffer()));
		System.out.println("=======");
		ChatServer.getStats().exceptions++;
	}
	
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
	
		//Logger.getLogger("global").info(e.toString());
	
		if (authenticated)
			logDisconnect();
			
		ChatServer.disconnectClient(this);
		
		super.channelInactive(ctx);
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
			Logger.getLogger("global").info(myName + " un-authenticated");
			ChatServer.disconnectClient(this);
			
			return;
		}
		
		//Add incoming IP address to known addresses list for this account
		String addr = getAddr();
		
		if (!myAccount.addressIsKnown(addr)) {
			myAccount.addAddress(addr);
			
			AccountManager.saveAccounts();
		}
		

		String acName = myAccount.getName();
		
		ChatClient zomb = null;
		List<ChatClient> clients = ChatServer.getClients();
		synchronized (clients) {
			for (ChatClient cl : clients) {
				if (cl.getName().equalsIgnoreCase(myName) ||
					cl.getAccount().getName().equalsIgnoreCase(acName)) {
					
					//Don't disconnect here, it would cause a ConcurrentModificationException on client list!
					zomb = cl;
					break;
				}
			}
		}
		
		if (zomb != null)
			zomb.setAuthenticated(false, null);
		
		//Add to global client list
		ChatServer.addClient(this);
		
		//Send Version
		sendChat(ChatServer.VERSION);
		
		//Send MOTD
		sendChat(ChatPrefs.getMOTD());
		
		//Global Echo
		String str = String.format("%s%s%s has connected %s(%s%s%s)%s",
			WHT, myName, RED, YEL, RED, reason, YEL, RED);
			
		ChatServer.echo(ChatServer.HEADER + str);
		
		Logger.getLogger("main").info(str);
			
		//ChatServer.getRoom("main").getLog().addEntry(str);
			
		//log.info(String.format("%s connected with %s", myName, reason));
				
		if (myRoom == null)	//Check if its a reconnect
			//Send person to room but dont echo
			toRoom(ChatServer.getRoom("main"), null, false, false);
		
		myAccount.updateLastLogin();
		
		ChatServer.getNotificationCenter().post(new NotificationEvent("ClientConnected", myName));
		
		ChatServer.getStats().connects++;
	}
	
	
	public Channel getSocket() {
		return myChannel;
	}
	
	/*
	void setVersion(String ver) {
		version = new String(ver);
		System.out.println("version set");
 	}
	*/
	
	
	/**
	 */
	public void checkAuth() {
		//If this is the first client connecting, make them a super admin
		if (AccountManager.numAccounts() == 0) {
			ChatAccount ac = new ChatAccount(myName, "", 5);
			
			//Add Account
			if (AccountManager.addAccount(ac)) {
			
				//Save
				AccountManager.saveAccounts();
			
				myAccount = ac;
				
				setAuthenticated(true, "Super Admin Auth");
				
				serverChat("Grats you're the first person to connect! I have made you a super admin... please change your password.");
			} else {
				Logger.getLogger("global").warning(String.format("Error trying to add initial super admin account for %s", myName));
			}
			
			return;
		}
		
		
		if (AccountManager.authEnabled()) {
		
			//Attempt to associate this client with an account
			ChatAccount account = AccountManager.getAccount(myName);
			
			//Check if account exists
			if (account == null) {
				serverChat("I did not find an account for you, please contact an administrator.");
				
				//Client isnt in clients table yet, so just close socket
				myChannel.close();
				return;
			}
			
			myAccount = account;	//FIX: required for setAuthenticated
		
			//Check if we can IP Auth
			//TODO: Add ip auth toggle in account settings
			if (ChatPrefs.getIPAuth()) {
				String incomingAddr = getAddr();
				
				if (myAccount.addressIsKnown(incomingAddr)) {				
					setAuthenticated(true, "IP Auth");
					
					return;
				}
			}
		
			//Send msg and set timer for password to be sent
			String name = ChatPrefs.getName();
			
			serverChat("Password required, you have 15 seconds to chat me your password");
			
			authTimer = new Timer(1000 * 15,
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						if (authenticated)
							return;
							
						serverChat("Password Timeout Expired");
						
						myChannel.close();
					}
				}
			);
			authTimer.setRepeats(false);
			authTimer.start();
			
		} else {	//Auth disabled
		
			//First make sure they arent trying to impersonate someone already online
			boolean found = ChatServer.checkOnlineName(myName, myName);
			
			if (found) {
				sendChat("That person is already online, connect with a different chatname.");
				
				myChannel.close();
				return;
			}
			
			
			//Create a temporary level 0 account for this person based on their chatname
			myAccount = new TemporaryChatAccount(myName);
		
			setAuthenticated(true, "Guest Auth");
		}
			
	}
	
	/**
	 * Log client disconnect
	 */
	public void logDisconnect() {
		//ChatServer.disconnectClient(this);
				
		if (authenticated) {
			String exceptionString = "";
			if (exception != null) {
				String cause = exception.getCause().getLocalizedMessage();
				System.out.println(cause);
				exceptionString = String.format(" %s(%s%s%s)", YEL, RED, exception.getCause().getMessage(), YEL);
			}
			
			String str = String.format("%s%s%s has left the server%s",
				WHT, myName, RED, exceptionString);
		
			//Let other ppl know someone disconnected
			ChatServer.echo(ChatServer.HEADER + str);
			
			//Add it to the main room log
			Logger.getLogger("main").info(str);
		}
		
		Logger.getLogger("global").info(String.format("%s has disconnected", myName));
		
		ChatServer.getNotificationCenter().post(new NotificationEvent("ClientDisconnected", myName));
	}

	
	public void setProtocol(ChatProtocol prot) {
		protocol = prot;
	}
	
	public void setSocket(Channel sock) {
		myChannel = sock;
	}
	
	public Channel getChannel() {
		return myChannel;
	}
	
	public ChatAccount getAccount() {
		return myAccount;
	}
	
	
	public String getAddr() {
		if (myChannel == null)
			return "null Channel";
			
		SocketAddress addr = myChannel.remoteAddress();
		
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
	
	public long getLastActivity() {
		return lastActivity;
	}
	
	public MessageLog getMessageLog() {
		if (messageLog == null)
			messageLog = new MessageLog(myName);
			
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
			//If our account matches the account of the name being changed to, assume
			//someone else stole our name, so kick them
			
			ChatClient impostor = null;
			Iterator<ChatClient> it = ChatServer.getClients().iterator();
			
			while (it.hasNext()) {
				ChatClient c = it.next();
				ChatAccount ac = c.getAccount();
				String name = ac.getName();
				
				if (name != null && name.equalsIgnoreCase(myAccount.getName()) && ac != myAccount) {
					impostor = c;
					break;
				}
			}

			if (impostor != null) {
				impostor.serverChat(myName + " wants their name back!");
				ChatServer.disconnectClient(impostor);			
			} else {
		
				//Kick user for attempting to impersonate someone
				serverChat("Nice try");
				
				ChatServer.disconnectClient(this);
				
				ChatServer.echo(String.format("%s[%s%s%s] %s%s%s has been kicked for attempting to impersonate %s%s%s!",
					RED, WHT, ChatPrefs.getName(), RED, WHT, myName, RED, WHT, newName, RED));
			}
			
			return;
		}
	
		//Finally we're ok to change chatname
		ChatServer.echo(String.format("%s[%s%s%s] %s%s%s is now known as %s%s%s",
			RED, WHT, ChatPrefs.getName(), RED, WHT, myName, RED, WHT, newName, RED));
	
		/*
		Logger.getLogger("global").info(String.format("Name change '%s' => '%s'", myName, newName));
		for (byte b : newName.getBytes()) {
			System.out.print(b + " ");
		}
		*/
	
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
	public boolean toRoom(ChatRoom room, String password, boolean echo, boolean force) {
	
		if (!force) {
			String roomPass = room.getPassword();
		
			//Verify password
			if (roomPass != null && !roomPass.equals(password))
				return false;
				
			//Check minlevel
			if (room.getMinLevel() > myAccount.getLevel())
				return false;
		}
	
		if (myRoom != null)
			//This does a room echo that the person has left
			myRoom.removePerson(this);
	
	
		//This takes care of all notifications
		room.addPerson(this, echo);
		
		myRoom = room;
		
		return true;
	}
	
	static final Matcher trimMatcher = Pattern.compile("\\s*(.*)\\s*", Pattern.DOTALL).matcher("");
	
	/**
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
				
				String gagMsg = String.format("%s%s%s Auto-Gagged for spamming", WHT, myName, RED);
				
				ChatServer.echo(ChatServer.HEADER + gagMsg);
					
				Logger.getLogger("global").info(gagMsg);
			}
			
			//Reset gag time to 15 seconds
			gagTime = System.currentTimeMillis() + 15 * 1000;
			
			sendChat("You have been gagged for 15 seconds");
		
			return;
		}
		
		trimMatcher.reset(msg);
		
		if (!trimMatcher.find()) {
			Logger.getLogger("global").warning("Unable to match anything?!");
			return;
		}
		
		msg = trimMatcher.group(1);
		
		msg = spoofCheck(msg);
		
		msg = msg.replaceFirst("\n", "");
		int lastCR = msg.lastIndexOf("\n");
		if (lastCR >= msg.length() - 5)	//If we end in a CR or CR<color>, get rid of the CR
			msg = msg.substring(0, lastCR);
		
		myRoom.echo(msg, this, this);
		
		ChatServer.getStats().chats++;
	}


	/**
	 *  Prepend a name tag if they're trying to spoof someone
	 */
	private String spoofCheck(String msg) {
	
		String msgNoANSI = ANSIColor.strip(msg).toLowerCase();
					
		if (!msgNoANSI.startsWith(ANSIColor.strip(myName).toLowerCase()))
			return String.format("%s(%s%s%s)%s%s", YEL, RED, myName, YEL, RED, msg);
		else
			return msg;
	}
	
	
	public void sendChatAll(String str) {
		protocol.sendChatAll(str+NRM);
	}
	
	public void serverChat(String str) {
		sendChat(String.format("%s chats to you, '%s'", ChatPrefs.getName(), str));
	}
	
	public void sendChat(String str) {
		if (protocol != null)
			protocol.sendChat(str);
		else
			Logger.getLogger("global").warning(String.format("Protocol null when trying to chat to %s", myName));
	}
	
	public void sendNameChange(String name) {
		protocol.sendNameChange(name);
	}
	
	public void startSnoop(ChatClient client) {
	
		if (snoopers == null)
			snoopers = new LinkedList<ChatClient>();
	
		if (snoopers.contains(client))
			client.serverChat(String.format("You're already snooping %s!", myName));
		else {
			snoopers.add(client);
			
			client.sendChat(String.format("%s[%s%s%s] You have started snooping [%s%s%s], but they may still need to enable snooping by the server", RED, WHT, ChatPrefs.getName(), RED, WHT, myName, RED));
			sendChat(String.format("%s[%s%s%s] You are now being snooped by [%s%s%s]", RED, WHT, ChatPrefs.getName(), RED, WHT, client.getName(), RED));
		}
	
		if (snooping)
			return;
		
		protocol.startSnoop();
	}
	
	public void stopSnoop(ChatClient client) {
		if (!snoopers.contains(client))
			client.serverChat("You're not snooping them!");
		else {
			snoopers.remove(client);
			client.sendChat(String.format("%s[%s%s%s] You have stopped snooping [%s%s%s]", RED, WHT, ChatPrefs.getName(), RED, WHT, myName, RED));
			sendChat(String.format("%s[%s%s%s] You are no longer being snooped by [%s%s%s]", RED, WHT, ChatPrefs.getName(), RED, WHT, client.getName(), RED));
		}
	}
	
	
	void handleSnoopData(String str) {
		//if (snoopLog == null)
		//	snoopLog = new StringBuilder();
		
		StringBuilder strBuf = new StringBuilder();
		
		byte[] data = str.getBytes();
		int len = data.length - 4;
		String line = new String(data, 4, len);
		
		String[] lines = line.replaceAll("\r", "").split("\n");
		
		for (String l : lines)
			strBuf.append(">>" + l);

		/*
		//Skip over fore and back colors
		for (int idx = 4; idx < len; idx++) {
			byte c = data[idx];
			
			if (c == '\r')
				continue;
			
			strBuf.append(c);
		}
		*/
		
		if (strBuf.length() > 0) {
			//snoopLog.append(strBuf);
			sendSnoopData(strBuf.toString());
		}
	}
	
	private void sendSnoopData(String str) {
		if (snoopers == null || snoopers.size() == 0)
			return;
			
		for (ChatClient c : snoopers) {
			c.sendChatAll(str);
		}
	}
	
	void handlePingRequest(String msg) {
		Logger.getLogger("global").info(myName + " PING Response");
		protocol.sendPingResponse(msg);
	}

	static final Matcher matcherChatPrivate =
			Pattern.compile("\\s*(.*) chats to you, '(.*)'\\s*", Pattern.DOTALL).matcher("");

	public void processCommand(String content) {

		//String trimmed = content.trim();	//does this trim off escape characters?
		//matcherChatPrivate.reset(trimmed);
		matcherChatPrivate.reset(content);

		if (!matcherChatPrivate.find()) {
			Logger.getLogger("global").info("Invalid private chat syntax from " + getName());
			System.out.println(content + NRM);

			return;
		}

		String clientName = ANSIColor.strip(matcherChatPrivate.group(1)).toLowerCase();
		String myName = ANSIColor.strip(getName()).toLowerCase();

		//Make sure incoming chat name matches the name of the client sending the command
		if (!myName.equals(clientName)) {
			Logger.getLogger("global").info(String.format("\tClient names do not match!  %s / %s", myName, clientName));
			return;
		}

		//Treat this input as a password if the client is not yet authenticated
		if (!isAuthenticated()) {
			//User supplied a password, check if it matches

			ChatAccount account = getAccount();

			//System.out.printf("comparing '%s' to '%s'\n", matcher.group(2), account.getPassword());

			String pwStr = matcherChatPrivate.group(2);

			BCrypt.Result result = BCrypt.verifyer().verify(pwStr.toCharArray(), account.getPassword().toCharArray());

			if (result.verified) {
				setAuthenticated(true, "Password Auth");

			} else {
				serverChat("Incorrect Password");

				//Logger.getGlobal().info("account password hash:");
				//Logger.getGlobal().info(account.getPassword());
				//Logger.getGlobal().info("hash result:");
				//Logger.getGlobal().info(bcryptHashString);

				getProtocol().closeSocket();
			}

			return;
		}

		//log.info(String.format("CMD from %s: '%s'", matcher.group(1), matcher.group(2)));

		//Split off the actual command and arguments to it
		String[] args = matcherChatPrivate.group(2).split(" ", 2);

		CommandEntry cmdEntry = ChatServer.getCommand(args[0]);

		if (cmdEntry == null || cmdEntry.level > getAccount().getLevel()) {
			serverChat(String.format("You idiot!  There is no <%s> command!", args[0]));

		} else {
			//instance.stats.cmds++;
			ChatServer.instance.stats.cmds++;

			cmdEntry.cmd.execute(this, args);
		}
	}
}
