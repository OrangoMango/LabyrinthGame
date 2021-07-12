package com.orangomango.labyrinth.menu.play.entity;

import javafx.scene.canvas.*;
import javafx.animation.*;
import javafx.scene.image.*;
import javafx.util.Duration;

import com.orangomango.labyrinth.World;
import com.orangomango.labyrinth.menu.editor.Editor;
import static com.orangomango.labyrinth.menu.editor.Editor.PATH;

public class Bat extends Entity{
	private World world;
	private int M = 0;
	private double startX = 0;
	private double startY = 0;
	private int suff = 1;
	private Image image = new Image("file://" + Editor.changeSlash(PATH) + ".labyrinthgame/Images/entities/bat_side_1.png");
	private Timeline t;
	private Timeline t2;
	private String direction;
	
	public static final String HORIZONTAL = "h";
	public static final String VERTICAL = "v";
	
	public Bat(World w, double x, double y, int pl, String d){
		setData(w);
		this.direction = d;
		setX(x);
		setY(y);
		startX = x;
		startY = y;
		t = new Timeline(new KeyFrame(Duration.millis(200), event -> {
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
			if (isOnPlayer(w.getPlayer())){
				w.getPlayer().die();
			}
		}));
		t.setCycleCount(Animation.INDEFINITE);
		
		t2 = new Timeline(new KeyFrame(Duration.millis(300), event -> {
      if (this.direction.equals(HORIZONTAL)){
			  this.image = new Image("file://" + Editor.changeSlash(PATH) + ".labyrinthgame/Images/entities/bat_side_"+this.suff+".png");
			  this.suff = (this.suff == 1) ? 2 : 1;
      } else if (this.direction.equals(VERTICAL)){
      	this.image = this.image = new Image("file://" + Editor.changeSlash(PATH) + ".labyrinthgame/Images/entities/bat_front_"+this.suff+".png");
      	if (this.suff == 3){
      		this.suff = 1;
      	} else {
      		this.suff++;
      	}
      }
			w.update(0, 0, 0, 0);
		}));
		t2.setCycleCount(Animation.INDEFINITE);
	}
	
	public void stop(){
		this.t.stop();
		this.t2.stop();
	}
	
	public void start(){
		this.t.play();
		this.t2.play();
	}
	
	@Override
	public void draw(GraphicsContext p){
		if (M == 1){
			p.drawImage(this.image, 0, 0, World.BLOCK_WIDTH, World.BLOCK_WIDTH, World.BLOCK_WIDTH+getX()*World.BLOCK_WIDTH, 0+getY()*World.BLOCK_WIDTH, -World.BLOCK_WIDTH, World.BLOCK_WIDTH);
		} else if (M == -1){
			p.drawImage(this.image, getX() * World.BLOCK_WIDTH, getY() * World.BLOCK_WIDTH, World.BLOCK_WIDTH, World.BLOCK_WIDTH);
		}
	}
}
