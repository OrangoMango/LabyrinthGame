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
	
	public Arrow(World w, int x, int y, String direction){
		setData(w);
		this.direction = direction;
		setX(x);
		setY(y);
		t = new Timeline(new KeyFrame(Duration.millis(200), event -> {
			double stepX = 0.0;
			double stepY = 0.0;
			
			switch (direction){
				case World.NORTH:
					stepX = 0.0;
					stepY = -0.25;
					setY(getY()-1);
					break;
				case World.SOUTH:
					stepX = 0.0;
					stepY = 0.25;
					setY(getY()+1);
					break;
				case World.EST:
					stepX = 0.25;
					stepY = 0.0;
					setX(getX()+1);
					break;
				case World.WEST:
					stepX = -0.25;
					stepY = 0.0;
					setX(getX()-1);
			}
			
			if (w.getBlockAt((int)(getX()+stepX), (int)(getY()+stepY)) != null){
				if (w.getBlockAt((int)(getX()+stepX), (int)(getY()+stepY)).getType() == World.AIR){
					setX(getX()+stepX);
					setY(getY()+stepY);
					w.update(0,0,0,0);
				} else {
					setX(x);
					setY(y);
				}
			} else {
				setX(x);
				setY(y);
			}
		}));
		t.setCycleCount(Animation.INDEFINITE);
	}
	
	public void start(){
	  if (!SHOW){
	  	SHOW = true;
	  }
		this.t.play();
	}
	
	public void stop(){
		this.t.stop();
	}
	
	@Override
	public void draw(GraphicsContext pen){
		if (SHOW){
			switch (this.direction){
				case World.WEST:
			    pen.drawImage(this.image, getX() * World.BLOCK_WIDTH, getY() * World.BLOCK_WIDTH, World.BLOCK_WIDTH, World.BLOCK_WIDTH);
			    break;
			  case World.EST:
			  	pen.drawImage(this.image, 0, 0, World.BLOCK_WIDTH, World.BLOCK_WIDTH, getX() * World.BLOCK_WIDTH + World.BLOCK_WIDTH, 0 + getY() * World.BLOCK_WIDTH, -World.BLOCK_WIDTH, World.BLOCK_WIDTH);
			  	break;
			}
		}
	}
}
