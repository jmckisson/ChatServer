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

public class HoloFinder implements ChatPlugin {
	
	static final String liveMapStr = "http://www.medievia.com/images/livemaps/livemapinfo.gif";
	
	static final int mode = Blitter.DIFFERENCE;
	
	static final int timeDelay = 1000 * 60 * 10;	//10 minutes
	
	ImagePlus liveMapOld, liveMapNew, resultImage = null;
	
	Opener op;
	
	List<Point> newLocations;
	
	public void register() {
	
		op = new Opener();
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				//Grab initial live map
				liveMapOld = op.openURL(liveMapStr);
				
				ImageProcessor imp = liveMapOld.getProcessor();
				
				System.out.println(imp.getMin() + "  " + imp.getMax());
				
				String locs = "All Locations: ";
				
				search(imp);
				
				for (Point p : newLocations) {
					locs += p + "\n";
				}
				
				System.out.println(locs);
				
				
				//Setup timer action
				javax.swing.Timer updateTimer = new Timer(timeDelay,
					new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							updateImage();
						}
					}
				);
				
				//Start timer and let it run while we do other crap
				updateTimer.setRepeats(true);
				updateTimer.start();
			}
		});
		
	}
	
	public String name() { return "Holo Finder"; }
	
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
		
		String locs = "New Locations: ";
		
		search((ByteProcessor)ipTemp);
		
		for (Point p : newLocations) {
			locs += p + "\n";
		}
		
		ChatServer.echo(locs);
	}
	
	void search(ImageProcessor ip) {
		newLocations = new ArrayList<Point>();

		for (int x = 1; x < 1999; x++) {
			for (int y = 1; y < 1999; y++) {
				int pix = ip.getPixel(x, y);
				if (x == 267 && y == 637)
					System.out.println(pix + " ("+x + ", " + y + ")");
					
				try {
					//Check if its green
					if (isGreen(pix)) {
						//Check for patterns
						
						//XXX
						// X
						if (isGreen(ip.getPixel(x + 1, y)) && isGreen(ip.getPixel(x + 2, y)) && isGreen(ip.getPixel(x + 1, y + 1)) && !isGreen(ip.getPixel(x, y + 1)) && !isGreen(ip.getPixel(x + 2, y + 1))) {
							checkPoint(x + 1, y);
						}						
						//X   X  X
						//XX XX XXX
						//X   X
						else if ((isGreen(ip.getPixel(x, y + 1)) && isGreen(ip.getPixel(x + 1, y + 1)) && isGreen(ip.getPixel(x, y + 2)) && !isGreen(ip.getPixel(x + 1, y))) ||
								( isGreen(ip.getPixel(x - 1, y + 1)) && isGreen(ip.getPixel(x, y + 1)) && isGreen(ip.getPixel(x, y + 2)) && !isGreen(ip.getPixel(x - 1, y))) ||
								( isGreen(ip.getPixel(x - 1, y + 1)) && isGreen(ip.getPixel(x, y + 1)) && isGreen(ip.getPixel(x + 1, y + 1)) && !isGreen(ip.getPixel(x - 1, y)))) {
								
							checkPoint(x, y + 1);
						}
						
					}
				} catch (IndexOutOfBoundsException e) {
					continue;
				}
			}
		}
	}
	
	boolean isGreen(int val) {
		return val == 5;
	}
	
	void checkPoint(int x, int y) {
		Point p = new Point(x, y);
		if (!newLocations.contains(p)) {
			newLocations.add(p);
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
