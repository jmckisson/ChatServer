//
//  ChatServer.java
//  ChatServer
//
//  Created by John McKisson on 3/31/08.
//  Copyright (c) 2008 __MyCompanyName__. All rights reserved.
//
package com.presence.chat;

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.logging.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.awt.event.*;

import org.jboss.netty.channel.Channel;

import com.presence.chat.commands.*;
import com.presence.chat.log.*;
import com.presence.chat.plugin.*;
import com.presence.chat.socket.*;

import com.webobjects.foundation.NSNotificationCenter;

import static com.presence.chat.ANSIColor.*;

public class ChatServer {
	
	static ChatServer instance;
	
	public static ChatServer getServer() {
		return instance;
	}

	static List<ChatClient> clients = new ArrayList<ChatClient>();
	static Hashtable<String, ChatRoom> rooms = new Hashtable<String, ChatRoom>();
	
	static Hashtable<String, CommandEntry> commands;
	
	static List<String> banList;
	
	javax.swing.Timer spamTimer = null;
	javax.swing.Timer shutdownTimer = null;
	
	//should these be static?
	static ChatLogHandler	slHandler;
	static Logger			sysLog;
			
	public static void loadCommands() {
		commands = new Hashtable<String, CommandEntry>();
	
		//Level 0 commands
		commands.put("ca",		new CommandEntry(new CMDChatAll(), 0));
		commands.put("compact", new CommandEntry(new CMDCompact(), 0));
		commands.put("help",	new CommandEntry(new CMDHelp(), 0));
		
		commands.put("log",		new CommandEntry(new CMDLog(), 0));
		commands.put("motd",	new CommandEntry(new CMDMOTD(), 0));
		commands.put("plugins",	new CommandEntry(new CMDPlugins(), 0));
		commands.put("pmsg",	new CommandEntry(new CMDChatPrivate(), 0));
		commands.put("who",		new CommandEntry(new CMDWho(), 0));
	
		//Level 1 commands
		commands.put("chpw",	new CommandEntry(new CMDPassword(), 1));
		commands.put("join",	new CommandEntry(new CMDJoinRoom(), 1));
		commands.put("plog",	new CommandEntry(new CMDMessageLog(), 1));
		//info Get detailed info on a user
		//setip Enable/reset ipauth feature
		
		//Level 2 commands
		commands.put("acinfo",	new CommandEntry(new CMDAccountInfo(), 2));
		commands.put("ignore",	new CommandEntry(new CMDIgnore(), 2));
		commands.put("listen",	new CommandEntry(new CMDListen(), 2));
		//commands.put("gca",		new CMDGlobalChatAll());
		
		//Level 3 commands
		commands.put("add",		new CommandEntry(new CMDAdd(), 3));
		commands.put("auth",	new CommandEntry(new CMDAuth(), 3));
		commands.put("fjoin",	new CommandEntry(new CMDForceJoin(), 3));
		commands.put("silent",	new CommandEntry(new CMDSilent(), 3));
		//commands.put("snoop",	new CommandEntry(new CMDSnoop(), 3));
		//remove Force removal of a channel
		
		//Level 4 commands
		commands.put("kick",	new CommandEntry(new CMDKick(), 4));
		commands.put("setlvl",	new CommandEntry(new CMDSetLevel(), 4));
		commands.put("setpw",	new CommandEntry(new CMDSetPassword(), 4));

		//gag Gag a user for specified time
		
		//Level 5 commands
		commands.put("accounts",new CommandEntry(new CMDAccounts(), 5));
		commands.put("del",		new CommandEntry(new CMDDelete(), 5));
		commands.put("info",	new CommandEntry(new CMDStats(), 5));
		commands.put("ipban",	new CommandEntry(new CMDIPBan(), 5));
		commands.put("load",	new CommandEntry(new CMDLoadPlugin(), 5));
		commands.put("setname", new CommandEntry(new CMDSetName(), 5));
		commands.put("shutdown",new CommandEntry(new CMDShutdown(), 5));
		commands.put("spoof",	new CommandEntry(new CMDSpoof(), 5));
		commands.put("syslog",	new CommandEntry(new CMDSyslog(), 5));
		commands.put("reload",	new CommandEntry(new CMDReloadPlugins(), 5));
		
		//commands.put("wl",		new CommandEntry(new CMDWriteLog(), 5));
	}
	
	public static void addCommand(String str, Command cmd, int level) {
		commands.put(str, new CommandEntry(cmd ,level));
	}


	public static final String VERSION = "jChatServ by Humera, " + ChatServer.class.getPackage().getImplementationVersion();
	
	public static String USAGE_STRING = "Usage: /chat " + ChatPrefs.getName() + " %s";
	
	public static String HEADER = String.format("%s%s[%s%s%s] ", BLD, RED, WHT, ChatPrefs.getName(), RED);
	
	boolean keepRunning = true;
	
	public PluginManager pManager;
	
	ServerStats stats;

	public ChatServer() throws Exception {
	
		instance = this;
		
		loadCommands();
		
		//Ban list
		String banString = ChatPrefs.getPref("BanList", "");
		System.out.println(banString);
		if (!banString.equals("")) {
			String[] banArray = banString.substring(1, banString.length() - 1).split(", ");
		
			banList = new ArrayList(Arrays.asList(banArray));
		
			Logger.getLogger("global").info("Loaded " + banList.size() + " banned IPs");
		} else
			banList = new ArrayList<String>();
		
		stats = new ServerStats();
		
		//Create default chat room
		ChatRoom mainRoom = new ChatRoom("main", null, 0);
		mainRoom.setDestroyable(false);
		
		rooms.put("main", mainRoom);

		AccountManager.loadAccounts();
		
		pManager = new PluginManager();
		pManager.installPlugins();
		
		initSpamTimer();
		
		TelnetServer telnetServer = new TelnetServer(ChatPrefs.getPort());
		
		while (keepRunning) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
			}
		}
		
		Logger.getLogger("global").info("Serving shutting down");
		
		NSNotificationCenter.defaultCenter().postNotification("ServerShutdown", null);
		
		AccountManager.saveAccounts();
		
		while (clients.size() > 0)
			disconnectClient(clients.get(0));
		
		telnetServer.shutdown();
	}
	
	public void reloadPlugins() {
		loadCommands();
		pManager.reloadPlugins();
	}
	
	public void loadPlugin(String pName) {
		pManager.installUserPlugin(pName);
	}
	
	
	void initSpamTimer() {
		//Set up spam timer to reset spam counters every 2 seconds
		spamTimer = new javax.swing.Timer(2000, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				for (ChatClient client : clients)
					client.spamThresh = 0;
					
				spamTimer.start();
			}
		});
		spamTimer.setRepeats(false);
		spamTimer.start();
	}
	
	
	static public void addClient(ChatClient client) {
		synchronized (clients) {
			clients.add(client);
		}
	}
	
	/**
	 * Closes a clients socket, removes them from the client list, and logs their disconnect
	 */
	static public void disconnectClient(ChatClient client) {
		Channel sock = client.getSocket();
		
		if (sock != null) {
			
			sock.close();
		}
		
		synchronized (clients) {
			clients.remove(client);
		}
	}
	
	public static List<String> banList() {
		return banList;
	}
	
	public static void addBan(String ip) {
		banList.add(ip);
		String banString = Arrays.toString(banList.toArray());
		System.out.println(banString);
		ChatPrefs.setPref("BanList", banString);
	}
	
	public static void removeBan(String ip) {
		banList.remove(ip);
		String banString = Arrays.toString(banList.toArray());
		System.out.println(banString);
		ChatPrefs.setPref("BanList", banString);
	}
	
	public static ServerStats getStats() {
		return instance.stats;
	}
	
	public static NSNotificationCenter getNotificationCenter() {
		return NSNotificationCenter.defaultCenter();
	}
	
	
	/**
	 * Process a server command from a client
	 * @param myClient Client that sent the personal msg
	 * @param content Contents of the msg
	 */
	static final Matcher matcherChatPrivate = Pattern.compile("(.*) chats to you, '(.*)'", Pattern.DOTALL).matcher("");
	 
	public static void processCommand(ChatClient client, String content) {
		
		String trimmed = content.trim();
		
		matcherChatPrivate.reset(trimmed);
		
		if (!matcherChatPrivate.find()) {
			Logger.getLogger("global").info("Invalid private chat syntax from " + client.getName());
			System.out.println(content + NRM);
			
			return;
		}
		
		String clientName = matcherChatPrivate.group(1).toLowerCase();

		//Make sure incoming chat name matches the name of the client sending the command
		if (!client.getName().toLowerCase().equals(clientName)) {
			Logger.getLogger("global").info(String.format("\tClient names do not match!  %s / %s", client.getName(), matcherChatPrivate.group(1)));
			return;
		}
			
		//Treat this input as a password if the client is not yet authenticated
		if (!client.isAuthenticated()) {
			//User supplied a password, check if it matches
				
			ChatAccount account = client.getAccount();
			
			//System.out.printf("comparing '%s' to '%s'\n", matcher.group(2), account.getPassword());
			
			String cryptedPassword = JCrypt.crypt("", matcherChatPrivate.group(2));
			
			if (cryptedPassword.equals(account.getPassword())) {
				client.setAuthenticated(true, "Password Auth");
				
			} else {
				client.serverChat("Incorrect Password");
				
				client.getProtocol().closeSocket();	
			}
			
			return;
		}
		
		//log.info(String.format("CMD from %s: '%s'", matcher.group(1), matcher.group(2)));
		
		//Split off the actual command and arguments to it
		String[] args = matcherChatPrivate.group(2).split(" ", 2);
		
		CommandEntry cmdEntry = getCommand(args[0]);
		
		if (cmdEntry == null || cmdEntry.level > client.getAccount().getLevel()) {
			client.serverChat(String.format("You idiot!  There is no <%s> command!", args[0]));
			return;
		} else {
			instance.stats.cmds++;
			
			cmdEntry.cmd.execute(client, args);
		}
	}
	
	
	/**
	 * Get a named command entry
	 * @param cmd Command name
	 * @return CommandEntry named by name, null if no entry exists
	 */
	public static CommandEntry getCommand(String cmd) {
		return commands.get(cmd);
	}
	
	public static Hashtable<String, CommandEntry> getCommands() {
		return commands;
	}
	
	
	/**
	 * @return true if the specified person is online, false otherwise
	 */
	public static boolean checkOnlineName(String testName, String skip) {
		synchronized (clients) {
			Iterator<ChatClient> it = clients.iterator();
			
			while (it.hasNext()) {
				ChatClient c = it.next();
				
				String name = c.getName();
				
				if (name != null && name.equalsIgnoreCase(testName) && !name.equalsIgnoreCase(skip)) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	
	public static ChatClient getClientByName(String name) {
		
		synchronized (clients) {
			for (Iterator<ChatClient> it = getClients().iterator() ; it.hasNext() ;) {
				ChatClient targ = it.next();
				
				if (targ.getName() == null) continue;
				
				if (targ.getName().equalsIgnoreCase(name))
					return targ;
					
			}
		}
		
		return null;
	}
	
	
	public static List<ChatClient> getClients() {
		return clients;
	}
	
	public static ChatRoom getRoom(String name) {
		return getRoom(new String[] {name});
	}
	
	/**
	 * Finds a room by name arg[0]
	 * If that room doesn't exist we create it using password/minLevel arguments
	 */
	public static ChatRoom getRoom(String[] args) {
		String roomName = args[0].toLowerCase();
		String password = null;
	
		ChatRoom r = rooms.get(roomName);
		if (r == null) {
			int minLevel = 0;
		
			//Check if a password has been supplied
			
			if (args.length == 2) {
				//args[1] is either the password or minLevel
				try {
					minLevel = Integer.parseInt(args[1]);
				} catch (NumberFormatException e) {
					//Wasnt a number, so its the password
					password = args[1];
				}
			} else if (args.length == 3) {
				//Password is second argument
				password = args[1];
				
				//MinLevel is 3rd argument
				try {
					minLevel = Integer.parseInt(args[2]);
				} catch (NumberFormatException e) {}
			}
			
			minLevel = Math.min(minLevel, 5);
			minLevel = Math.max(minLevel, 0);
			
			r = new ChatRoom(roomName, password, minLevel);
			
			rooms.put(roomName, r);
		}
		
		return r;
	}
	
	public static Hashtable<String, ChatRoom> getRooms() {
		return rooms;
	}
	
	
	/**
	 * Echo a message to everyone in all rooms
	 * @param msg Message to be displayed
	 */
	/*
	public static void echo(String msg) {
		Enumeration<ChatRoom> e = rooms.elements();
		
		while (e.hasMoreElements()) {
			ChatRoom room = e.nextElement();
			
			room.echo(msg, null);
		}
	}
	*/
	
	public static String makeANSI(String str) {
		str = str.replace("&k", BLK).replace("&r", RED).replace("&g", GRN).replace("&y", YEL).replace("&b", BLU).replace("&m", MAG)
					.replace("&c", CYN).replace("&w", WHT).replace("&N", NRM).replace("&B", BLD);
		
		return str;
	}
	
	public static void echo(String msg) {
		
		for (ChatClient c : clients) {
			c.sendChatAll(msg);
		}
		
		//Logger.getLogger("global").info(ANSIColor.strip(msg));
	}
	
	
	public static void shutdown(int seconds) {
		instance._shutdown(seconds);
	}
	
	void _shutdown(int seconds) {

		if (shutdownTimer != null)
			shutdownTimer = null;
			
		shutdownTimer = new javax.swing.Timer(seconds * 1000, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				echo(String.format("%s[%s%s%s] Server %sRestart%s",
					RED, WHT, ChatPrefs.getName(), RED, YEL, RED));
			
				keepRunning = false;
		
				//mainSelector.wakeup();
			}
		});
		
		shutdownTimer.setRepeats(false);
		shutdownTimer.start();
	}
	
	
	static void parseArgs(String args[]) {
		int n = 0;
		
		while (args.length >= n + 1) {
		
			if (args[n + 1] != null /*&& !args[n + 1].startsWith("-")*/) {
			
				if (args[n].equals("-p")) {
					ChatPrefs.setPort(Integer.parseInt(args[n + 1]));
					
				} else if (args[n].equals("-n")) {
					ChatPrefs.setName(args[n + 1]);
				}

			} else
				break;
			
			n += 2;
		}
	}
	

	public static void printStringBytes(String str) {
		StringBuilder strBuf = new StringBuilder();
		byte[] b = str.getBytes();
		for (int i = 0; i < b.length; i++)
			strBuf.append(String.format("%02X ", b[i]));
			
		System.out.println(strBuf.toString() + "\n");
	}
	
	public static ChatLogHandler getSystemLog() {
		return slHandler;
	}

    public static void main (String args[]) throws Exception {
		
		//Setup logging
		LogManager logManager = LogManager.getLogManager();
		logManager.reset();
			
		sysLog = Logger.getLogger("global");
			
		sysLog.setUseParentHandlers(false);
		
		SystemLogFormatter slf = new SystemLogFormatter();
		
		Handler consoleHandler = new ConsoleHandler();
		consoleHandler.setFormatter(slf);
		sysLog.addHandler(consoleHandler);
		
		//Install memoryhandler so we can grep system log
		slHandler = new ChatLogHandler(10000);
		slHandler.setFormatter(slf);
		sysLog.addHandler(slHandler);

		//Make sure log directory exists
		boolean diskLog = false;
		try {
			File logDir = new File("log/");
			if (logDir.exists() || logDir.mkdir())
				diskLog = true;
		} catch (SecurityException e) {
			sysLog.warning("Permission denied trying to create log directory");
		}
		
		if (diskLog) {
			Handler fileHandler = new FileHandler("log/syslog%g.log", 5000000, 10, true);
			fileHandler.setFormatter(slf);
			sysLog.addHandler(fileHandler);
		}
		
		//Redirect stdout and stderr to our logger
        System.setOut(new PrintStream(new LoggingOutputStream(sysLog, StdOutErrLevel.STDOUT), true));
        System.setErr(new PrintStream(new LoggingOutputStream(sysLog, StdOutErrLevel.STDERR), true));
		
		//Parse arguments
		parseArgs(args);
		
		try {
			new ChatServer();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
}
