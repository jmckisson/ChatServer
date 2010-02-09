//
//  ChatLogFormatter.java
//  ChatServer
//
//  Created by John McKisson on 4/14/08.
//  Copyright 2008 __MyCompanyName__. All rights reserved.
//
package com.presence.chat;

import java.text.*;
import java.util.*;
import java.util.logging.*;

public class ChatLogFormatter extends java.util.logging.Formatter {
	DateFormat dateFormat;
	
	//The Windows XP Command Prompt sucks and cannot display ANSI colors via escape sequences =/
	public static final char ESC = (char)27;
	public static final String NRM = ESC + "[0m";
	public static final String BLD = ESC + "[1m";
	public static final String RED = ESC + "[31m";
	public static final String GRN = ESC + "[32m";
	public static final String YEL = ESC + "[33m";
	public static final String BLU = ESC + "[34m";
	public static final String MAG = ESC + "[35m";
	public static final String CYN = ESC + "[36m";
	public static final String WHT = ESC + "[37m";
	public static final String NUL = "";

	public ChatLogFormatter() {
		dateFormat = new SimpleDateFormat("HH:mm:ss.SSS");
	}

	public String format(LogRecord rec) {
		StringBuilder buf = new StringBuilder(1000);
		
		buf.append(dateFormat.format(new Date(rec.getMillis())));
		buf.append(" [");
		
		// Bold any levels >= WARNING
		//if (rec.getLevel().intValue() >= Level.WARNING.intValue()) {
		//	buf.append(MAG);
		//	buf.append(rec.getLevel());
		//	buf.append(NRM);
		//} else {
		//	buf.append(CYN);
			buf.append(rec.getLevel());
		//	buf.append(NRM);
		//}
		
		buf.append("] ");
		
		StringTokenizer tok = new StringTokenizer(rec.getSourceClassName(), ".");
		int numTokens = tok.countTokens();
		for (int i = 0; i < numTokens - 1; i++)
			tok.nextToken();
			
		String className = tok.nextToken();
		
		//Chop off inner-class names
		int idx = className.indexOf("$");
		if (idx > 0)
			className = className.substring(0, idx);
		
		buf.append(className + ":: ");
		buf.append(formatMessage(rec));
		buf.append('\n');
		
		return buf.toString();
	}

}