package com.orangomango.labyrinth.menu;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.net.*;

import static com.orangomango.labyrinth.menu.editor.Editor.PATH;

public class LoadingScreen{
  private static final int LEVELS = 2;

  private void downloadFile(String link, String path){
    try (InputStream in = new URL(link).openStream()) {
      Files.copy(in, Paths.get(path));
    } catch (IOException ex){
    }
  }

  public LoadingScreen(){
    if (new File(PATH+".labyrinthgame").exists()){
      return;
    }
    System.out.println("Downloading files...");
    for (int x = 1; x <= LEVELS; x++){
      System.out.println("Downloading "+"level"+x);
      downloadFile(String.format("https://raw.githubusercontent.com/OrangoMango/LabyrinthGame/main/app/lib/levels/level%s.wld", x), String.format(PATH+".labyrinthgame"+File.separator+"SystemLevels"+File.separator+"level%s.wld", x));
    }
    System.out.println("Done.");
  }
}
