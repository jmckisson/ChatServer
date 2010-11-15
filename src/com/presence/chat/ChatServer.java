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
import com.presence.chat.plugin.*;
import com.presence.chat.socket.*;

import static com.presence.chat.ANSIColor.*;

public class ChatServer {
	
	static ChatServer instance;
	
	public static ChatServer getServer() {
		return instance;
	}

	static List<ChatClient> clients = new ArrayList<ChatClient>();
	static Hashtable<String, ChatRoom> rooms = new Hashtable<String, ChatRoom>();
	
	static Hashtable<String, CommandEntry> commands;
	
	javax.swing.Timer spamTimer = null;
	javax.swing.Timer shutdownTimer = null;

	
	static {
		commands = new Hashtable<String, CommandEntry>();
	
		//Level 0 commands
		commands.put("ca",		new CommandEntry(new CMDChatAll(), 0));
		commands.put("compact", new CommandEntry(new CMDCompact(), 0));
		commands.put("help",	new CommandEntry(new CMDHelp(), 0));
		commands.put("join",	new CommandEntry(new CMDJoinRoom(), 0));
		commands.put("log",		new CommandEntry(new CMDLog(), 0));
		commands.put("motd",	new CommandEntry(new CMDMOTD(), 0));
		commands.put("plugins",	new CommandEntry(new CMDPlugins(), 0));
		commands.put("pmsg",	new CommandEntry(new CMDChatPrivate(), 0));
		commands.put("who",		new CommandEntry(new CMDWho(), 0));
		commands.put("stats",	new CommandEntry(new CMDStats(), 0));
	
		//Level 1 commands
		commands.put("ccreate",	new CommandEntry(new CMDCreateRoom(), 1));
		commands.put("chpw",	new CommandEntry(new CMDPassword(), 1));
		commands.put("plog",	new CommandEntry(new CMDMessageLog(), 1));
		//info Get detailed info on a user
		//setip Enable/reset ipauth feature
		
		//Level 2 commands
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
		//ban Ban a username or IP address
		//unban
		//banlist
		//gag Gag a user for specified time
		
		//Level 5 commands
		commands.put("accounts",new CommandEntry(new CMDAccounts(), 5));
		commands.put("del",		new CommandEntry(new CMDDelete(), 5));
		commands.put("setname", new CommandEntry(new CMDSetName(), 5));
		commands.put("shutdown",new CommandEntry(new CMDShutdown(), 5));
		//commands.put("wl",		new CommandEntry(new CMDWriteLog(), 5));
	}
	
	public static void addCommand(String str, Command cmd, int level) {
		commands.put(str, new CommandEntry(cmd ,level));
	}
	

	public static final String VERSION = "jChatServ by Humera, " + ChatServer.class.getPackage().getImplementationVersion();
	
	public static String USAGE_STRING = ChatPrefs.getName() + " chats to you, 'Usage: /chat " + ChatPrefs.getName() + " %s'";
	
	//Use the same ByteBuffer for all channels.  A single thread is servicing all the channels
	//so there is no danger of concurrent access
	//public static ByteBuffer BUF = ByteBuffer.allocateDirect(40960);	//40k buffer

	
	//Selector mainSelector;
	
	boolean keepRunning = true;
	
	public PluginManager pManager;
	
	ServerStats stats;

	public ChatServer() throws Exception {
	
		instance = this;
		
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
		
		AccountManager.saveAccounts();
		
		while (clients.size() > 0)
			disconnectClient(clients.get(0));
		
		telnetServer.shutdown();
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
	
	/*
	void mainLoop() throws Exception {
		//Allocate an unbound server socket channel
		ServerSocketChannel serverChannel = ServerSocketChannel.open();
		
		ServerSocket serverSocket = serverChannel.socket();
		
		mainSelector = Selector.open();
		
		try {
			serverSocket.bind(new InetSocketAddress(ChatPrefs.getPort()));
		} catch (BindException e) {
			Logger.getLogger("global").severe("Address already bound, restarting in 5 seconds");
			serverSocket = null;
			Thread.sleep(5000);
			return;
		}
		
		serverChannel.configureBlocking(false);
		
		//Register selector with ssc
		serverChannel.register(mainSelector, SelectionKey.OP_ACCEPT);
		
		Logger.getLogger("global").info("Server ready, listening on port " + ChatPrefs.getPort());
		
		while (keepRunning) {

			//I'm using select without a timeout here to avoid additional overhead
			//The selector is an instance member so we're able to wakeup() externally
			int n = mainSelector.select();
			
			if (n == 0) continue;
			
			Iterator it = mainSelector.selectedKeys().iterator();
			
			while (it.hasNext()) {
				SelectionKey key = (SelectionKey)it.next();
				
				//Invalid key, connection dropped or something
				if (!key.isValid()) {
					SocketChannel socketChannel = (SocketChannel)key.channel();
		
					ChatClient theClient = clients.get(socketChannel);
					
					if (theClient == null) {
						Logger.getLogger("global").warning("Something bad happened trying to read data from a client that doesnt exist!");
						socketChannel.close();
					} else {
						Logger.getLogger("global").warning("Closing socket with invalid key");
						theClient.disconnect();
					}
				}
				
				//New connection
				if (key.isAcceptable()) {
					ServerSocketChannel server = (ServerSocketChannel)key.channel();
					
					SocketChannel channel = server.accept();
					
					registerClient(mainSelector, channel, SelectionKey.OP_READ);
				}
				
				//New data
				if (key.isReadable()) {
					readDataFromSocket(key);
				}
				
				//Done with this key
				it.remove();
			}
		}
		
		serverChannel.close();
	}
	*/
	
	/*
	void registerClient(Selector selector, SocketChannel channel, int ops) throws Exception {
		if (channel == null) return;
		
		channel.configureBlocking(false);
		
		channel.register(selector, ops);	//Regsiter with selector
		
		clients.put(channel, new ChatClient(channel));
	}
	*/
	
	/**
	 * Closes a clients socket, removes them from the client list, and logs their disconnect
	 */
	static public void disconnectClient(ChatClient client) {
		Channel sock = client.getSocket();
		
		if (sock != null) {
			
			sock.close();
		}
		
		clients.remove(client);
	}

	/*
	void readDataFromSocket(SelectionKey key) throws Exception {
		SocketChannel socketChannel = (SocketChannel)key.channel();
		
		ChatClient theClient = clients.get(socketChannel);
		
		if (theClient == null) {
			Logger.getLogger("global").warning("Something bad happened trying to read data from a client that doesnt exist!");
			socketChannel.close();
			return;
		}
		
		BUF.clear();
		
		try {
			int numBytesRead = socketChannel.read(BUF);
			
			if (numBytesRead == -1) {
				//Close channel on EOF, this invalidates the key
				
				Logger.getLogger("global").warning("EOF on socket");
				theClient.disconnect();
			
			} else {
				BUF.flip();
				
				theClient.processIncomingData();
			}

		} catch (IOException e) {
			//Ok, we don't know what happened here...  This seems to be disconnecting clients a lot
			Logger.getLogger("global").warning("Client Exception");
			e.printStackTrace();
			
			//theClient.disconnect();
		}
	}
	*/
	
	
	public static ServerStats getStats() {
		return instance.stats;
	}
	
	
	/**
	 * Process a server command from a client
	 * @param myClient Client that sent the personal msg
	 * @param content Contents of the msg
	 */
	static final Matcher matcherChatPrivate = Pattern.compile("^(.*) chats to you, '(.*)'").matcher("");
	 
	public static void processCommand(ChatClient client, String content) {
		
		matcherChatPrivate.reset(content.trim());
		
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
				client.sendChat(String.format("%s chats to you, 'Incorrect Password'", ChatPrefs.getName()));
				
				client.getProtocol().closeSocket();	
			}
			
			return;
		}
		
		//log.info(String.format("CMD from %s: '%s'", matcher.group(1), matcher.group(2)));
		
		//Split off the actual command and arguments to it
		String[] args = matcherChatPrivate.group(2).split(" ", 2);
		
		CommandEntry cmdEntry = getCommand(args[0]);
		
		if (cmdEntry == null || cmdEntry.level > client.getAccount().getLevel()) {
			client.sendChat(String.format("%s chats to you, 'You idiot!  There is no <%s> command!'", ChatPrefs.getName(), args[0]));
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
		Iterator<ChatClient> it = clients.iterator();
		
		while (it.hasNext()) {
			ChatClient c = it.next();
			
			String name = c.getName();
			
			if (name != null && name.equalsIgnoreCase(testName) && !name.equalsIgnoreCase(skip)) {
				return true;
			}
		}
		
		return false;
	}
	
	
	public static ChatClient getClientByName(String name) {
		
		for (Iterator<ChatClient> it = getClients().iterator() ; it.hasNext() ;) {
			ChatClient targ = it.next();
			
			if (targ.getName() == null) continue;
			
			if (targ.getName().equalsIgnoreCase(name))
				return targ;
				
		}
		
		return null;
	}
	
	
	public static List<ChatClient> getClients() {
		return clients;
	}
	
	public static ChatRoom getRoom(String name) {
		return rooms.get(name.toLowerCase());
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
	
	public static void echo(String msg) {
		
		for (ChatClient c : clients) {
			c.sendChatAll(msg);
		}
		
		Logger.getLogger("global").info(msg.replaceAll("\u001b\\[[0-9;]+m", ""));
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
	


    public static void main (String args[]) throws Exception {
		
		//Setup logging facilities
		LogManager logManager = LogManager.getLogManager();
        logManager.reset();
		
		Logger log = Logger.getLogger("global");
		
		log.setUseParentHandlers(false);
		
		ChatLogFormatter clf = new ChatLogFormatter();
		
		Handler consoleHandler = new ConsoleHandler();
		consoleHandler.setFormatter(clf);
		log.addHandler(consoleHandler);
		
		
		//Make sure log directory exists
		boolean diskLog = false;
		try {
			File logDir = new File("log/");
			if (logDir.exists() || logDir.mkdir())
				diskLog = true;
		} catch (SecurityException e) {
			log.warning("Permission denied trying to create log directory");
		}
		
		if (diskLog) {
			// log file max size 10K, 6 rolling files, append-on-open
			Handler fileHandler = new FileHandler("log/syslog%g.log", 10000000, 6, true);
			fileHandler.setFormatter(clf);
			log.addHandler(fileHandler);
		}
		
		//Redirect stdout and stderr to our logger
        System.setOut(new PrintStream(new LoggingOutputStream(log, StdOutErrLevel.STDOUT), true));
        System.setErr(new PrintStream(new LoggingOutputStream(log, StdOutErrLevel.STDERR), true));
		
		//Parse arguments
		parseArgs(args);
		
		try {
			new ChatServer();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
}
