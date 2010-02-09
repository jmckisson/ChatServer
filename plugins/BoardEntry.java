//
//  BoardEntry.java
//  ChatServer
//
//  Created by John McKisson on 4/3/08.
//  Copyright 2008 __MyCompanyName__. All rights reserved.
//

import java.io.Serializable;
import java.lang.reflect.*;
import java.text.*;
import java.util.Date;

import com.presence.chat.*;

import static com.presence.chat.ANSIColor.*;

class BoardEntry implements Serializable {
	Date date;
	String author;
	String title;
	String content;
	
	//DateFormat used for displaying creation date of board entries
	transient static final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
	
	transient static final String TEMPLATE = String.format("%s[%s%%03d%s][%s%%s%s][%s%%13s%s][%s%%-45s%s]", WHT, YEL, WHT, CYN, WHT, GRN, WHT, CYN, WHT);
	
	public BoardEntry(ChatClient client, String title, String content) {
		author = client.getAccount().getName();
		
		this.title = title;
		this.content = content;
		
		date = new Date(System.currentTimeMillis());
	}
	
	public String getAuthor() {
		return author;
	}
	
	public String getContent() {
		return content;
	}

	public String getHeader() {
		return String.format(TEMPLATE, Bulletin_Board.indexOf(this) + 1, df.format(date), author, title);
	}
}
