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
	private int startX = 0;
	private int startY = 0;
	private int suff = 1;
	private Image image = new Image("file://" + Editor.changeSlash(PATH) + ".labyrinthgame/Images/entities/bat_side_1.png");
	
	public Bat(World w, int x, int y, int pl){
		setData(w);
		setX(x);
		setY(y);
		startX = x;
		startY = y;
		Timeline t = new Timeline(new KeyFrame(Duration.millis(600), event -> {
			if (getX() == startX){
				M = 1;
			}
			if (getX() == startX+pl-1){
				M = -1;
			}
			setX(getX()+M);
		}));
		t.setCycleCount(Animation.INDEFINITE);
		t.play();
		
		Timeline t2 = new Timeline(new KeyFrame(Duration.millis(300), event -> {
			this.image = new Image("file://" + Editor.changeSlash(PATH) + ".labyrinthgame/Images/entities/bat_side_"+this.suff+".png");
			this.suff = (this.suff == 1) ? 2 : 1;
			w.update(0, 0, 0, 0);
		}));
		t2.setCycleCount(Animation.INDEFINITE);
		t2.play();
	}
	
	@Override
	public void draw(GraphicsContext p){
		if (M == 1){
			p.drawImage(this.image, 0, 0, World.BLOCK_WIDTH, World.BLOCK_WIDTH, World.BLOCK_WIDTH, 0, -World.BLOCK_WIDTH, World.BLOCK_WIDTH);
		} else if (M == -1){
			p.drawImage(this.image, getX() * World.BLOCK_WIDTH, getY() * World.BLOCK_WIDTH, World.BLOCK_WIDTH, World.BLOCK_WIDTH);
		}
	}
}
