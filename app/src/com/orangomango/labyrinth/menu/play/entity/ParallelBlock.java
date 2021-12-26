package com.orangomango.labyrinth.menu.play.entity;

import javafx.scene.canvas.*;
import javafx.animation.*;
import javafx.scene.image.*;
import javafx.util.Duration;

import com.orangomango.labyrinth.World;
import com.orangomango.labyrinth.Block;
import com.orangomango.labyrinth.menu.editor.Editor;
import static com.orangomango.labyrinth.menu.editor.Editor.PATH;
import static com.orangomango.labyrinth.menu.editor.LevelExe.PWS;

public class ParallelBlock extends Entity{
	private final Image image;
	public String[] parallelBlockData;
	private String info;
	private int[][] imageFrames;
	private int internalCounter = 0;
	private Timeline t;
	private boolean active = false;
	private boolean doing = false;
	private int activeImageIndex, inactiveImageIndex;
	private int currentImageIndex;
	
	public ParallelBlock(World w, double x, double y, String info, int[][] imageFrames, int activeImageIndex, int inactiveImageIndex){
		setData(w);
		setX(x);
		setY(y);
		this.info = info;
		this.imageFrames = imageFrames;
		this.activeImageIndex = activeImageIndex;
		this.inactiveImageIndex = inactiveImageIndex;
		this.engineering = true;
		
		int counter = 0;
		parallelBlockData = new String[this.info.split(";").length];
		for (String infoPart : this.info.split(";")){
			parallelBlockData[counter] = infoPart.split("#")[1];
			counter++;
		}
		
		this.image = new Image("file://" + Editor.changeSlash(PATH) + ".labyrinthgame/Images/"+parallelBlockData[Block.checkInfoKey(this.info, "imagePath")]);
		
		t = new Timeline(new KeyFrame(Duration.millis(150), event -> {
			boolean updateRequest = false;
			if (w.getEngineeringWorld().getBlockAt((int)getX(), (int)getY()).isActive()){
				if (!active){
					this.currentImageIndex = imageFrames[0][internalCounter];
					updateRequest = true;
					if (internalCounter+1 != imageFrames[0].length){
						internalCounter++;
					} else {
						internalCounter = 0;
						if (active != true){
							active = true;
						}
					}
				} else {
					if (this.currentImageIndex != this.activeImageIndex){
						internalCounter = 0;
						updateRequest = true;
					}
					this.currentImageIndex = this.activeImageIndex;
				}
			} else {
				if (active){
					this.currentImageIndex = imageFrames[1][internalCounter];
					updateRequest = true;
					if (internalCounter+1 != imageFrames[1].length){
						internalCounter++;
					} else {
						internalCounter = 0;
						if (active != false){
							active = false;
						}
					}
				} else {
					if (this.currentImageIndex != this.inactiveImageIndex){
						internalCounter = 0;
						updateRequest = true;
					}
					this.currentImageIndex = this.inactiveImageIndex;
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
	
	public void stop() {
		super.stop();
		this.t.stop();
	}

	public void start() {
		super.start();
		this.t.play();
	}

	public void draw(GraphicsContext p, double px, double py) {
		p.drawImage(this.image, 1+(World.DEFAULT_BLOCK_WIDTH+2)*this.currentImageIndex, 1, World.DEFAULT_BLOCK_WIDTH, World.DEFAULT_BLOCK_WIDTH, px * World.BLOCK_WIDTH, py * World.BLOCK_WIDTH, World.BLOCK_WIDTH, World.BLOCK_WIDTH);
	}
}
