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
import java.util.logging.*;
import javax.swing.Timer;

import com.presence.chat.*;
import com.presence.chat.commands.Command;
import com.presence.chat.event.NotificationCenter;
import com.presence.chat.event.NotificationEvent;
import com.presence.chat.plugin.*;

import com.thoughtworks.xstream.*;

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
	
	static final int MAXROWS = 22;

	Timer hourlyTimer;
	int hourlyChats = 0;
	int hourlyOnline = 0;

	//Hourly collections
	LinkedList<Integer> chats, maxOnline;

	public void register() {
		
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
			Logger.getLogger("global").info("Filling in for " + difHours + " missed hours");
		
			for (int i = 0; i < difHours; i++) {
				chats.addFirst(0);
				maxOnline.addFirst(0);
			}
		} else {
			hourlyChats = chats.pop();
			hourlyOnline = maxOnline.pop();
		}
		
		//Set initial delay to the time left the partial hour
		int initialDelay = (int)(timeConst - (difMillis - (difHours * timeConst)));
		Logger.getLogger("global").info("Initial delay: " + initialDelay);

		hourlyAL = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				pushStats();
			}
		};

	
		hourlyTimer = new Timer(timeConst, hourlyAL);	//one hour
		hourlyTimer.setInitialDelay(initialDelay);
		hourlyTimer.setRepeats(true);
		hourlyTimer.start();
		
		NotificationCenter nc = ChatServer.getNotificationCenter();
	
		nc.getObservable().subscribe(event -> notificationChatAll(event));
		nc.getObservable().subscribe(event -> notificationClientConnected(event));
		nc.getObservable().subscribe(event -> notificationNeedDataPush(event));
		nc.getObservable().subscribe(event -> notificationNeedDataPush(event));

		ChatServer.addCommand("stats", new CMDStats(), 2);
	}
	
	public String name() {
		return "Server Stats";
	}
	
	void pushStats() {
		//Push new values
		synchronized (chats) {
			chats.push(hourlyChats);
			maxOnline.push(hourlyOnline);
			
			hourlyChats = 0;
			hourlyOnline = 0;
			
			ChatPrefs.setPref(PREF_LAST_PUSH, String.valueOf(System.currentTimeMillis()));
			saveEntries();
		}
	}
	
	ActionListener hourlyAL;
	
	public void notificationChatAll(NotificationEvent n) {
		synchronized (chats) {
			hourlyChats++;
		}
	}
	
	public void notificationClientConnected(NotificationEvent n) {
		synchronized (chats) {
			int numOnline = ChatServer.getClients().size();
			if (numOnline > hourlyOnline)
				hourlyOnline = numOnline;
		}
	}
	
	public void notificationNeedDataPush(NotificationEvent n) {
		pushStats();
	}
	
	private String makeHist(Integer[] data, String header1, String header2, String titleStr) {
		StringBuilder strBuf = new StringBuilder(BLD);
		
		if (data.length == 0)
			return "";
		
		DecimalFormat df = new DecimalFormat("###,###,###");
	
		//Compute row index values
		int dataLen = Math.min(60, data.length);
				
		int high = 0, low = 0;
		for (int i = 0; i < dataLen; i++) {
			if (data[i] > high)
				high = data[i];
			else if (data[i] < low)
				low = data[i];
		}
		
		int dif = high - low;
		int binDif = dif < MAXROWS ? 1 : (int)dif / MAXROWS;
		int numRows = dif < MAXROWS ? dif + 1 : MAXROWS;
		
		int[] rowVal = new int[numRows];
		String[] rowString = new String[numRows];
		
		//System.out.printf("low: %d   high: %d   dif: %d   binDif: %d numRows: %d\n", low, high, high - low, binDif, numRows);

		int maxLen = 0;
		
		for (int i = 0; i < numRows; i++) {
		
			if (i == 0)
				rowVal[0] = high;
			else
				rowVal[i] = rowVal[i - 1] - binDif;
				
			//Find longest number string
			rowString[i] = df.format(rowVal[i]);
			if (rowString[i].length() > maxLen)
				maxLen = rowString[i].length();
		}
		
		String rowFormat = String.format("%%s%%%ds%%s ", maxLen);

		String h1String = "" + header1;
		String h2String = "60 " + header2 + " Ago";
		
		int len = h1String.length() + h2String.length() + titleStr.length();
		
		//50 is 60 minus string length of "Total" (5), 1 space, <- and ->
		int pad1 = (int)((50 - len) / 2.0);
		
		/*
		System.out.println(len);
		if (len % 2 == 0)
			System.out.println("even");
		else
			System.out.println("odd");
		*/
		
		int pad2 = len % 2 == 0 ? pad1 : pad1 + 1;
		
		String headerFormat = String.format("%%%ds%s <-%s%%%ds%sTotal %s%%%ds%s%s->\n",
											maxLen, RED, h1String, pad1, CYN, titleStr, pad2, RED, h2String);
				
		String header = String.format(headerFormat, " ", " ", " ");
		
		strBuf.append(header);
	
		int bins = Math.min(60, data.length);
	
		//Loop over rows
		for (int r = 0; r < rowVal.length; r++) {
	
			String rowColor = rowVal[r] >= 0 ? YEL : RED;
	
			//Print row value, TODO use number formatter!
			strBuf.append(String.format(rowFormat, GRN, rowString[r], rowColor));
			
			//Loop over bins
			for (int i = 0; i < bins; i++) {
				//For each column check if the bin value is >= the row index value
				if ((	data[i] > 0 && rowVal[r] >= 0 && data[i] >= rowVal[r]) ||
					(	data[i] < 0 && rowVal[r] < 0 && data[i] <= rowVal[r]))
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
		
		public String usage() { return String.format(ChatServer.USAGE_STRING, "stats <daily|hourly>"); }
		
		public boolean execute(ChatClient sender, String[] args) {
			
			if (args.length < 2) {
				sender.sendChat(usage());
				return true;
			}
			
			/*
			String[] statArgs = args[1].split(" ");
			
			if (statArgs.length < 2) {
				sender.sendChat(usage());
				return true;
			}
			*/
			
			//Push current hourly chats
			chats.push(hourlyChats);
			
			Integer[] data = chats.toArray(new Integer[0]);
			
			String type = args[1].toLowerCase();
			String header1, header2, header3 = "Chats";
			
			if (args[1].compareTo("daily") == 0) {
				int[] dayData = new int[(data.length / 24) + 1];
				
				//System.out.println("dayData.length: " + dayData.length);
				
				int day = dayData.length - 1;
				int hour = 0;
				int i = data.length - 1;
				//try {
					for (; i >= 0; i--) {
					
						//System.out.println("dayData["+day+"]("+dayData[day]+") += data["+i+"]("+data[i]+")");
					
						dayData[day] += data[i];
						
						if (++hour % 24 == 0)
							day--;

					}
				//} catch (NullPointerException e) {
				//	System.out.printf("datalen: %d, dayDataLen %d, i: %d", data.length, dayData.length, 1);
				//}
				
				data = new Integer[dayData.length];
				for (i = 0; i < dayData.length; i++)
					data[i] = dayData[i];
				
				header1 = "Today";
				header2 = "Days";
								
			} else if (args[1].compareTo("hourly") == 0) {
				header1 = "This Hour";
				header2 = "Hours";
				
			} else {
				sender.sendChat(usage());
				return true;
			}

			sender.sendChat(makeHist(data, header1, header2, header3));
			
			//Pop hourly chats back off
			chats.pop();
			
			return true;
		}
	}
	
	public Server_Stats() {
	}
	
	
	
	public Server_Stats(String title, String h1, String h2) {
		//int data[] = new int[] {500, 1000, 1005, 2000, 2005, 3000, 3005, 4000, 4005, 5000,
		//						-50, -100, -105, -200,-205, -300, -305, -400, -450, -500,
		//						100, 200, 400, 800, 1600, 3200, -3200, -1600, -800, -400,
		//						0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		//						0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		//						0, 0, 0, 0, 0, 0, 0, 0, 0, 5000};
		
		int data[] = new int[] {0, 0, 0, 1, 1, 1, 1, 2, 2, 2, 2};
								
		//for (int i = 0; i < data.length; i++)
		//	data[i] *= 1000;
			
		Integer[] ints = new Integer[data.length];
		int i = 0;
		for (int val : data) ints[i++] = val;
		
		System.out.println(makeHist(ints, h1, h2, title) + NRM);
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
		new Server_Stats(args[0], args[1], args[2]);
	}
	
	
}
