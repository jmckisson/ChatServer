#!/bin/sh

rm -f Battleship.jar
javac -cp ../dist/ChatServer.jar Battleship.java
jar cf Battleship.jar Battleship*.class
rm -f Battleship*.class
