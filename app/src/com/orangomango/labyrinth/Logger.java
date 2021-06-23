package com.orangomango.labyrinth;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.orangomango.labyrinth.menu.editor.Editor.PATH;

public class Logger{
	private static File logFile;
	
	static {
		logFile  = new File(PATH+".labyrinthgame"+File.separator+"labgame.log");
	}
	
	private static void addLine(String line){
		try {
			SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			Date now = new Date();
			BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true));
			writer.write(String.format("[%s] %s\n", formatter.format(now), line));
			writer.close();
		} catch (IOException ex) {
		}
	}
	
	public static void info(String text){
		addLine("INFO: "+text);
	}
	
	public static void warning(String text){
		addLine("WARNING: "+text);
	}
	
	public static void error(String text){
		addLine("ERROR: "+text);
	}
}
