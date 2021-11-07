package com.orangomango.labyrinth.menu.play.entity;

import javafx.scene.paint.Color;
import javafx.scene.canvas.*;

import com.orangomango.labyrinth.World;
import com.orangomango.labyrinth.Player;

public abstract class Entity{
	protected double x = 0;
	protected double y = 0;
	protected World world;
	public boolean engineering = false;
	public boolean layer = false;
	
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
		draw(p, x, y);
	}

	public void draw(GraphicsContext p, double px, double py){
		p.setFill(Color.BLACK);
		p.fillRect(px*World.BLOCK_WIDTH, py*World.BLOCK_WIDTH, World.BLOCK_WIDTH, World.BLOCK_WIDTH);
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
	
	@Override
	public String toString(){
		return "Entity at X:"+getX()+" Y:"+getY();
	}

}
