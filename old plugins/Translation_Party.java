//
//  Translation_Party.java
//  
//
//  Created by John McKisson on 11/17/10.
//  Copyright 2010 Jefferson Lab. All rights reserved.
//

import java.io.*;
import java.net.*;
import java.util.*;

import com.presence.chat.*;
import com.presence.chat.commands.Command;
import com.presence.chat.event.NotificationEvent;
import com.presence.chat.plugin.*;


import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JsonHierarchicalStreamDriver;
import io.reactivex.rxjava3.disposables.Disposable;

//http://www.timscripts.com/book/translation-party
/*
	alias tparty {
  if (!%tparty) {
    set %tparty 1
    var %x = 1,%last,%url = http://ajax.googleapis.com/ajax/services/language/translate?v=1.0&q= $+ $json.enccomponent($1-) $+ &langpair=en|ja
    var %cur,%last2,%last3
    echo -a [Translation Party] $1-
    while (%x <= 30) {
      %cur = $json(%url,responseData,translatedText)
      echo -a [Translation Party] $iif($calc(%x % 2),To Japanese:,To English:) %cur
      if (!$calc(%x % 2)) {
        if (%cur == %last) {
          echo -a [Translation Party] Equilibrium reached.
          unset %tparty
          return
        }
        if (%cur == %last2) {
          break
        }
        else { %last2 = %last }
        %last = %cur
      }
      inc %x
      %url = http://ajax.googleapis.com/ajax/services/language/translate?v=1.0&q= $+ $json.enccomponent(%cur) $+ &langpair= $+ $iif($calc(%x % 2),en|ja,ja|en)
    }
    echo -a [Translation Party] Equilibrium could not be reached.
    unset %tparty
  }
  else { echo -a [Translation Party] Please wait until the current attempt is finished. }
 
  :error
  ;not that there will be one :D
  unset %tparty
}
*/

public class Translation_Party implements ChatPlugin {

	static final String TPARTY_FIELD = "TParty";
	
	XStream xs;

	public void register() {
		Disposable subscription = ChatServer.getNotificationCenter().getObservable().subscribe(event -> notificationChatAll(event));

		//ChatServer.getNotificationCenter().addObserver(this, new NSSelector("notificationChatAll", new Class[] {NSNotification.class}), "ChatAll", null);
	
		xs = new XStream(new JsonHierarchicalStreamDriver());
		
		//ChatServer.addCommand("tp", new CMDTParty(), 5);
	}
	
	public String name() {
		return "Translation Party";
	}
	
	public void notificationChatAll(NotificationEvent n) {
		String chatString = n.getMessage();
		String[] chatData = chatString.split(":");
		
		System.out.println(chatString);
		
		List<ChatAccount> accounts = AccountManager.getAccounts();
		for (ChatAccount account : accounts) {
			if (account.getName().compareTo(chatData[0]) == 0) {
		
				//String val = account.getField(TPARTY_FIELD);
				//if (val != null && val.compareTo("1") == 0) {
					startParty(account, chatData);
					break;
				//}
			}
		}
		
	}
	
	private void startParty(ChatAccount account, String[] chatData) {
		System.out.println("Getting this party started!");
	
		Party p = new Party(account, chatData);
		
		new Thread(p).start();
	}
	
	/*
	class CMDTParty implements Command {
		public String help() { return "Translate a string"; }
		
		public String usage() { return String.format(ChatServer.USAGE_STRING, "tp message"); }
	
		public boolean execute(ChatClient sender, String[] args) {
			if (args.length == 2)
				
				
			return true;
		}
	}
	*/
	
	class Party implements Runnable {
		ChatAccount account;
		String[] chatData;
		
		Party(ChatAccount account, String[] chatData) {
			this.account = account;
			this.chatData = chatData;
		}
		
		//http://download.oracle.com/javase/tutorial/networking/urls/readingWriting.html
		//http://xstream.codehaus.org/json-tutorial.html
		public void run() {
			try {
				String encodedString = URLEncoder.encode(chatData[2], "UTF-8");
			
				URL url = new URL(String.format("http://ajax.googleapis.com/ajax/services/language/translate?v=1.0&q=%s&langpair=en|ja",
					encodedString));
					
				URLConnection connection = url.openConnection();
				/*
				connection.setDoOutput(true);

				OutputStreamWriter out = new OutputStreamWriter(
										  connection.getOutputStream());
				out.write("string=" + stringToReverse);
				out.close();
				*/
				BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
							
				String decodedString;

				while ((decodedString = in.readLine()) != null) {
					System.out.println(decodedString);
				}
				in.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		
			/*
			int x = 0;
			while (x++ < 30) {
				
			}
			*/
		}
	}
}
