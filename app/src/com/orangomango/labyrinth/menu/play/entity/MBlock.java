package com.orangomango.labyrinth.menu.play.entity;

import javafx.animation.*;
import javafx.scene.canvas.*;
import javafx.scene.image.*;
import javafx.util.Duration;

import com.orangomango.labyrinth.World;
import static com.orangomango.labyrinth.menu.editor.Editor.PATH;
import com.orangomango.labyrinth.menu.editor.Editor;

public class MBlock extends Entity{
	private double startX;
	private int M = 0;
	private double startY;
  private String direction;
  private Timeline t;
  
  public MBlock(World w, double x, double y, int pl, String d){
  	setX(x);
  	setY(y);
  	setData(w);
  	this.startX = x;
  	this.startY = y;
  	this.direction = d;
  	
  	this.t = new Timeline(new KeyFrame(Duration.millis(60), ev -> {
  		if (this.direction.equals(HORIZONTAL)){
			  if (getX() == startX){
		  		M = 1;
		  	}
	  		if (getX() == startX+pl-1){
	  			M = -1;
  			}
		  	setX(getX()+0.25*M);
			} else if (this.direction.equals(VERTICAL)){
				if (getY() == startY){
					M = 1;
				}
				if (getY() == startY+pl-1){
					M = -1;
				}
				setY(getY()+0.25*M);
			}
  		w.update(0,0,0,0);
  	}));
  	this.t.setCycleCount(Animation.INDEFINITE);
  }
  
  public void start(){
  	this.t.play();
  }
  
  @Override
  public void draw(GraphicsContext p){
  	if (M != 0)
    	p.drawImage(new Image("file://" + Editor.changeSlash(PATH) + ".labyrinthgame/Images/entities/move_block.png"), getX() * World.BLOCK_WIDTH, getY() * World.BLOCK_WIDTH, World.BLOCK_WIDTH, World.BLOCK_WIDTH);
  }
}