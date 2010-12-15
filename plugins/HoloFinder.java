//
//  HoloFinder.java
//  
//
//  Created by John McKisson on 12/14/10.
//  Copyright 2010 Jefferson Lab. All rights reserved.
//

import ij.*;
import ij.gui.*;
import ij.io.*;
import ij.measure.Calibration;
import ij.plugin.*;
import ij.process.*;

import com.presence.chat.*;
import com.presence.chat.commands.Command;
import com.presence.chat.plugin.*;

import java.awt.Point;
import java.awt.event.*;
import java.util.*;
import javax.swing.Timer;
import javax.swing.SwingUtilities;

import com.webobjects.foundation.*;

import static com.presence.chat.ANSIColor.*;

public class HoloFinder implements ChatPlugin {
	
	static final String liveMapStr = "http://www.medievia.com/images/livemaps/livemapinfo.gif";
	
	static final int mode = Blitter.DIFFERENCE;
	
	static final int timeDelay = 1000 * 60 * 10;	//10 minutes
	
	ImagePlus liveMapOld, liveMapNew, resultImage = null;
	
	Opener op;
	
	List<Point> initialLocations;
	List<Point> newLocations;
	
	javax.swing.Timer updateTimer;
	
	public void register() {
		
		ChatServer.getNotificationCenter().addObserver(this, new NSSelector("cleanupTimer", new Class[] {NSNotification.class}), "ServerShutdown", null);
		ChatServer.getNotificationCenter().addObserver(this, new NSSelector("cleanupTimer", new Class[] {NSNotification.class}), "PluginReload", null);
	
		ChatServer.addCommand("holoall", new CMDAllLocations(), 2);
		ChatServer.addCommand("holonew", new CMDNewLocations(), 2);
		ChatServer.addCommand("holoupdate", new CMDHoloUpdate(), 5);
	
		op = new Opener();
	}
	
	public void holoUpdate() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				//Grab initial live map
				liveMapOld = op.openURL(liveMapStr);
				
				ImageProcessor imp = liveMapOld.getProcessor();
						
				initialLocations = new ArrayList<Point>();
				newLocations = new ArrayList<Point>();
															
				search(imp, initialLocations);
				
				ChatServer.echo(getInitialLocations());
								
				//Setup timer action
				updateTimer = new Timer(timeDelay,
					new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							updateTimer.start();
							updateImage();
						}
					}
				);
				
				//Start timer and let it run while we do other crap
				updateTimer.setRepeats(false);
				updateTimer.start();
			}
		});
	}

	//This really shouldnt be needed since I changed the timer to not repeat...
	public void cleanupTimer(NSNotification n) {
		if (updateTimer != null) {
			updateTimer.stop();
			updateTimer = null;
		}
	}
	
	public String name() { return "Holo Finder"; }
	
	class CMDNewLocations implements Command {
		public String help() { return "View New Holo Locations"; }
		
		public String usage() { return String.format(ChatServer.USAGE_STRING, "holonew"); }
		
		public boolean execute(ChatClient sender, String[] args) {
			sender.sendChat(getNewLocations());
			return true;
		}
	}
	
	class CMDAllLocations implements Command {
		public String help() { return "View All Possible Holo Locations"; }
		
		public String usage() { return String.format(ChatServer.USAGE_STRING, "holoall"); }
		
		public boolean execute(ChatClient sender, String[] args) {
			sender.sendChat(getInitialLocations());
			return true;
		}
	}
	
	class CMDHoloUpdate implements Command {
		public String help() { return "Force HoloFinder to update"; }
		
		public String usage() { return String.format(ChatServer.USAGE_STRING, "holoupdate"); }
		
		public boolean execute(ChatClient sender, String[] args) {
			holoUpdate();
			return true;
		}
	}
	
	public String getInitialLocations() {
		if (initialLocations == null)
			return "";
		
		String locs = "";
				
		for (Point p : initialLocations) {
			locs += String.format("(%d, %d) ", (int)p.getX(), (int)p.getY());
		}
		
		return String.format("%s[%sHoloFinder%s] %s%d Possible initial locations:\n%s%s", BLD + RED, GRN, RED, YEL, initialLocations.size(), CYN, locs);
	}
	
	public String getNewLocations() {
		if (newLocations == null)
			return "";
	
		String locs = "";
				
		for (Point p : newLocations) {
			locs += String.format("(%d, %d) ", (int)p.getX(), (int)p.getY());
		}
		
		return String.format("%s[%sHoloFinder%s] %s%d Possible new locations:\n%s%s", BLD + RED, GRN, RED, YEL, newLocations.size(), CYN, locs);
	}
	
	/**
	 * Downloads a new live map and subtracts it from the previous, displaying the result
	 * in a new window.
	 */
	void updateImage() {
	
		liveMapNew = op.openURL(liveMapStr);
		
		ImageProcessor ipOld = liveMapOld.getProcessor();
		ImageProcessor ipNew = liveMapNew.getProcessor();
		
		Calibration cal = liveMapNew.getCalibration();
		
		//We need to keep liveMapNew intact, so create a duplicate image
		ImageProcessor ipTemp = duplicateImage(ipNew);
		
		//Temp = new - old
		ipTemp.copyBits(ipOld, 0, 0, mode);
		
		//old = new
		liveMapOld = liveMapNew;
		
		ipTemp.resetMinAndMax();
		
		newLocations.clear();
		
		search(ipTemp, newLocations);
	
		ChatServer.echo(getNewLocations());
	}
	
	void search(ImageProcessor ip, List<Point> list) {

		for (int x = 10; x < 1990; x++) {
			for (int y = 10; y < 1990; y++) {
				int pix = ip.getPixel(x, y);
					
				try {
					//Check if its green
					if (isGreen(pix)) {
						//Check for patterns
						
						if (isGreen(ip.get(x + 1, y)) &&				//XXX
							isGreen(ip.get(x + 2, y)) &&				// X
							isGreen(ip.get(x + 1, y + 1)) &&
							!isGreen(ip.get(x, y + 1)) &&
							!isGreen(ip.get(x + 2, y + 1)) &&
							!isGreen(ip.get(x + 1, y - 1)) &&
							!isGreen(ip.get(x - 1, y)) &&
							!isGreen(ip.get(x + 3, y)) &&
							!isGreen(ip.get(x + 1, y + 2))) {
							checkPoint(x + 1, y, list);
						}						
						
						else if ((	isGreen(ip.get(x, y + 1)) &&		//X
									isGreen(ip.get(x + 1, y + 1)) &&	//XX
									isGreen(ip.get(x, y + 2)) &&		//X
									!isGreen(ip.get(x + 1, y)) &&
									!isGreen(ip.get(x + 1, y + 2)) &&
									!isGreen(ip.get(x - 1, y + 1)) &&
									!isGreen(ip.get(x, y - 1)) &&
									!isGreen(ip.get(x, y + 3)) &&
									!isGreen(ip.get(x + 2, y + 1))) ||
									
								(	isGreen(ip.get(x - 1, y + 1)) &&	// X
									isGreen(ip.get(x, y + 1)) &&		//XX
									isGreen(ip.get(x, y + 2)) &&		// X
									!isGreen(ip.get(x - 1, y)) &&
									!isGreen(ip.get(x, y + 2)) &&
									!isGreen(ip.get(x + 1, y + 1)) &&
									!isGreen(ip.get(x, y - 1)) &&
									!isGreen(ip.get(x, y + 3)) &&
									!isGreen(ip.get(x - 2, y + 1))) ||
									
								(	isGreen(ip.get(x - 1, y + 1)) &&	// X
									isGreen(ip.get(x, y + 1)) &&		//XXX
									isGreen(ip.get(x + 1, y + 1)) &&
									!isGreen(ip.get(x - 1, y)) &&
									!isGreen(ip.get(x + 1, y)) &&
									!isGreen(ip.get(x, y + 2)) &&
									!isGreen(ip.get(x - 2, y + 1)) &&
									!isGreen(ip.get(x + 2, y + 1)) &&
									!isGreen(ip.get(x, y - 1)))) {
								
							checkPoint(x, y + 1, list);
						}
					}
					
				//This shouldnt happen because I skip the 10 border pixels around the entire map
				} catch (IndexOutOfBoundsException e) {
					continue;
				}
			}
		}
	}
	
	boolean isGreen(int val) {
		return val == 5;
	}
	
	void checkPoint(int x, int y, List<Point> list) {
		Point p = new Point(x, y);
		if (!list.contains(p)) {
			list.add(p);
		}
	}
	
	ImageProcessor duplicateImage(ImageProcessor ip) {
		int width = Math.min(ip.getWidth(), ip.getWidth());
		int height = Math.min(ip.getHeight(), ip.getHeight());
		
		ImageProcessor ipNew = ip.createProcessor(width, height);
		
		ipNew.insert(ip, 0, 0);
		
		return ipNew;
	}

}
