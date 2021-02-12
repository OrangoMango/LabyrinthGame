package com.orangomango.labyrinth.menu.editor;

import javafx.scene.canvas.*;

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