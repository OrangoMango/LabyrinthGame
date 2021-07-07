package com.orangomango.labyrinth.menu.play.entity;

import javafx.scene.canvas.*;
import javafx.animation.*;
import javafx.util.Duration;

import com.orangomango.labyrinth.World;

public class Bat extends Entity{
	private World world;
	private static int M = 0;
	private static int startX = 0;
	private static int startY = 0;
	
	public Bat(World w, int x, int y){
		setData(w);
		setX(x);
		setY(y);
		startX = x;
		startY = y;
		Timeline t = new Timeline(new KeyFrame(Duration.millis(500), event -> {
			if (getX() == startX){
				M = 1;
			}
			if (getX() == startX+4){
				M = -1;
			}
			System.out.println(M);
			setX(getX()+M);
			w.update(0, 0, 0, 0);
		}));
		t.setCycleCount(Animation.INDEFINITE);
		t.play();
	}
}
