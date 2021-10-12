package com.orangomango.labyrinth.menu.play.entity;

import javafx.scene.canvas.*;
import javafx.animation.*;
import javafx.scene.image.*;
import javafx.util.Duration;

import com.orangomango.labyrinth.World;
import com.orangomango.labyrinth.menu.editor.Editor;
import static com.orangomango.labyrinth.menu.editor.Editor.PATH;
import static com.orangomango.labyrinth.menu.editor.LevelExe.PWS;

public class ParallelBlock extends Entity{
	private Image image;
	public String[] parallelBlockData;
	private String info;
	private String[][] imageFrames;
	private int internalCounter = 0;
	private Timeline t;
	private boolean open = false; // = active
	private boolean doing = false;
	private String activeImagePath, inactiveImagePath;
	private String currentImagePath = "";
	
	public ParallelBlock(World w, double x, double y, String info, String[][] imageFrames, String activeImagePath, String inactiveImagePath){
		setData(w);
		setX(x);
		setY(y);
		this.info = info;
		this.imageFrames = imageFrames;
		this.activeImagePath = activeImagePath;
		this.inactiveImagePath = inactiveImagePath;
		this.engineering = true;
		
		int counter = 0;
		parallelBlockData = new String[this.info.split(";").length];
		for (String infoPart : this.info.split(";")){
			parallelBlockData[counter] = infoPart.split("#")[1];
			counter++;
		}
		this.image = new Image("file://" + Editor.changeSlash(PATH) + ".labyrinthgame/Images/"+parallelBlockData[0]);
		
		t = new Timeline(new KeyFrame(Duration.millis(150), event -> {
			boolean updateRequest = false;
			if (w.getEngineeringWorld().getBlockAt((int)getX(), (int)getY()).isActive()){
				if (!open){
					this.currentImagePath = "file://" + Editor.changeSlash(PATH) + ".labyrinthgame/Images/"+imageFrames[0][internalCounter];
					this.image = new Image(this.currentImagePath);
					updateRequest = true;
					if (internalCounter+1 != imageFrames[0].length){
						internalCounter++;
					} else {
						internalCounter = 0;
						if (open != true){
							open = true;
						}
					}
				} else {
					if (!this.currentImagePath.equals("file://" + Editor.changeSlash(PATH) + ".labyrinthgame/Images/"+this.activeImagePath)){
						internalCounter = 0;
						updateRequest = true;
					}
					this.currentImagePath = "file://" + Editor.changeSlash(PATH) + ".labyrinthgame/Images/"+this.activeImagePath;
					this.image = new Image(this.currentImagePath);
				}
			} else {
				if (open){
					this.currentImagePath = "file://" + Editor.changeSlash(PATH) + ".labyrinthgame/Images/"+imageFrames[1][internalCounter];
					this.image = new Image(this.currentImagePath);
					updateRequest = true;
					if (internalCounter+1 != imageFrames[1].length){
						internalCounter++;
					} else {
						internalCounter = 0;
						if (open != false){
							open = false;
						}
					}
				} else {
					if (!this.currentImagePath.equals("file://" + Editor.changeSlash(PATH) + ".labyrinthgame/Images/"+this.inactiveImagePath)){
						internalCounter = 0;
						updateRequest = true;
					}
					this.currentImagePath = "file://" + Editor.changeSlash(PATH) + ".labyrinthgame/Images/"+this.inactiveImagePath;
					this.image = new Image(this.currentImagePath);
				}
			}
			if (updateRequest){
				if (w.getPlayerView()){
					w.update(w.getPlayer().getX()-PWS,w.getPlayer().getY()-PWS, w.getPlayer().getX()+PWS, w.getPlayer().getY()+PWS);
				} else {
					if (w.getPlayerView()){
						w.update(w.getPlayer().getX()-PWS, w.getPlayer().getY()-PWS, w.getPlayer().getX()+PWS, w.getPlayer().getY()+PWS);
					} else {
						w.update(0, 0, 0, 0);
					}
				}
			}
		}));
		t.setCycleCount(Animation.INDEFINITE);
	}
	
	@Override
	public void draw(GraphicsContext p){
		draw(p, getX(), getY());
	}
	
	public void stop() {
		this.t.stop();
	}

	public void start() {
		this.t.play();
	}

	public void draw(GraphicsContext p, double px, double py) {
		p.drawImage(this.image, px * World.BLOCK_WIDTH, py * World.BLOCK_WIDTH, World.BLOCK_WIDTH, World.BLOCK_WIDTH);
	}
}
