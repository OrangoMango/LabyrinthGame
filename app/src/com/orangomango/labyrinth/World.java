/**
   Labirinth game - world class
   @author OrangoMango
*/

package com.orangomango.labyrinth;

import java.io.*;
import java.util.Arrays;

public class World{
  private int[][] world;
  private String filePath;
  private int height, width;
  public int[] start, end;

  public final static String WALL = "wall";
  public final static String AIR = "air";

  public World(String path){
    filePath = path;
    world = readWorld(filePath);
  }

  private int[][] readWorld(String path){
    File file = new File(path);
    try {
      BufferedReader reader = new BufferedReader(new FileReader(file));
      
      // Get world width and height from file
      String data = reader.readLine();
      this.width = Integer.parseInt(data.split("x")[0]);
      this.height = Integer.parseInt(data.split("x")[0]);

      // Get world layout
      String wData = reader.readLine();
      int[][] Fworld = parseWorldData(wData, height, width);

      String startData = reader.readLine();
      String endData = reader.readLine();

      start = configureFromString(startData);
      end = configureFromString(endData);
      
      reader.close();
      return Fworld;

    } catch (IOException ex){
      System.err.println("Error in world file");
      return null;
    }
  }

  private int[] configureFromString(String data){
    String[] split = data.split(",");
    int[] output = new int[2];
    int x = 0;

    for (String i : split){
      output[x] = Integer.parseInt(i);
      x++;
    }

    return output;
  }

  /**
    Parse give string from file and return an array
    @param String data - string with all file data
    @param int h - world height
    @param int w - world width
  */
  private int[][] parseWorldData(String data, int h, int w){
    String[] current = data.split(",");
    int[][] output = new int[h][w];
    
    int iterator = 0;
    int counter = 0;
    for (String i : current){
      int[] x = new int[w];
      int it2 = 0;

      if (iterator+w > current.length){  // If itertor is bigger than the list length then stop
        break;
      }

      for (String v : Arrays.copyOfRange(current, iterator, iterator+w)){  // Parse every array range in "width" length parts
        x[it2] = Integer.parseInt(v);
        it2++;
      }
      output[counter] = x;
      iterator += w;
      counter++;
    }
    return output;
  }

  public String getBlockAt(int x, int y){
    int item = world[y][x];
    if (item == 0){
      return AIR;
    } else {
      return WALL;
    }
  }

  public int[] getXRow(int y){
    return world[y];
  }

  public int[] getYRow(int x){
    int[] output = new int[height];
    int counter = 0;
    for (int[] i : world){
      output[counter] = i[x];
      counter++;
    }

    return output;
  }

  @Override
  public String toString(){
    StringBuilder builder = new StringBuilder();
    builder.append("World:\n");
    for (int[] x : world){
      builder.append(Arrays.toString(x)).append("\n");
    }
    builder.append(String.format("Width: %s, Height: %s.\nStart at: %s, End at: %s", width, height, Arrays.toString(start), Arrays.toString(end)));

    return builder.toString();
  }
}