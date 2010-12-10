//
//  ChatLogFormatter.java
//  ChatServer
//
//  Created by John McKisson on 4/14/08.
//  Copyright 2008 __MyCompanyName__. All rights reserved.
//
package com.presence.chat.log;

import java.text.*;
import java.util.*;
import java.util.logging.*;

import static com.presence.chat.ANSIColor.*;

public class ChatLogFormatter extends java.util.logging.Formatter {

	static final DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM);
	
	private static final String TEMPLATE = String.format("%s%s[%s%%-12s%s] %s%%s\n", BLD, WHT, CYN, WHT, RED);

	public ChatLogFormatter() {
	}

	public String format(LogRecord rec) {
		/*
		Object[] params = rec.getParameters();
		boolean compact = false;
		if (params != null && params[0] instanceof Boolean)
			compact = ((Boolean)params[0]).booleanValue();
		*/
		
		//Logger.getLogger("global").info("Formatting log record");
		
		return String.format(TEMPLATE, dateFormat.format(new Date(rec.getMillis())), rec.getMessage());
		
		//return String.format("%s %s", dateFormat.format(new Date(rec.getMillis())), rec.getMessage());
	}

}