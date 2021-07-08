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
	private int M = 1;
	private double startX = 0;
	private double startY = 0;
	private int suff = 1;
	private Image image = new Image("file://" + Editor.changeSlash(PATH) + ".labyrinthgame/Images/entities/bat_side_1.png");
	private Timeline t;
	private Timeline t2;
	
	public Bat(World w, double x, double y, int pl){
		setData(w);
		setX(x);
		setY(y);
		startX = x;
		startY = y;
		t = new Timeline(new KeyFrame(Duration.millis(200), event -> {
			if (getX() == startX){
				M = 1;
			}
			if (getX() == startX+pl-1){
				M = -1;
			}
			setX(getX()+0.25*M);
		}));
		t.setCycleCount(Animation.INDEFINITE);
		
		t2 = new Timeline(new KeyFrame(Duration.millis(300), event -> {
			this.image = new Image("file://" + Editor.changeSlash(PATH) + ".labyrinthgame/Images/entities/bat_side_"+this.suff+".png");
			this.suff = (this.suff == 1) ? 2 : 1;
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
