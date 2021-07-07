package com.orangomango.labyrinth.menu.play.entity;

import javafx.scene.paint.Color;
import javafx.scene.canvas.*;

import com.orangomango.labyrinth.World;

public abstract class Entity{
	protected int x = 0;
	protected int y = 0;
	protected World world;

	public int getX(){
		return x;
	}
	
	public int getY(){
		return y;
	}
	
	public void setX(int v){
		x = v;
	}
	
	public void setY(int v){
		y = v;
	}
	
	protected void setData(World w){
		this.world = w;
	}

	public void draw(GraphicsContext p){
		p.setFill(Color.BLACK);
		p.fillRect(x*World.BLOCK_WIDTH, y*World.BLOCK_WIDTH, World.BLOCK_WIDTH, World.BLOCK_WIDTH);
	}

}
