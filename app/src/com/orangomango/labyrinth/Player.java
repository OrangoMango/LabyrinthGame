package com.orangomango.labyrinth;

public class Player{
  private World world;
  private int x, y;

  public static final String X = "x";
  public static final String Y = "y";
  public static final int POSITIVE = 1;
  public static final int NEGATIVE = -1;

  public Player(int x, int y, World w){
    this.x = x;
    this.y = y;
    world = w;
  }

  public int getX(){return x;}
  public int getY(){return y;}

  public void setX(int x){this.x = x;}
  public void setY(int y){this.y = y;}

  public void moveOn(String direction, int m){
    if (direction == X){
      Block[] xrow = this.world.getXRow(getY());
      while (this.world.getBlockAt(getX() + m, getY()).getType() != this.world.WALL){
        setX(getX() + m);
      }

    } else if (direction == Y){
      Block[] yrow = this.world.getYRow(getX());
      while (this.world.getBlockAt(getX(), getY() + m).getType() != this.world.WALL){
        setY(getY() + m);
      }
    } else {
      System.err.println("Unknown direction");
    }
  }

  public boolean isOnEnd(){
    if (getX() == world.end[0] && getY() == world.end[1]){
      return true;
    }
    return false;
  }

  public boolean isOnStart(){
    if (getX() == world.start[0] && getY() == world.start[1]){
      return true;
    }
    return false;
  }

  @Override
  public String toString(){
    return String.format("Player at X:%s and Y:%s, %s", getX(), getY(), isOnEnd() ? "On end" : (isOnStart() ? "On start" : "Not on end"));
  }
}