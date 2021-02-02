package com.orangomango.labyrinth;

public class Block{
  private String type;

  public Block(String t){
      this.type = t;
  }

  public String getType(){
    return this.type;
  }

  public static Block fromInt(int x){
    if (x == 0){
      return new Block(World.AIR);
    } else {
      return new Block(World.WALL);
    }
  }

  @Override
  public String toString(){
    return "BT:"+this.type;
  }
}