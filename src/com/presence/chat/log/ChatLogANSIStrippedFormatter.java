//
//  ChatLogANSIStrippedFormatter.java
//  ChatServer
//
//  Created by John McKisson on 12/10/10.
//  Copyright 2010 Jefferson Lab. All rights reserved.
//
package com.presence.chat.log;

import java.util.Date;
import java.util.logging.*;

import com.presence.chat.ANSIColor;

/**
 * Formatter used by FileHandlers
 */
public class ChatLogANSIStrippedFormatter extends ChatLogFormatter {

	@Override
	public String format(LogRecord rec) {
		return String.format("%s %s\n", dateFormat.format(new Date(rec.getMillis())), ANSIColor.strip(rec.getMessage()));
	}

}
