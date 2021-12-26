package com.orangomango.labyrinth.menu.play.entity;

import javafx.scene.canvas.*;
import javafx.scene.image.Image;
import javafx.animation.*;
import javafx.util.Duration;

import com.orangomango.labyrinth.World;
import static com.orangomango.labyrinth.menu.editor.LevelExe.PWS;
import static com.orangomango.labyrinth.menu.editor.Editor.PATH;
import com.orangomango.labyrinth.menu.editor.Editor;

public class PoisonCloud extends Entity{
	private int width, height;
	private Timeline t, t2;
	// Max index: 3, from 0 to 3
	private int index = 0;
	
	public PoisonCloud(World w, int width, int height, int yH){
		setData(w);
		setX(0);
		setY(yH);
		this.width = width;
		this.height = height;
		this.layer = true;
		
		t = new Timeline(new KeyFrame(Duration.seconds(3), e -> {
			this.height++;
			if ((int)Math.round(getY()+this.height*World.BLOCK_WIDTH) >= w.getPlayer().getY()*World.BLOCK_WIDTH || (w.getPlayer().psy != null && w.getPlayer().psy*World.BLOCK_WIDTH <= getY()+this.height*World.BLOCK_WIDTH)){
				w.getPlayer().removeHealth(25);
			}
			if (w.getPlayerView()){
				w.update(w.getPlayer().getX()-PWS,w.getPlayer().getY()-PWS, w.getPlayer().getX()+PWS, w.getPlayer().getY()+PWS);
			} else {
				w.update(0, 0, 0, 0);
			}
		}));
		t.setCycleCount(Animation.INDEFINITE);
		
		t2 = new Timeline(new KeyFrame(Duration.millis(500), e -> {
			if (index == 3){
				index = 0;
			} else {
				index++;
			}
		}));
		t2.setCycleCount(Animation.INDEFINITE);
	}
	
	public int getHeight(){
		return this.height;
	}
	
	@Override
	public void start(){
		super.start();
		this.t.play();
		this.t2.play();
	}
	
	@Override
	public void stop(){
		super.stop();
		this.t.stop();
		this.t2.stop();
	}
	
	@Override
	public void draw(GraphicsContext pen, double px, double py){
		for (int y = 0; y < this.height; y++){
			for (int x = 0; x < this.width; x++){
				pen.drawImage(new Image("file://" + Editor.changeSlash(PATH) + ".labyrinthgame/Images/entities/poison_cloud.png"), 1+(World.DEFAULT_BLOCK_WIDTH+2)*this.index, 1, World.DEFAULT_BLOCK_WIDTH, World.DEFAULT_BLOCK_WIDTH, (px+x)*World.BLOCK_WIDTH, (py+y)*World.BLOCK_WIDTH, World.BLOCK_WIDTH, World.BLOCK_WIDTH);
			}
		}
	}
}
