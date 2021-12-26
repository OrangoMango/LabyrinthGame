package com.orangomango.labyrinth.menu.play.entity;

import javafx.scene.canvas.*;
import javafx.animation.*;
import javafx.util.Duration;

import com.orangomango.labyrinth.World;
import com.orangomango.labyrinth.menu.editor.Editor;
import static com.orangomango.labyrinth.menu.editor.Editor.PATH;
import static com.orangomango.labyrinth.menu.editor.LevelExe.PWS;

public class Arrow extends Entity{
	private String direction = "";
	private String image = "file://" + Editor.changeSlash(PATH) + ".labyrinthgame/Images/entities/arrow";
	private Timeline t;
	private boolean SHOW = false;
	private double stepX = 0.0;
	private double stepY = 0.0;
	private double startX = 0.0;
	private double startY = 0.0;
	
	public Arrow(World w, int x, int y, String direction, int damage){
		setData(w);
		this.direction = direction;
		this.layer = true;
		setX(x);
		setY(y);
			
		switch (direction){
			case World.NORTH:
				stepX = 0.0;
				stepY = -0.25;
				startX = getX();
				startY = getY()-1;
				setY(startY);
				break;
			case World.SOUTH:
				stepX = 0.0;
				stepY = 0.25;
				startX = getX();
				startY = getY()+1;
				setY(startY);
				break;
			case World.EAST:
				stepX = 0.25;
				stepY = 0.0;
				startX = getX()+1;
				startY = getY();
				setX(startX);
				break;
			case World.WEST:
				stepX = -0.25;
				stepY = 0.0;
				startX = getX()-1;
				startY = getY();
				setX(startX);
				break;
		}
		if (w.getPlayerView()){
			w.update(w.getPlayer().getX()-PWS,w.getPlayer().getY()-PWS, w.getPlayer().getX()+PWS, w.getPlayer().getY()+PWS);
		} else {
			w.update(0, 0, 0, 0);
		}
			
		t = new Timeline(new KeyFrame(Duration.millis(40), event -> {
			if (w.getBlockAt((int)Math.round(getX()+stepX), (int)Math.round(getY()+stepY)) != null){
				if ((w.getBlockAt((int)Math.round(getX()+stepX), (int)Math.round(getY()+stepY)).getCategory() == World.AIR)){ // && !isOnPlayer(w.getPlayer(), getX()+stepX, getY()+stepY)){
					if (isOnPlayer(w.getPlayer(), getX()+stepX, getY()+stepY)){
						w.getPlayer().removeHealth(damage);
					}
					setX(getX()+stepX);
					setY(getY()+stepY);
				} else {
					setX(startX);
					setY(startY);
				}
			} else {
				setX(startX);
				setY(startY);
			}
			if (w.getPlayerView()){
				w.update(w.getPlayer().getX()-PWS,w.getPlayer().getY()-PWS, w.getPlayer().getX()+PWS, w.getPlayer().getY()+PWS);
			} else {
				w.update(0, 0, 0, 0);
			}
		}));
		t.setCycleCount(Animation.INDEFINITE);
	}
	
	@Override
	public void start(){
		super.start();
		if (!SHOW){
			SHOW = true;
		}
		this.t.play();
	}
	
	@Override
	public void stop(){
		super.stop();
		this.t.stop();
	}
	
	@Override
	public void draw(GraphicsContext pen, double px, double py){
		if (SHOW){
			World.drawRotatedImage(pen, this.image, px * World.BLOCK_WIDTH, py * World.BLOCK_WIDTH, World.BLOCK_WIDTH, this.direction, false, false, false, null);
		}
	}
}
