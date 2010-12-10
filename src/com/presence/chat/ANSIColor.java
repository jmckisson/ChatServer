//
//  ANSIColor.java
//  ChatServer
//
//  Created by John McKisson on 4/4/08.
//  Copyright 2008 __MyCompanyName__. All rights reserved.
//
package com.presence.chat;

public class ANSIColor {
	public static final String NRM = (char)27 + "[0m";
	public static final String BLD = (char)27 + "[1m";
	public static final String DAR = (char)27 + "[2m";
	public static final String UND = (char)27 + "[4m";
	public static final String BLN = (char)27 + "[5m";
	public static final String REV = (char)27 + "[7m";
	public static final String BLK = (char)27 + "[30m";
	public static final String RED = (char)27 + "[31m";
	public static final String GRN = (char)27 + "[32m";
	public static final String YEL = (char)27 + "[33m";
	public static final String BLU = (char)27 + "[34m";
	public static final String MAG = (char)27 + "[35m";
	public static final String CYN = (char)27 + "[36m";
	public static final String WHT = (char)27 + "[37m";
	
	public static String strip(String msg) {
		return msg.replaceAll("\u001b\\[[0-9;]+m", "");
	}
	
}
