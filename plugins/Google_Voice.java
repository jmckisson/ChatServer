//
//  Google_Voice.java
//  ChatServer
//
//  Created by John McKisson on 11/17/10.
//  Copyright 2010 Jefferson Lab. All rights reserved.
//

import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

import com.presence.chat.*;
import com.presence.chat.commands.Command;
import com.presence.chat.plugin.*;
import com.techventus.server.voice.Voice;

import static com.presence.chat.ANSIColor.*;

/*
 * http://code.google.com/p/google-voice-java/wiki/GettingStarted
 */
public class Google_Voice implements ChatPlugin {

	static final String NUMBER_FIELD = "SMSNumber";
	static final String GROUP_FIELD = "SMSGroup";

	Voice voice = null;

	public void register() {
		
		tryConnect();
		
		ChatServer.addCommand("sms", new CMDSendSMS(), 5);
		ChatServer.addCommand("getsms", new CMDGetNumber(), 5);
		ChatServer.addCommand("setsms", new CMDSetNumber(), 5);
		ChatServer.addCommand("smslist", new CMDListSMS(), 5);
	}
	
	public String name() {
		return "Google Voice";
	}
	
	private void tryConnect() {
		try {
			voice = new Voice("john.mckisson@gmail.com", "!$Xdf4gh5");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	class CMDSendSMS implements Command {
		public String help() { return "Send an SMS message"; }
		
		public String usage() { return String.format(ChatServer.USAGE_STRING, "sms <group|person> message"); }
	
		public boolean execute(ChatClient sender, String[] args) {
			//args[1] is the argument after the actual command
		
			if (args.length < 2) {
				sender.sendChat(usage());
				return true;
			}
			
			if (voice == null) {
				sender.sendChat("Error connecting to Google Voice!");
				return true;
			}
			
			String[] smsArgs = args[1].split(" ", 2);
			if (smsArgs.length != 2) {
				sender.sendChat(usage());
			} else {
			
				String groupStr = smsArgs[0].toLowerCase();
				String messageStr = smsArgs[1];
				
				List<String> numbers = new ArrayList<String>();
				List<ChatAccount> accounts = AccountManager.getAccounts();
				
				if (groupStr.compareTo("all") == 0) {
					//Send to everyone
					for (ChatAccount account : accounts) {
						String number = account.getField(NUMBER_FIELD);
						if (number != null)
							numbers.add(number);
					}
				} else {
					//Send to everyone in specified group
					for (ChatAccount account : accounts) {
						String group = account.getField(GROUP_FIELD);
						String number = account.getField(NUMBER_FIELD);
						
						//If target was a person, just send to them
						if (account.getName().compareTo(groupStr) == 0) {
							numbers.add(number);
							break;
						}
						
						if (group != null && group.compareTo(groupStr) == 0 && number != null)
							numbers.add(number);
					}
				}
				
				if (numbers.size() == 0) {
					sender.sendChat("No recipients found...");
				} else {
					sender.sendChat("Sending SMS Messages...");
					
					for (String number : numbers) {
						try {
							voice.sendSMS(number, messageStr);
						} catch (IOException e) {
							//Find account with this number
							for (ChatAccount account : accounts) {
								String num = account.getField(NUMBER_FIELD);
								if (num.compareTo(number) == 0) {
									sender.sendChat(String.format("Error sending SMS to %s (%s)", num, account.getName()));
								}
							}
							e.printStackTrace();
						}
					}
					sender.sendChat("Done!");
				}
			}
			
			return true;
		}
	}
	
	class CMDGetNumber implements Command {
		public String help() { return "Show a persons SMS number"; }
		
		public String usage() { return String.format(ChatServer.USAGE_STRING, "getsms <person>"); }
	
		public boolean execute(ChatClient sender, String[] args) {
			//args[1] is the argument after the actual command
			if (args.length < 2) {
				sender.sendChat(usage());
				return true;
			}
			
			//Find an account by the specified name
			ChatAccount ac = AccountManager.getAccount(args[1]);
			
			if (ac == null) {
				sender.sendChat(String.format("There is no account by the name of %s.", args[1]));
				return true;
			}
			
			String number = ac.getField(NUMBER_FIELD);
			
			if (number == null) {
				sender.sendChat("That person has no SMS number set!");
			} else {
				sender.sendChat(String.format("%s's number is %s", ac.getName(), number));
			}
			return true;
		}
	}
	
	class CMDSetNumber implements Command {
		public String help() { return "Set a persons SMS number"; }
		
		public String usage() { return String.format(ChatServer.USAGE_STRING, "setsms <person> <number> [<groups>]"); }
	
		public boolean execute(ChatClient sender, String[] args) {
			//args[1] is the argument after the actual command
			if (args.length < 2) {
				sender.sendChat(usage());
				return true;
			}
			
			String[] setArgs = args[1].split(" ");
			if (setArgs.length < 2) {
				sender.sendChat(usage());
			}
			
			//Find an account by the specified name
			ChatAccount ac = AccountManager.getAccount(setArgs[0]);
			
			if (ac == null) {
				sender.sendChat(String.format("There is no account by the name of %s.", setArgs[0]));
				return true;
			}
			
			ac.setField(NUMBER_FIELD, setArgs[1]);
			
			if (setArgs.length == 3) {
				String newGroupNames = setArgs[2].toLowerCase();
				String existingGroups = ac.getField(GROUP_FIELD);
				
				String groupsString = "";
				
				if (existingGroups == null) {
					groupsString = newGroupNames;
					
				} else {
					List<String> groupsList = Arrays.asList(existingGroups.split(" "));
					for (String newGroup : newGroupNames.split(" ")) {
						if (!groupsList.contains(newGroup))
							groupsList.add(newGroup);
					}
					
					int size = groupsList.size();
					for (int i = 0; i < size; i++) {
						groupsString += groupsList.get(i);
						if (i < size - 1)
							groupsString += " ";
					}
				}
				
				ac.setField(GROUP_FIELD, groupsString);
			}
			
			String groups = ac.getField(GROUP_FIELD);
			
			sender.sendChat(String.format("Added SMS number (%s) for %s in group%s %s",
				setArgs[1], ac.getName(), (groups.split(" ").length > 1 ? "s" : ""), groups));
			
			AccountManager.saveAccounts();
			return true;
		}
	}
	
	class CMDListSMS implements Command {
		public String help() { return "List accounts with SMS numbers"; }
		
		public String usage() { return String.format(ChatServer.USAGE_STRING, "smslist [<group>]"); }
	
		public boolean execute(ChatClient sender, String[] args) {
		
			String groupStr = "all";
		
			if (args.length == 2)
				groupStr = args[1];
			
			StringBuilder strBuf = new StringBuilder();
			List<ChatAccount> accounts = AccountManager.getAccounts();	
			for (ChatAccount account : accounts) {
				String number = account.getField(NUMBER_FIELD);
				String groups = account.getField(GROUP_FIELD);
					
				if (number != null) {
					if (groupStr.compareTo("all") == 0) {
						String gStr = groups == null ? "" : RED + " (" + groups + ")";
						strBuf.append(String.format("\n%s%s - %s%s%s", RED, account.getName(), YEL, number, gStr));
					} else {
						for (String grp : groups.split(" ")) {
							if (grp.compareTo(groupStr) == 0)
								strBuf.append(String.format("\n%s%s (%s) - %s%s", RED, account.getName(), grp, CYN, number));
						}
					}
				}
			}
			
			if (strBuf.length() > 0) {
				sender.sendChat(strBuf.toString());
			} else {
				sender.sendChat("There are no accounts with assigned SMS numbers!");
			}
			
			return true;
		}
	}
}
