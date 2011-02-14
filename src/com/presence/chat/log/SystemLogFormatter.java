//
//  SystemLogFormatter.java
//  ChatServer
//
//  Created by John McKisson on 12/10/10.
//  Copyright 2010 Jefferson Lab. All rights reserved.
//
package com.presence.chat.log;

import java.text.*;
import java.util.*;
import java.util.logging.*;

import static com.presence.chat.log.StdOutErrLevel.*;

import com.presence.chat.ANSIColor;

/**
 * Formatter used by the global log instance
 */
public class SystemLogFormatter extends java.util.logging.Formatter {
	DateFormat dateFormat;

	public SystemLogFormatter() {
		dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM);
	}

	public String format(LogRecord rec) {
		StringBuilder buf = new StringBuilder();
		
		buf.append(dateFormat.format(new Date(rec.getMillis())));
		
		Level level = rec.getLevel();
		
		buf.append(" [" + level.getName() + "] ");
		
		StringTokenizer tok = new StringTokenizer(rec.getSourceClassName(), ".");
		int numTokens = tok.countTokens();
		for (int i = 0; i < numTokens - 1; i++)
			tok.nextToken();
			
		String className = tok.nextToken();
		
		//Chop off inner-class names
		int idx = className.indexOf("$");
		if (idx > 0)
			className = className.substring(0, idx);
		
		int lvl = level.intValue();
		
		if (lvl != OUTLVLINT && lvl != ERRLVLINT)
			buf.append(className + ":: ");

		buf.append(ANSIColor.strip(rec.getMessage()));
		//buf.append(formatMessage(rec));
		buf.append('\n');
		
		return buf.toString();
	}
}