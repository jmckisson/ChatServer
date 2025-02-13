//
//  Alchemy_Helper.java
//  ChatServer
//
//  Created by John McKisson on 4/3/08.
//  Copyright 2008 __MyCompanyName__. All rights reserved.
//
import java.io.*;
import java.util.*;
import java.util.logging.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import com.presence.chat.*;
import com.presence.chat.commands.Command;
import com.presence.chat.plugin.*;

public class Alchemy_Helper implements ChatPlugin {
	private static final Logger log = Logger.getLogger("global");
	
	
	static Hashtable<String, String> entries;
	
	static final String FILEPATH = "alchemy.xml";
	
	static final Pattern setPattern = Pattern.compile("^'([A-Za-z', ]+)' (.*)$");


	public void register() {
		ChatServer.addCommand("herbs", new CMDHerb(), 3);
		ChatServer.addCommand("herbset", new CMDHerbSet(), 3);
		
		try {
			loadEntries();
		} catch (FileNotFoundException e) {
			//TODO: clean this up
			e.printStackTrace();
		}
	}
	
	public String name() {
		return "Alchemy Helper";
	}
	
	/**
	 * Deserialize the bulletin board
	 */
	public void loadEntries() throws FileNotFoundException {
		
		try {
			entries = (Hashtable<String, String>)PluginManager.loadRecords(FILEPATH);
			
			if (entries == null)
				entries = new Hashtable<String, String>();
				
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		/*
		boolean exists = new File(FILEPATH).exists();
		
		if (!exists) {
			entries = new Hashtable<String, String>();
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
		
		entries = (Hashtable<String, String>)xstream.fromXML(xml.toString());
		*/
		
		log.info("Alchemy Database loaded");
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
		
		log.info("Alchemy Database saved");
	}
	
	/**
     * Quick sort the given chunk of the array in place.
     * @param array The array to sort.
     * @param lo0 The low bound of the chunk of the array to sort.
     * @param hi0 The high bound of the array to sort.
     */
    static void quickSortStringArray(String array[], int lo0, int hi0) {
        int lo = lo0 ;
        int hi = hi0 ;
        String mid = null ;

        if ( hi0 > lo0 ) {
            mid = array[(lo0+hi0)/2] ;
            while (lo <= hi) {
                while ((lo < hi0) && (array[lo].compareTo(mid) < 0)) 
                    ++lo ;
                while ((hi > lo0) && (array[hi].compareTo(mid) > 0))
                    --hi ;
                if ( lo <= hi ) {
                    String tmp = array[lo] ;
                    array[lo]  = array[hi] ;
                    array[hi]  = tmp ;
                    ++lo ;
                    --hi ;
                }
            }
            if ( lo0 < hi )
                quickSortStringArray(array, lo0, hi) ;
            if ( lo < hi0 )
                quickSortStringArray(array, lo, hi0) ;
        }
    }
	
	/**
     * Sort the given String array in place.
     * @param array The array of String to sort.
     * @param inplace Sort the array in place if <strong>true</strong>, 
     *    allocate a fresh array for the result otherwise.
     * @return The same array, with string sorted.
     */
    public static String[] sortStringArray(String array[], boolean inplace) {
        String tosort[] = array ;
        if ( ! inplace ) {
            tosort = new String[array.length] ;
            System.arraycopy(array, 0, tosort, 0, array.length) ;
        }
        quickSortStringArray(tosort, 0, tosort.length-1) ;
        return tosort ;
    }
	
	
	public String herbString(String[] herbs) {
		//Remove spaces
		for (int i = 0; i < herbs.length; i++)
			herbs[i] = herbs[i].replace(" ", "");
		
		//Ok now we have a list of herb combinations that needs to be sorted
		sortStringArray(herbs, true);
		
		//Now combine all arguments together
		StringBuffer key = new StringBuffer();
		
		for (String s : herbs)
			key.append(s);

		return key.toString();
	}
	
	
	class CMDHerb implements Command {
		public String help() {
			return "Info About Alchemy Recipes";
		}
		
		public String usage() {
			return String.format(ChatServer.USAGE_STRING, "herbs [<name1> <#1>, <name2> <#2>, ... <nameN> <#N>]");
		}
		
		public boolean execute(ChatClient sender, String[] args) {
			//If there are no arguments, just send a list of non null combinations
			if (args.length < 2) {
				sender.sendChat(listHerbs());
				return true;
			}
			
			String[] herbArgs = args[1].split(",");
			
			if (herbArgs.length <= 1) {
				sender.sendChat(usage());
				return true;
			}
			
			String key = herbString(herbArgs);
				
			//Check if that key exists
			String val = entries.get(key);

			sender.sendChat(String.format("%s = %s", key, val));
			
			return true;
		}
		
		String listHerbs() {
			StringBuffer strBuf = new StringBuffer("Herb List:\n");
			
			Enumeration<String> e = entries.keys();
			for (String herb : entries.values()) {
				String combo = e.nextElement();
				
				strBuf.append(String.format("%15s - %s\n", combo, herb));
			}
			
			return strBuf.toString();
		}
	}
	
	
	class CMDHerbSet implements Command {
		public String help() {
			return "Set Alchemy Recipe";
		}
		
		public String usage() {
			return String.format(ChatServer.USAGE_STRING, "herbset '<name>' <name1> <#1>, <name2> <#2>, ... <nameN> <#N>");
		}
		
		public boolean execute(ChatClient sender, String[] args) {
			if (args.length < 2) {
				sender.sendChat(usage());
				return true;
			}
			
			Matcher matcher = setPattern.matcher(args[1]);
			
			String herbName = null, herbList = null;
			
			if (matcher.find()) {
				herbName = matcher.group(1);
				herbList = matcher.group(2);
			} else {
				sender.sendChat("Invalid syntax");
				return true;
			}
			
			String[] herbArgs = herbList.split(",");
			
			if (herbArgs.length <= 1) {
				sender.sendChat(usage());
				return true;
			}
			
			String key = herbString(herbArgs);
				
			//Check if that key exists
			sender.sendChat(String.format("Setting '%s' to '%s'\n", herbName, key));
			
			entries.put(key, herbName);
			
			saveEntries();

			return true;
		}
	}


}
