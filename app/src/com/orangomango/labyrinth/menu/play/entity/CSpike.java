package com.orangomango.labyrinth.menu.play.entity;

import javafx.scene.canvas.*;
import javafx.scene.image.*;
import javafx.animation.*;
import javafx.util.Duration;

import com.orangomango.labyrinth.World;
import static com.orangomango.labyrinth.menu.editor.Editor.PATH;
import com.orangomango.labyrinth.menu.editor.Editor;
import static com.orangomango.labyrinth.menu.editor.LevelExe.PWS;

public class CSpike extends Entity{
	private Timeline t;
	private Image image = new Image("file://" + Editor.changeSlash(PATH) + ".labyrinthgame/Images/blocks/block_spike_closed.png");
	private int timeOnSpike = 0;
	private boolean opened = false;

	public CSpike(World w, double x, double y){
		setData(w);
		setX(x);
		setY(y);
		
		this.t = new Timeline(new KeyFrame(Duration.millis(30), ev -> {
			if (isOnPlayer(w.getPlayer()) || this.timeOnSpike != 0){
				this.timeOnSpike++;
				if (this.timeOnSpike == 15){
					this.image = new Image("file://" + Editor.changeSlash(PATH) + ".labyrinthgame/Images/blocks/block_spike.png");
					this.opened = true;
					if (w.getPlayerView()){
						w.update(w.getPlayer().getX()-PWS,w.getPlayer().getY()-PWS, w.getPlayer().getX()+PWS, w.getPlayer().getY()+PWS);
					} else {
						w.update(0, 0, 0, 0);
					}
					this.timeOnSpike = -50;
					if (isOnPlayer(w.getPlayer())){
						w.getPlayer().die(w.getPlayerView(), PWS);
					}
				}
			} else {
				if (this.timeOnSpike < 0){
					this.timeOnSpike++;
				}
				this.image = new Image("file://" + Editor.changeSlash(PATH) + ".labyrinthgame/Images/blocks/block_spike_closed.png");
				this.opened = false;
				if (w.getPlayerView()){
					w.update(w.getPlayer().getX()-PWS,w.getPlayer().getY()-PWS, w.getPlayer().getX()+PWS, w.getPlayer().getY()+PWS);
				} else {
					w.update(0, 0, 0, 0);
				}
			}
		}));
		this.t.setCycleCount(Animation.INDEFINITE);
	}
	
	public boolean isOpened(){
		return this.opened;
	}
	
	public void start() {
		this.t.play();
	}

	public void stop() {
		this.t.stop();
	}
	
	@Override
	public void draw(GraphicsContext p){
		draw(p, getX(), getY());
	}
	
	public void draw(GraphicsContext p, double px, double py){
		p.drawImage(this.image, px * World.BLOCK_WIDTH, py * World.BLOCK_WIDTH, World.BLOCK_WIDTH, World.BLOCK_WIDTH);
	}
}
