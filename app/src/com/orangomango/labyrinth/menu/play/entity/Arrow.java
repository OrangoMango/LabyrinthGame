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
	private Image image = new Image("file://" + Editor.changeSlash(PATH) + ".labyrinthgame/Images/entities/arrow_h.png");
	private Image image2 = new Image("file://" + Editor.changeSlash(PATH) + ".labyrinthgame/Images/entities/arrow_v.png");
	private Timeline t;
	private boolean SHOW = false;
	private double stepX = 0.0;
	private double stepY = 0.0;
	private double startX = 0.0;
	private double startY = 0.0;
	
	public Arrow(World w, int x, int y, String direction){
		setData(w);
		this.direction = direction;
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
      case World.EST:
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
		w.update(0,0,0,0);
			
		t = new Timeline(new KeyFrame(Duration.millis(40), event -> {
			if (w.getBlockAt((int)Math.round(getX()+stepX), (int)Math.round(getY()+stepY)) != null){
				if ((w.getBlockAt((int)Math.round(getX()+stepX), (int)Math.round(getY()+stepY)).getCategory() == World.AIR) && !isOnPlayer(w.getPlayer(), getX()+stepX, getY()+stepY)){
					setX(getX()+stepX);
					setY(getY()+stepY);
					w.update(0,0,0,0);
				} else {
					if (isOnPlayer(w.getPlayer(), getX()+stepX, getY()+stepY)){
						w.getPlayer().die();
					}
					setX(startX);
					setY(startY);
				}
			} else {
				setX(startX);
				setY(startY);
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
			  case World.NORTH:
			  	pen.drawImage(this.image2, getX() * World.BLOCK_WIDTH, getY() * World.BLOCK_WIDTH, World.BLOCK_WIDTH, World.BLOCK_WIDTH);
			    break;
			  case World.SOUTH:
			  	pen.drawImage(this.image2, 0, 0, World.BLOCK_WIDTH, World.BLOCK_WIDTH, getX() * World.BLOCK_WIDTH + 0, World.BLOCK_WIDTH + getY() * World.BLOCK_WIDTH, World.BLOCK_WIDTH, -World.BLOCK_WIDTH);
			    break;
			}
		}
	}
}
