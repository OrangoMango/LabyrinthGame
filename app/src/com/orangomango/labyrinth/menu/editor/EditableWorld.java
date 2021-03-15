package com.orangomango.labyrinth.menu.editor;

import javafx.scene.canvas.*;

import java.io.*;

import com.orangomango.labyrinth.World;
import com.orangomango.labyrinth.Block;

public class EditableWorld extends World{
  public EditableWorld(String path){
    super(path);
  }

  public Block getBlockAtCoord(int x, int y){
    int x1 = x / World.BLOCK_WIDTH;
    int y1 = y / World.BLOCK_WIDTH;
    return super.getBlockAt(x1, y1);
  }

  public void updateOnFile(){
    try {
      BufferedWriter writer = new BufferedWriter(new FileWriter(this.filePath));
      writer.write(this.width+"x"+this.height+"\n");
      int counter = 0;
      for (Block[] bArr : this.world){
        for (Block block : bArr){
          writer.write((block.getType() == World.WALL) ? "1" : "0");
          if (counter+1 != this.height*this.width){
            writer.write(",");
          }
          counter++;
        }
      }
      writer.newLine();
      writer.write(start[0]+","+start[1]+"\n");
      writer.write(end[0]+","+end[1]);
      writer.close();
    } catch (IOException e){
      e.printStackTrace();
    }
    changeToWorld(this.filePath);
  }

  public void setBlockOn(EditableBlock block){
    this.world[block.getY()][block.getX()] = new Block(block.getType(), block.getX(), block.getY());
  }

  public void setCanvas(Canvas c){
    this.canvas = c;
  }

  public void addRow(){

  }

  public void addColumn(){

  }

  public void removeRow(){

  }

  public void removeColumn(){

  }
}