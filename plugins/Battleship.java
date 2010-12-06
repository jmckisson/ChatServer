//
//  Battleship.java
//  
//
//  Created by John McKisson on 12/1/10.
//  Copyright 2010 Jefferson Lab. All rights reserved.
//

import com.presence.chat.*;
import com.presence.chat.commands.Command;
import com.presence.chat.plugin.*;

import java.awt.Point;
import java.io.*;
import java.util.*;

import com.thoughtworks.xstream.*;

import static com.presence.chat.ANSIColor.*;

public class Battleship implements ChatPlugin {

	Map<ChatClient, Game> games;
	
	ArrayList<String> history;
	
	static final String HISTORY_PATH = "bship.xml";

	public void register() {
		XStream xs = PluginManager.getXStream();
		
		try {
			loadHistory();
		} catch (FileNotFoundException e) {
			//TODO: clean this up
			e.printStackTrace();
		}
	
		ChatServer.addCommand("bship", new CMDBShip(), 2);
		
		games = new Hashtable<ChatClient, Game>();
	}
	
	public String name() {
		return "Battleship";
	}
	
	
	public void loadHistory() throws FileNotFoundException {
		try {
			history = (ArrayList<String>)PluginManager.loadRecords(HISTORY_PATH);
			
			if (history == null)
				history = new ArrayList<String>();
				
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void saveHistory() {
		try {
			PluginManager.saveRecords(HISTORY_PATH, history);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	// Contants for the ships
	static final int	NONE = 0,
						CARRIER = 1,	//5
						BATTLESHIP = 2,	//4
						CRUISER = 3,	//3
						SUBMARINE = 4,	//3
						DESTROYER = 5;	//2
						
	static final String[] ROW = new String[] {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J"};
	static final int NUM[] = new int[] {NONE, CARRIER, BATTLESHIP, CRUISER, SUBMARINE, DESTROYER};
	static final String[] NAME = new String[] {"ERROR", "Carrier", "Battleship", "Cruiser", "Submarine", "Destroyer"};
	static final String[] SHIP = new String[] {"X", "C", "B", "R", "S", "D"};
	static final int SLOT[] = new int[] {0, 5, 4, 3, 3, 2};
						
	class CMDBShip implements Command {
		public String help() { return "Battleship Control"; }
		public String usage() { return String.format(ChatServer.USAGE_STRING, "bship <stuff>"); }
		
		
		private Point parsePosition(String pos) {
			int x, y;
			//System.out.println("parsePosition: " + pos);
			try {
				String s = pos.substring(0, 1);
				//System.out.println("s: " + s);
				if (s.equalsIgnoreCase("a"))		y = 0;
				else if (s.equalsIgnoreCase("b"))	y = 1;
				else if (s.equalsIgnoreCase("c"))	y = 2;
				else if (s.equalsIgnoreCase("d"))	y = 3;
				else if (s.equalsIgnoreCase("e"))	y = 4;
				else if (s.equalsIgnoreCase("f"))	y = 5;
				else if (s.equalsIgnoreCase("g"))	y = 6;
				else if (s.equalsIgnoreCase("h"))	y = 7;
				else if (s.equalsIgnoreCase("i"))	y = 8;
				else								y = 9;
				//y = Integer.parseInt(pos.substring(0, 1), 16);
				
				s = pos.substring(1);
				//System.out.println("s: " + s);
				
				x = Integer.parseInt(s);
			} catch (NumberFormatException e) {
				//e.printStackTrace();
				return null;
			}
			
			if (x < 0 || y < 0 || x > 9 || y > 9)
				return null;
			
			return new Point(x, y);
		}
		
		public boolean execute(ChatClient sender, String[] args) {
			
			if (args.length < 2) {
				sender.sendChat(usage());
				return true;
			}
			
			String[] shipArgs = args[1].split(" ");
			
			/////////////////////////
			// Challenge           //
			/////////////////////////
			if (shipArgs[0].equals("challenge")) {
				Game game = games.get(sender);
			
				//Make sure we're not already in a game
				if (game != null) {	//Later add support for multiple games at once
					sender.sendChat("You are in a game, finish this one first!");
					return true;
				}
				
				//Make sure challengee is online
				ChatClient other = ChatServer.getClientByName(shipArgs[1]);
				if (other == null) {
					sender.sendChat("Noone online by that name");
					return true;
				}
				
				if (other == sender) {
					sender.sendChat("You cannot challenge yourself!");
					return true;
				}
				
				if (games.get(other) != null) {
					sender.sendChat("They are already playing a game!");
					return true;
				}
				
				//Create new game
				game = new Game(sender, other);
				games.put(sender, game);
				games.put(other, game);
				
				sender.sendChat("You have started a game of Battleship with " + other.getName() + "!");
				other.sendChat("You have started a game of Battleship with " + sender.getName() + "!");
				return true;
			}
			
			//Get game associated with this client
			Game game = games.get(sender);
			if (game == null) {
				sender.sendChat("You arent in a game!");
				return true;
			}
				
			/////////////////////////
			// Ready               //
			/////////////////////////
			if (shipArgs[0].equals("ready")) {
				//Check if we have placed all ships
				Player player = game.getPlayer(sender);
				if (!player.checkAllPlaced()) {
					sender.sendChat("You have not placed all of your ships yet!");
					return true;
				}
				
				player.setReady();
				sender.sendChat("Ok you are ready, waiting on the other player to finish placement");
				
				//If the other player is also ready, begin the game!
				if (game.getEnemy(sender).isReady()) {
					game.start();
					game.nextTurn();
				} else {
					game.getEnemy(sender).client().sendChat("The other player is waiting on you!");
				}
					
					
			/////////////////////////
			// Show                //
			/////////////////////////
			} else if (shipArgs[0].equals("show")) {
				if (game == null) {
					sender.sendChat("You're not currently playing a game!");
					return true;
				}
				
				game.showFields(sender);
				
				game.getPlayer(sender).sendInfo();
				
				
			/////////////////////////
			// Place               //
			/////////////////////////
			} else if (shipArgs[0].equals("place")) {
				if (game == null) {
					sender.sendChat("You're not currently playing a game!");
					return true;
				}
				
				Player player = game.getPlayer(sender);
			
				if (player.state() != Player.STATE_PLACEMENT) {
					sender.sendChat("You cannot move any ships at this time!");
					return true;
				}
				
				if (shipArgs.length < 4) {
					sender.sendChat("Usage: place <ship> <grid> <v or h>\n" +
									"  Example: 'bship place c a0 h' will put your carrier at a0 horizontally");
					return true;
				}
				
				//Get ship
				int ship = 0;
				for (int i = 0; i < SHIP.length; i++)
					if (shipArgs[1].equalsIgnoreCase(SHIP[i])) {
						ship = i;
						break;
					}
						
				if (ship == 0) {
					sender.sendChat("Invalid Ship!, ships are Carrier cRuiser Battleship Submarine or Destroyer");
					return true;
				}
				
				//System.out.println("Found ship: " + ship);
				
				//Check if ship is already placed
				if (player.isPlaced(ship - 1)) {
					sender.sendChat("You've already placed that ship! Use reset to reset playfield");
					return true;
				}
						
				//Get position
				Point loc = parsePosition(shipArgs[2]);
				if (loc == null) {
					sender.sendChat("Invalid Position, use a0 thru j9");
					return true;
				}
				
				int x = (int)loc.getX();
				int y = (int)loc.getY();
				
				//System.out.println("x: "+ x + "  y: " + y);
				
				//Get alignment
				boolean vert = shipArgs[3].toLowerCase().startsWith("v");
				
				//System.out.println("vertical: " + (vert ? "yes" : "no"));
				
				PlayField myField = player.getField();
				
				//Make sure the given ship/location/alignment will fit and isnt overlapping any other ship
				boolean fail = false;
				try {
					for (int i = 0; i < SLOT[ship]; i++) {
						int tile = myField.getTile(loc);
						
						if ((tile & 0x7) != 0) {
							fail = true;
							sender.sendChat("That would overlap another ship!");
							break;
						}
						
						if (vert)	loc.translate(0, 1);
						else		loc.translate(1, 0);
					}
				} catch (IndexOutOfBoundsException e) {
					sender.sendChat("That ship wont fit there!");
					fail = true;
				}
				
				if (fail)
					return true;
					
				//Ok it should fit and isnt overlapping
				//System.out.println("placing ship: " + NAME[ship]);
				int slots = SLOT[ship];
				//System.out.println("slots: " + slots);
				
				loc.setLocation(x, y);
				for (int i = 0; i < slots; i++) {
					myField.setTile(loc, ship);
					
					if (vert)	loc.translate(0, 1);
					else		loc.translate(1, 0);
				}
				
				player.setPlaced(ship - 1);	//offset for 0 index in ship array
				game.showFields(sender);
				
				player.sendInfo();
					
			
			/////////////////////////
			// Reset              //
			/////////////////////////
			} else if (shipArgs[0].equals("reset")) {
				if (game == null) {
					sender.sendChat("You're not currently playing a game!");
					return true;
				}
				
				Player player = game.getPlayer(sender);
			
				if (player.state() != Player.STATE_PLACEMENT) {
					sender.sendChat("You can only reset your playfield during Placement!");
					return true;
				}
				
				sender.sendChat("Resetting your playfield");
				player.reset();
				game.showFields(sender);
				
				
			/////////////////////////
			// Fire                //
			/////////////////////////
			} else if (shipArgs[0].equals("fire")) {
				if (game == null) {
					sender.sendChat("You're not currently playing a game!");
					return true;
				}
				
				Player player = game.getPlayer(sender);
				
				if (player.state() != Player.STATE_BATTLE) {
					sender.sendChat("The battle hasnt started yet!");
					return true;
				}
				
				if (game.getCurrentTurn() != player) {
					sender.sendChat("It is not your turn!");
					return true;
				}
			
				//fire b1
				Point loc = parsePosition(shipArgs[1]);
				if (loc == null) {
					sender.sendChat("Invalid Position, use a0 thru j9");
					return true;
				}
				
				Player enemy = game.getEnemy(sender);
				PlayField enemyField = enemy.getField();
				
				int tile = enemyField.getTile(loc);
				
				int ship = tile & 0x7;
				if (ship != 0) {
					sender.sendChat("You hit a ship!");
					enemy.client().sendChat("Your " + NAME[ship] + " was hit!");
					
					if (enemy.hit(loc)) {	//returns true if you sunk
					
						ChatClient enemyClient = enemy.client();
					
						sender.sendChat("You sunk their " + NAME[ship] + "!");
						ChatServer.echo(String.format("%s%s[%sBattleShip%s] %s%s %ssunk %s%s's %s%s%s!",
							BLD, RED, GRN, RED, WHT, sender.getName(), RED, WHT, enemyClient.getName(), YEL, NAME[ship], RED));
						enemyClient.sendChat("Your " + NAME[ship] + " was sunk!");
						
						if (enemy.checkAllSunk()) {
							sender.sendChat("You win the game!");
							ChatServer.echo(String.format("%s%s[%sBattleShip%s] %s%s %shas beaten %s%s%s!",
								BLD, RED, GRN, RED, WHT, sender.getName(), RED, WHT, enemyClient.getName(), RED));
							
							games.remove(sender);
							games.remove(enemyClient);
							
							game.saveToHistory();
							return true;
						}
					}
					
				} else {
					sender.sendChat("You missed all ships!");
					enemyField.setTile(loc, tile | 0x10);	//just set try bit
				}
				
				game.nextTurn();
			}
			
			return true;
		}
	}
	
	class Player {
		static final int STATE_PLACEMENT	= 0;
		static final int STATE_READY		= 1;
		static final int STATE_BATTLE		= 2;
		
		int state = STATE_PLACEMENT;
		
		ChatClient client;
		PlayField field;
		
		boolean[] placed = new boolean[5];
		boolean[] sunk = new boolean[5];
		
		Player(ChatClient cl) {
			client = cl;
			field = new PlayField();
			
			for (int i = 0; i < 5; i++) 
				placed[i] = sunk[i] = false;
		}
		
		public ChatClient client() {
			return client;
		}
		
		//return if the ship was sunk
		public boolean hit(Point loc) {
			int ship = field.getTile(loc) & 0x7;
			
			int hitTile = ship | 0x30;
		
			field.setTile(loc, hitTile);
			
			int hitsNeeded = SLOT[ship];	//adjust for index origin
			
			//System.out.println("HIT! need " + hitsNeeded);
			
			int[][] grid = field.getGrid();
			for (int x = 0; x < 10; x++) {
				for (int y = 0; y < 10; y++) {
					if (grid[x][y] == hitTile) {
						if (--hitsNeeded == 0) {
							//System.out.println("SUNK! hit, needed: " + hitsNeeded + " on ship: " + (ship - 1) + "  SLOT[ship] = " + SLOT[ship - 1]);

							sunk[ship - 1] = true;
							return true;
						} else {
							//System.out.println("hit, needed: " + hitsNeeded + " on ship: " + (ship - 1) + "  SLOT[ship] = " + SLOT[ship - 1]);
						}
					}
				}
			}
									
			return false;
		}
		
		public boolean[] getSunk() {
			return sunk;
		}
		
		public boolean checkAllSunk() {
			return sunk[0] && sunk[1] && sunk[2] && sunk[3] && sunk[4];
		}
		
		public boolean checkAllPlaced() {
			return placed[0] && placed[1] && placed[2] && placed[3] && placed[4];
		}
		
		//Show us which ships still need to be placed, or which of ours/theirs we have sunk
		public void sendInfo() {
			StringBuilder strBuf = new StringBuilder();
			if (state == STATE_PLACEMENT) {
				if (placed[0] && placed[1] && placed[2] && placed[3] && placed[4])
					strBuf.append("You have placed all of your ships!\nChat me 'bship ready' if you're ready, or 'bship reset' to start over with placement");
				else {
					strBuf.append("Ships awaiting placement: ");
					for (int i = 0; i < 5; i++) {
						if (!placed[i])
							strBuf.append(NAME[i + 1] + " ");
					}
				}
			} else if (state == STATE_BATTLE) {
				strBuf.append("Enemy ships sunk: ");
				Player enemy = games.get(client).getEnemy(client);
				boolean[] esunk = enemy.getSunk();
				for (int i = 0; i < 5; i++) {
					if (esunk[i])
						strBuf.append(NAME[i + 1] + " ");
				}
			}
			
			client.sendChat(strBuf.toString());
		}
		
		public void setReady() {
			state = STATE_READY;
		}
	
		public boolean isReady() {
			return state == STATE_READY;
		}
			
		public int state() {
			return state;
		}
		
		public void setState(int s) {
			state = s;
		}
		
		public PlayField getField() {
			return field;
		}
		
		public boolean isPlaced(int ship) {
			return placed[ship];
		}
		
		public void setPlaced(int ship) {
			placed[ship] = true;
		}
		
		public void reset() {
			field = new PlayField();
			placed[0] = placed[1] = placed[2] = placed[3] = placed[4] = false;
		}
	}
	
	class Game {
		Player player1, player2;
		
		Player currentTurn = null;
		
		Game(ChatClient p1, ChatClient p2) {
			player1 = new Player(p1);
			player2 = new Player(p2);
			
			String str = "You may now place your ships";
			p1.sendChat(str);
			p2.sendChat(str);
		}
		
		public void start() {
			player1.setState(Player.STATE_BATTLE);
			player2.setState(Player.STATE_BATTLE);
		}
		
		public void nextTurn() {
			ChatClient client1 = player1.client();
			ChatClient client2 = player2.client();
		
			if (currentTurn == null || currentTurn == player2) {
				currentTurn = player1;
				client1.sendChat("It is your turn, 'use bship fire <grid>' to attack " + client2.getName() + "!");
			} else {
				currentTurn = player2;
				client1.sendChat("Waiting for " + client2.getName() + " to fire...");
			}
				
			showFields(client1);
			showFields(client2);
			
			player1.sendInfo();
			player2.sendInfo();
		}
		
		public Player getPlayer(ChatClient cl) {
			if (cl == player1.client())
				return player1;
			else
				return player2;
		}
		
		public Player getEnemy(ChatClient cl) {
			if (cl == player1.client())
				return player2;
			else
				return player1;
		}
		
		public Player getCurrentTurn() {
			return currentTurn;
		}
		
		public void showFields(ChatClient sender) {
		
			PlayField myField = getPlayer(sender).getField();
			PlayField enemyField = getEnemy(sender).getField();
		
			//Show playfields
			Point p = new Point(0, 0);
			StringBuilder strBuf = new StringBuilder();
			
			strBuf.append("    You        Enemy   \n");
			strBuf.append(BLD + GRN + " 0123456789  0123456789\n");
			
			//Loop over rows
			for (int i = 0; i < 10; i++) {
			
				strBuf.append(GRN + ROW[i]);
			
				p.setLocation(0, i);
					
				//Draw Player field row
				for (int j = 0; j < 10; j++) {
					
					int tile = myField.getTile(p);
					
					//On my own field 0x10 is an enemy hit
					
					//Background blue if its clear, red if its been hit
					String bgColor = ((tile & 0x10) == 0x10 ? "41m" : "44m");
					String shipStr = ((tile & 0x7) == 0 ? " " : SHIP[tile & 0x7]);
					
					//if (tile != 0)
					//	System.out.println("tile @ " + i + ", " + j + " = " + tile + "(" + SHIP[tile & 0x7]+")");
					
					strBuf.append((char)27 + "[33;" + bgColor + shipStr);
					p.translate(1, 0);
				}
				
				//Black spacing between
				strBuf.append((char)27 + "[40m" + " ");
				strBuf.append(GRN + ROW[i]);
				
				p.setLocation(0, i);
				
				//Draw Enemy field row
				for (int j = 0; j < 10; j++) {
					
					int tile = enemyField.getTile(p);
					//On enemy field 0x10 is my hit
					//0x20 is set if the enemy reported that I hit a ship
					
					String bgColor = ((tile & 0x10) == 0x10 ? "41m" : "44m");
					String shipStr = ((tile & 0x20) == 0x20 ? "X" : " ");
					
					strBuf.append((char)27 + "[33;" + bgColor + shipStr);
					p.translate(1, 0);
				}
				
				strBuf.append((char)27 + "[40m\n");
			}
			
			sender.sendChat(strBuf.toString());
		}
		
		public void saveToHistory() {
			//currentTurn is the winner
			
			ChatClient winner = currentTurn.client();
			ChatClient loser = getEnemy(winner).client();
			
			String str = String.format("%d;%s;%s", System.currentTimeMillis(), winner.getName(), loser.getName());
			
			history.add(str);
			saveHistory();
		}
	}

	class PlayField {
		int grid[][] = 
			{{	0,	0,	0,	0,	0,	0,	0,	0,	0,	0},
			{	0,	0,	0,	0,	0,	0,	0,	0,	0,	0},
			{	0,	0,	0,	0,	0,	0,	0,	0,	0,	0},
			{	0,	0,	0,	0,	0,	0,	0,	0,	0,	0},
			{	0,	0,	0,	0,	0,	0,	0,	0,	0,	0},
			{	0,	0,	0,	0,	0,	0,	0,	0,	0,	0},
			{	0,	0,	0,	0,	0,	0,	0,	0,	0,	0},
			{	0,	0,	0,	0,	0,	0,	0,	0,	0,	0},
			{	0,	0,	0,	0,	0,	0,	0,	0,	0,	0},
			{	0,	0,	0,	0,	0,	0,	0,	0,	0,	0}};
		
		public void setTile(Point loc, int contents){
			grid[(int)loc.getX()][(int)loc.getY()] = contents;
		}

		public int getTile(Point loc) {
			return grid[(int)loc.getX()][(int)loc.getY()];
		}
		
		public int[][] getGrid() {
			return grid;
		}
	}
}
