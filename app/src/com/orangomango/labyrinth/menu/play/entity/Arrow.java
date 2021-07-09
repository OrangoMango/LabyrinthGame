package com.orangomango.labyrinth.menu.play.entity;

import javafx.scene.canvas.*;
import javafx.animation.*;
import javafx.scene.image.*;
import javafx.util.Duration;

import com.orangomango.labyrinth.World;
import com.orangomango.labyrinth.menu.editor.Editor;
import static com.orangomango.labyrinth.menu.editor.Editor.PATH;

public class Arrow extends Entity{
	private String direction = "";
	private Image image = new Image("file://" + Editor.changeSlash(PATH) + ".labyrinthgame/Images/entities/arrow.png");
	private Timeline t;
	private boolean SHOW = false;
	
	public Arrow(World w, int x, int y){
		setData(w);
		setX(x);
		setY(y);
		t = new Timeline(new KeyFrame(Duration.millis(50), event -> {
			if (!SHOW){
				SHOW = true;
			}
			if (w.getBlockAt((int)getX(), (int)getY()).getType() != World.AIR){
				return;
			} else if (w.getBlockAt((int)(getX()-0.25), (int)getY()) != null){
				if (w.getBlockAt((int)(getX()-0.25), (int)getY()).getType() == World.AIR){
					setX(getX()-0.25);
					w.update(0,0,0,0);
				}
			} else {
				setX(x);
			}
		}));
		t.setCycleCount(Animation.INDEFINITE);
	}
	
	public void start(){
		this.t.play();
	}
	
	public void stop(){
		this.t.stop();
	}
	
	@Override
	public void draw(GraphicsContext pen){
		if (SHOW){
			pen.drawImage(this.image, getX() * World.BLOCK_WIDTH, getY() * World.BLOCK_WIDTH, World.BLOCK_WIDTH, World.BLOCK_WIDTH);
		}
	}
}
