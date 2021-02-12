package com.orangomango.labyrinth.menu.editor;

import com.orangomango.labyrinth.Block;

public class EditableBlock extends Block{
  public EditableBlock(String type, int x, int y){
    super(type, x, y);
  }

  public void setType(String type){
    this.type = type;
  }

  public static EditableBlock fromBlock(Block bl){
    return new EditableBlock(bl.getType(), bl.getX(), bl.getY());
  }

  public void toggleType(){
    if (getType() == EditableWorld.WALL){
      setType(EditableWorld.AIR);
    } else if (getType() == EditableWorld.AIR) {
      setType(EditableWorld.WALL);
    }
  }
}