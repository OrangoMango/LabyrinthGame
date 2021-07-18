package com.orangomango.labyrinth.menu.play.entity;

import javafx.scene.paint.Color;
import javafx.scene.canvas.*;

import com.orangomango.labyrinth.World;
import com.orangomango.labyrinth.Player;

public abstract class Entity{
	protected double x = 0;
	protected double y = 0;
	protected World world;
	
  public static final String VERTICAL = "v";
  public static final String HORIZONTAL = "h";

	public double getX(){
		return x;
	}
	
	public double getY(){
		return y;
	}
	
	public void setX(double v){
		x = v;
	}
	
	public void setY(double v){
		y = v;
	}
	
	protected void setData(World w){
		this.world = w;
	}

	public void draw(GraphicsContext p){
		p.setFill(Color.BLACK);
		p.fillRect(x*World.BLOCK_WIDTH, y*World.BLOCK_WIDTH, World.BLOCK_WIDTH, World.BLOCK_WIDTH);
	}
	
	public boolean isOnPlayer(Player pl, double px, double py){
		if (pl.getX() == (int)Math.round(px) && pl.getY() == (int)Math.round(py)){
			return true;
		}
		return false;
	}
	
	public boolean isOnPlayer(Player pl){
		if ((int)Math.round(getX()) == pl.getX() && (int)Math.round(getY()) == pl.getY()){
			return true;
		}
		return false;
	}
	
	public void start(){
	}
	
	public void stop(){
	}

}
