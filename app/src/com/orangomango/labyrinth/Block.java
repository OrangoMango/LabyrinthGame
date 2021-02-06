package com.orangomango.labyrinth;

import javafx.scene.canvas.*;
import javafx.scene.paint.Color;

public class Block{
  private String type;
  private int x, y;

  public Block(String t, int x, int y){
      this.type = t;
      this.x = x;
      this.y = y;
  }

  public String getType(){
    return this.type;
  }

  public static Block fromInt(int x, int x1, int y1){
    if (x == 0){
      return new Block(World.AIR, x1, y1);
    } else {
      return new Block(World.WALL, x1, y1);
    }
  }
  
  public void draw(GraphicsContext pen){
  	pen.setStroke(Color.BLACK);
  	pen.setLineWidth(1);
  	pen.setFill(getType() == World.WALL ? Color.BLACK : Color.WHITE);
  	pen.fillRect(this.x*50, this.y*50, 50, 50);
  }

  @Override
  public String toString(){
    return "BT:"+this.type+" X:"+this.x+" Y:"+this.y;
  }
}
