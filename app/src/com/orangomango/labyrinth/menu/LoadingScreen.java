package com.orangomango.labyrinth.menu;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.net.*;

import static com.orangomango.labyrinth.menu.editor.Editor.PATH;

public class LoadingScreen{
  private static final int LEVELS = 3;
	private static final String[] IMGNAMES = new String[]{"ac", "ar", "rr", "rc", "run", "new", "open", "save", "sse"};
	private static final int IMAGES = IMGNAMES.length;

  private void downloadFile(String link, String path){
    try (InputStream in = new URL(link).openStream()) {
      Files.copy(in, Paths.get(path));
    } catch (IOException ex){
    }
  }

  public LoadingScreen(){
    if (new File(PATH+".labyrinthgame"+File.separator+"SystemLevels").list().length != 0){
      return;
    }
    System.out.println("Downloading files...");
    for (int x = 1; x <= LEVELS; x++){
      System.out.println("Downloading "+"level"+x);
      downloadFile(String.format("https://raw.githubusercontent.com/OrangoMango/LabyrinthGame/main/app/lib/levels/level%s.wld", x), String.format(PATH+".labyrinthgame"+File.separator+"SystemLevels"+File.separator+"level%s.wld.sys", x));
    }
		for (int x = 0; x < IMAGES; x++){
			System.out.println("Downloading image "+IMGNAMES[x]);
			downloadFile(String.format("https://github.com/OrangoMango/LabyrinthGame/raw/main/app/lib/images/%s.png", IMGNAMES[x]), String.format(PATH+".labyrinthgame"+File.separator+"Images"+File.separator+"%s.png", IMGNAMES[x]));
		}
    System.out.println("Done.");
  }
}
