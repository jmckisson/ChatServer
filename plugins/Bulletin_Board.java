//
//  BulletinBoard.java
//  ChatServer
//
//  Created by John McKisson on 4/3/08.
//  Copyright 2008 __MyCompanyName__. All rights reserved.
//

import java.io.*;
import java.text.DateFormat;
import java.util.logging.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import com.presence.chat.*;
import com.presence.chat.commands.Command;
import com.presence.chat.plugin.*;

import com.thoughtworks.xstream.*;


import static com.presence.chat.ANSIColor.*;

public class Bulletin_Board implements ChatPlugin {
	
	static final Pattern postPattern = Pattern.compile("^'([A-Za-z!?,#@ ]+)' (.*)$");
	
	static final String FILEPATH = "board.xml";
	
	public void register() {
	
		XStream xs = PluginManager.getXStream();
		
		xs.alias("entry", BoardEntry.class);
		
		xs.useAttributeFor(BoardEntry.class, "author");
		xs.useAttributeFor(BoardEntry.class, "date");
	
		ChatServer.addCommand("board", new CMDBoard(), 1);
		
		try {
			loadEntries();
		} catch (FileNotFoundException e) {
			//TODO: clean this up
			e.printStackTrace();
		}
	}
	
	public String name() {
		return "Bulletin Board";
	}
	
	static Vector<BoardEntry> entries;
	
	static final String HEADER =	String.format("%s                              %s BULLETIN BOARD\n" +
									"%s[ %s# %s][   %sDate   %s][       %sAuthor%s][%sSubject                                      %s]\n" + 
									"%s ---  ----------  -------------  ---------------------------------------------%s",
									WHT, ChatPrefs.getName().toUpperCase(), BLU, WHT, BLU, WHT, BLU, WHT, BLU, WHT, BLU, WHT, RED);
	
	
	/**
	 * Deserialize the bulletin board
	 */
	public void loadEntries() throws FileNotFoundException {
		
		try {
			entries = (Vector<BoardEntry>)PluginManager.loadRecords(FILEPATH);
			
			if (entries == null)
				entries = new Vector<BoardEntry>();
				
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		/*
		boolean exists = new File(FILEPATH).exists();
		
		if (!exists) {
			entries = new Vector<BoardEntry>();
			return;
		}
			
		BufferedReader in = new BufferedReader(new FileReader(FILEPATH));
		StringBuffer xml = new StringBuffer();
		
		String str;
		try {
			while ((str = in.readLine()) != null) {
				xml.append(str);
			}
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		entries = (Vector<BoardEntry>)xstream.fromXML(xml.toString());
		*/
		
		Logger.getLogger("global").info("Bulletin Board loaded");
	}
	
	
	/**
	 * Serialize the bulletin board entries
	 */
	public void saveEntries() {
	
		try {
			PluginManager.saveRecords(FILEPATH, entries);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		/*
		String xml = xstream.toXML(entries);
		
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
		*/
		
		Logger.getLogger("global").info("Bulletin Board saved");
	}
	
	/**
	 * Add an entry to the top of the board.
	 * @param entry Entry to be added.
	 */
	public static void addEntry(BoardEntry entry) {
		entries.add(0, entry);
	}
	
	/**
	 * Get the index number of a BoardEntry on the board.
	 * @param entry Specified entry.
	 * @return Index of the entry.
	 */
	public static int indexOf(BoardEntry entry) {
		return entries.indexOf(entry, 0);
	}

	
	public static String getHeader() {
		return HEADER;
	}
	
	
	class CMDBoard implements Command {

		public boolean execute(ChatClient sender, String[] args) {
			if (args.length < 2) {	//Command has no arguments, just list the board entries to the client
				showBoard(sender);
				return true;
			}
			
			String[] boardArgs = args[1].split(" ", 2);	//Split off first argument (read/post/remove)
			
			if (boardArgs.length <= 1) {
				sender.sendChat(usage());
				return true;
			}
			
			String cmd = boardArgs[0].toLowerCase();
			
			if (cmd.equals("post")) {	//Post command requires postPattern to be matched
				
				Matcher matcher = postPattern.matcher(boardArgs[1]);
				
				String subject = null, content = null;
				
				if (matcher.find()) {
					subject = matcher.group(1);
					content = matcher.group(2);

				} else
					sender.sendChat("Invalid Syntax");
				
				if (subject != null && content != null) {
					BoardEntry entry = new BoardEntry(sender, subject, content);
					addEntry(entry);
					saveEntries();
					
					ChatServer.echo(String.format("%s[%s%s%s] %s%s%s posts a note on the board",
						RED, WHT, ChatPrefs.getName(), RED, WHT, sender.getName(), RED));
				}
			
			} else if (cmd.equals("read")) {	//Read command requires a number
				//TODO: if not a number, argument is a keyword
				
				int idx = 0;
				try {
					idx = Integer.parseInt(boardArgs[1]) - 1;
				} catch (NumberFormatException e) {
					sender.sendChat("Invalid Syntax");
				}
				
				if (idx >= 0 && idx < entries.size()) {
					BoardEntry entry = entries.get(idx);
					
					StringBuilder strBuf = new StringBuilder(getHeader() + "\n");
					
					strBuf.append(entry.getHeader() + "\n");
					strBuf.append(NRM + "\n" + entry.getContent() + "\n");
					
					sender.sendChat(strBuf.toString());
					
				} else {
					sender.sendChat("There is no post at that index!");
				}
				
			
			} else if (cmd.equals("remove")) {	//Remove command requires a number
				//We can remove someones post if we are the author, or if we're level 5
				
				int idx = 0;
				try {
					idx = Integer.parseInt(boardArgs[1]) - 1;
				} catch (NumberFormatException e) {
					sender.sendChat("Invalid Syntax");
				}
				
				if (idx >= 0 && idx < entries.size()) {
					BoardEntry entry = entries.get(idx);
					
					ChatAccount senderAccount = sender.getAccount();

					if (senderAccount.getName().equals(entry.getAuthor()) || senderAccount.getLevel() == 5) {
						entries.remove(entry);
						saveEntries();
						
						ChatServer.echo(String.format("%s[%s%s%s] %s%s%s removes a note from the board",
							RED, WHT, ChatPrefs.getName(), RED, WHT, sender.getName(), RED));
					
					} else {
						sender.sendChat("You are not allowed to do that!");
					}
					
				} else {
					sender.sendChat("There is no post at that index!");
				}
				
				
			} else {
				sender.sendChat("Invalid Syntax, board post 'subject' message");
			}
			
			return true;
		}
		
		
		public String help() {
			return "Interact with the Bulletin Board";
		}
		
		public String usage() {
			return String.format(ChatServer.USAGE_STRING, "board [read <#> | post '<title>' <msg> | remove <#>]");
		}
		
		void showBoard(ChatClient sender) {
			//TODO: Change this to a StringBuilder
			String outStr = String.format("\n%s\n", getHeader());
			
			Iterator<BoardEntry> it = entries.iterator();
			
			while (it.hasNext()) {
				BoardEntry entry = it.next();
			
				outStr = String.format("%s%s\n", outStr, entry.getHeader());
			}
			
			sender.sendChat(outStr);
		}
	}
	
}
