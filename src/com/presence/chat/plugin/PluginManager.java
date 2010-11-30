//
//  PluginManager.java
//  ChatServer
//
//  Created by John McKisson on 5/12/08.
//  Copyright 2008 __MyCompanyName__. All rights reserved.
//
package com.presence.chat.plugin;

import java.io.*;
import java.util.*;
import java.util.logging.*;

import com.presence.chat.ChatPrefs;

import com.thoughtworks.xstream.*;
import com.webobjects.foundation.NSNotificationCenter;

public class PluginManager {
	ClassLoader classLoader;
	
	Hashtable<String, String> pluginsTable;
	
	XStream xstream;
	
	static PluginManager instance;
	
	public PluginManager() {
		pluginsTable = new Hashtable<String, String>();
		
		xstream = new XStream();
		xstream.setClassLoader(getClassLoader());
		xstream.autodetectAnnotations(true);
		
		instance = this;
	}
	
	public static Hashtable<String, String> getPlugins() {
		return instance.pluginsTable;
	}
	
	
	/**
	 * Utility function to allow customization of xstream output by plugins
	 * @return The XStream instance
	 */
	public static XStream getXStream() {
		return instance.xstream;
	}
	
	
	/**
	 * Utility function abstracting XStream to allow loading of plugin records
	 * @param fileName File path
	 * @return Lloaded record or null if the file doesnt exist
	 */
	public static Object loadRecords(String fileName) throws IOException {
		boolean exists = new File(fileName).exists();
		
		if (!exists) return null;
			
		BufferedReader in = new BufferedReader(new FileReader(fileName));
		StringBuilder xml = new StringBuilder();
		
		String str;

		while ((str = in.readLine()) != null) {
			xml.append(str);
		}
		in.close();

		return instance.xstream.fromXML(xml.toString());
	}
	
	
	/**
	 * Utility function abstracting XStream to allow saving of plugin records
	 * @param fileName File path
	 * @param obj Object ot be saved
	 */
	public static void saveRecords(String fileName, Object obj) throws IOException {
		String xml = instance.xstream.toXML(obj);
		
		try {
			FileOutputStream fos = new FileOutputStream(fileName);
			
			fos.write(xml.getBytes());
			
			fos.flush();
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Returns an instance of the class loader we use to run plugins
	 */
	public ClassLoader getClassLoader() {
		if (classLoader == null)
			classLoader = new ChatPluginClassLoader("plugins");
		
		return classLoader;
	}

	Object runUserPlugIn(String className) {
		ClassLoader loader = classLoader;
		Object thePlugIn = null;
		try { 
			thePlugIn = (loader.loadClass(className)).newInstance(); 
 			if (thePlugIn instanceof ChatPlugin) {
				((ChatPlugin)thePlugIn).register();
			} else
				Logger.getLogger("global").warning("Plugin does not conform to protocol!");

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			if (className.indexOf('_') != -1)
				Logger.getLogger("global").warning("Plugin or class not found: \"" + className + "\"\n(" + e+")");
				
		} catch (NoClassDefFoundError e) {
			e.printStackTrace();
		
			int dotIndex = className.indexOf('.');
			if (dotIndex >= 0)
				return runUserPlugIn(className.substring(dotIndex + 1));
				
			if (className.indexOf('_') != -1)
				Logger.getLogger("global").warning("Plugin or class not found: \"" + className + "\"\n(" + e+")");
				
		} catch (InstantiationException e) {
			Logger.getLogger("global").warning("Unable to load plugin (ins)");
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			Logger.getLogger("global").warning("Unable to load plugin, possibly because it is not public.");
		}

		return thePlugIn;
	} 
	
	
	/**
	 * Locates and installs user plugins
	 */
	public void installPlugins() {
		//String[] plugins = findPlugins();
		
		String pString = ChatPrefs.getPref("Plugins", "");
		if (pString.equals(""))
			return;
		
		String[] plugins = pString.split(";");
		
		if (plugins == null)
			return;
			
		ChatPrefs.setPref("Plugins", "");
			
		Logger.getLogger("global").info(String.format("Installing %d plugins:", plugins.length));

		for (int i = 0; i < plugins.length; i++) {
			if (!plugins[i].equals(""))
				installUserPlugin(plugins[i]);
		}
	}
	
	
	public void reloadPlugins() {
		NSNotificationCenter.defaultCenter().postNotification("PluginReload", null);
	
		pluginsTable.clear();
		
		xstream.setClassLoader(null);
		xstream = null;
		
		((ChatPluginClassLoader)classLoader).clearCache();
		classLoader = null;
		System.gc();
		System.gc();
		
		xstream = new XStream();
		xstream.autodetectAnnotations(true);
		xstream.setClassLoader(getClassLoader());
		installPlugins();
	}


	/** Installs a plugin in the Plugins menu using the class name,
		with underscores replaced by spaces, as the command. */
	public void installUserPlugin(String className) {
	
		String plugins = ChatPrefs.getPref("Plugins", "");
		
		if (plugins.contains(className)) {
			return;
		}
	
		Logger.getLogger("global").info(String.format("Installing plugin: %s", className));

		int slashIndex = className.indexOf('/');
		String command = className;
		
		if (slashIndex > 0) {
			String dir = className.substring(0, slashIndex);
			command = className.substring(slashIndex+1, className.length());
		}
		command = command.replace('_',' ');
		command.trim();
		
		if (pluginsTable.get(command) != null)  // duplicate command?
			command = command + " Plugin";
			
		pluginsTable.put(command, className.replace('/', '.'));
		
		Object plug = runUserPlugIn(className);
		if (plug != null) {
			plugins += className + ";";
			ChatPrefs.setPref("Plugins", plugins);
		}
	}
	
	
	/** Returns a list of the plugins in the plugins directory. */
	public String[] findPlugins() {
			
		File f = new File("plugins");
		
		if (f == null || (f != null && !f.isDirectory())) {
			Logger.getLogger("global").warning("Plugins directory not found!");
			return null;
		}
		
		String[] list = f.list();
		if (list == null) {
			Logger.getLogger("global").info("Found no plugins");
			return null;
		}
			
		Vector v = new Vector();

		for (int i = 0; i < list.length; i++) {
			String name = list[i];
			boolean isClassFile = name.endsWith(".class");
			boolean hasUnderscore = name.indexOf('_') >= 0;
			if (hasUnderscore && isClassFile && name.indexOf('$') < 0 ) {
				name = name.substring(0, name.length() - 6); // remove ".class"
				v.addElement(name);
			} else {
				if (!isClassFile)
					checkSubdirectory("plugins", name, v);
			}
		}
		list = new String[v.size()];
		v.copyInto((String[])list);
		
		return list;
	}
	
	
	/** Looks for plugins and jar files in a subdirectory of the plugins directory. */
	void checkSubdirectory(String path, String dir, Vector v) {
		if (dir.endsWith(".java"))
			return;
			
		File f = new File(path, dir);
		if (!f.isDirectory())
			return;
			
		String[] list = f.list();
		if (list==null)
			return;
			
		dir += "/";
		for (int i = 0; i < list.length; i++) {
			String name = list[i];
			boolean hasUnderscore = name.indexOf('_') >= 0;
			if (hasUnderscore && name.endsWith(".class") && name.indexOf('$') < 0) {
				name = name.substring(0, name.length() - 6); // remove ".class"
				v.addElement(dir + name);
			} 
		}
	}
}
