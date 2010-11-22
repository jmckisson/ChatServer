//
//  Server_Stats.java
//  
//
//  Created by John McKisson on 11/19/10.
//  Copyright 2010 Jefferson Lab. All rights reserved.
//

import java.io.*;
import java.util.LinkedList;
import java.text.DecimalFormat;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;

import com.presence.chat.*;
import com.presence.chat.commands.Command;
import com.presence.chat.plugin.*;

import com.thoughtworks.xstream.*;
import com.webobjects.foundation.*;

import static com.presence.chat.ANSIColor.*;

/**
 * Stats to track:
 *	*Chats per day
 *  *Online people per day
 */
public class Server_Stats implements ChatPlugin {

	static final String CHATS_PATH = "hourlyChat.xml";
	static final String ONLINE_COUNT_PATH = "hourlyMaxOnline.xml";
	static final String PREF_LAST_PUSH = "LastStatsPush";

	static final int timeConst = 3600000;

	Timer hourlyTimer;
	int hourlyChats = 0;
	int hourlyOnline = 0;

	//Hourly collections
	LinkedList<Integer> chats, maxOnline;

	public void register() {
	
		System.out.println("register");
	
		XStream xs = PluginManager.getXStream();
		
		try {
			loadEntries();
		} catch (FileNotFoundException e) {
			//TODO: clean this up
			e.printStackTrace();
		}
		
		long currentTime = System.currentTimeMillis();
		
		long lastPush = Long.parseLong(ChatPrefs.getPref(PREF_LAST_PUSH, String.valueOf(currentTime)));
		
		//Find different in hours between now and the last push, then fill in zeroes to account for downtime
		long difMillis = (currentTime - lastPush);
		
		long difHours = difMillis / timeConst;
		
		if (difHours > 0) {
			System.out.println("Filling in for " + difHours + " missed hours");
		
			for (int i = 0; i < difHours; i++) {
				chats.addFirst(0);
				maxOnline.addFirst(0);
			}
		}
		
		//Set initial delay to the time left the partial hour
		int initialDelay = (int)(timeConst - (difMillis - (difHours * timeConst)));
		System.out.println("Initial delay: " + initialDelay);

		hourlyAL = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//Push new values
				synchronized (chats) {
					chats.addFirst(hourlyChats);
					maxOnline.addFirst(hourlyOnline);
					
					hourlyChats = 0;
					hourlyOnline = 0;
					
					ChatPrefs.setPref(PREF_LAST_PUSH, String.valueOf(System.currentTimeMillis()));
					saveEntries();
				}
			}
		};

	
		hourlyTimer = new Timer(timeConst, hourlyAL);	//one hour
		hourlyTimer.setInitialDelay(initialDelay);
		hourlyTimer.setRepeats(true);
		hourlyTimer.start();
	
		ChatServer.getNotificationCenter().addObserver(this, new NSSelector("notificationChatAll", new Class[] {NSNotification.class}), "ChatAll", null);
		ChatServer.getNotificationCenter().addObserver(this, new NSSelector("notificationClientConnected", new Class[] {NSNotification.class}), "ClientConnected", null);

		ChatServer.addCommand("stats", new CMDStats(), 5);
	}
	
	public String name() {
		return "Server Stats";
	}
	
	ActionListener hourlyAL;
	
	public void notificationChatAll(NSNotification n) {
		synchronized (chats) {
			hourlyChats++;
		}
	}
	
	public void notificationClientConnected(NSNotification n) {
		synchronized (chats) {
			int numOnline = ChatServer.getClients().size();
			if (numOnline > hourlyOnline)
				hourlyOnline = numOnline;
		}
	}
	
	private String makeHist(Integer[] data, String titleStr) {
		StringBuilder strBuf = new StringBuilder(BLD);
		
		if (data.length == 0)
			return "";
		
		DecimalFormat df = new DecimalFormat("###,###,###");
	
		//Compute row index values
		int[] rowVal = new int[22];
		String[] rowString = new String[22];
		
		int high = Integer.MIN_VALUE, low = Integer.MAX_VALUE;
		for (int i = 0; i < data.length; i++) {
			if (data[i] > high)
				high = data[i];
			else if (data[i] < low)
				low = data[i];
		}
		
		int dif = (int)Math.round((high - low) / 22.0);
		
		int maxLen = 0;
		for (int i = 0; i < 22; i++) {
			if (i == 0)
				rowVal[0] = high;
			else
				rowVal[i] = rowVal[i - 1] - dif;
				
			//Find longest number string
			rowString[i] = df.format(rowVal[i]);
			if (rowString[i].length() > maxLen)
				maxLen = rowString[i].length();
		}
		
		String rowFormat = String.format("%%s%%%ds%%s ", maxLen);

		
		int pad1 = (int)((30 - titleStr.length()) / 2.0);
		int pad2 = titleStr.length() % 2 == 0 ? pad1 : pad1 + 1;
		
		String headerFormat = String.format("%%%ds %s<-Yesterday%%%ds%sTOTAL %s%%%ds%s60 Days Ago->\n",
											maxLen, RED, pad1, CYN, titleStr, pad2, RED);
				
		String header = String.format(headerFormat, " ", " ", " ");
		
		strBuf.append(header);
	
		int bins = Math.min(60, data.length);
	
		//Loop over rows
		for (int r = 0; r < 22; r++) {
	
			String rowColor = rowVal[r] >= 0 ? YEL : RED;
	
			//Print row value, TODO use number formatter!
			strBuf.append(String.format(rowFormat, GRN, rowString[r], rowColor));
			
			//Loop over bins
			for (int i = 0; i < bins; i++) {
				//For each column check if the bin value is >= the row index value
				if ((	data[i] > 0 && rowVal[r] >= 0 && data[i] >= rowVal[r]) ||
					(	data[i] < 0 && rowVal[r] < 0 && data[i] < rowVal[r]))
					strBuf.append("X");
				else
					strBuf.append(" ");
			}
			
			strBuf.append("\n");
		}
		
		return strBuf.toString();
	}
	
	
	/////////////////////
	// Stats Command
	/////////////////////
	class CMDStats implements Command {
		public String help() { return "View Server Statistics"; }
		
		public String usage() { return String.format(ChatServer.USAGE_STRING, "remove [#]"); }
		
		public boolean execute(ChatClient sender, String[] args) {
			Integer[] data = chats.toArray(new Integer[0]);
			
			sender.sendChat(makeHist(data, "Chats"));
			
			return true;
		}
	}
	
	public Server_Stats() {
	}
	
	
	
	public Server_Stats(String title) {
		int data[] = new int[] {500, 1000, 1005, 2000, 2005, 3000, 3005, 4000, 4005, 5000,
								-50, -100, -105, -200,-205, -300, -305, -400, -450, -500,
								100, 200, 400, 800, 1600, 3200, -3200, -1600, -800, -400,
								0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
								0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
								0, 0, 0, 0, 0, 0, 0, 0, 0, 5000};
								
		for (int i = 0; i < data.length; i++)
			data[i] *= 1000;
			
		Integer[] ints = new Integer[data.length];
		int i = 0;
		for (int val : data) ints[i++] = val;
		
		System.out.println(makeHist(ints, title) + NRM);
	}
	
	public void loadEntries() throws FileNotFoundException {
		
		try {
			chats = (LinkedList<Integer>)PluginManager.loadRecords(CHATS_PATH);
			
			if (chats == null)
				chats = new LinkedList<Integer>();
				
			maxOnline = (LinkedList<Integer>)PluginManager.loadRecords(ONLINE_COUNT_PATH);
			
			if (maxOnline == null)
				maxOnline = new LinkedList<Integer>();
				
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void saveEntries() {
	
		try {
			PluginManager.saveRecords(CHATS_PATH, chats);
			PluginManager.saveRecords(ONLINE_COUNT_PATH, maxOnline);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String args[]) {
		new Server_Stats(args != null ? args[0] : "Nothing");
	}
	
	
}
