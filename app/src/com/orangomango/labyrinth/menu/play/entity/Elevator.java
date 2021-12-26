package com.orangomango.labyrinth.menu.play.entity;

import javafx.animation.*;
import javafx.scene.canvas.*;
import javafx.scene.image.*;
import javafx.util.Duration;

import com.orangomango.labyrinth.World;
import static com.orangomango.labyrinth.menu.editor.Editor.PATH;
import com.orangomango.labyrinth.menu.editor.Editor;
import static com.orangomango.labyrinth.menu.editor.LevelExe.PWS;

public class Elevator extends Entity {
	private double startX;
	private int M = 0;
	private double startY;
	private String direction;
	private Timeline t;
	private boolean inRound = false;

	public Elevator(World w, double x, double y, int pl, String d) {
		setX(x);
		setY(y);
		setData(w);
		this.startX = x;
		this.startY = y;
		this.direction = d;

		this.t = new Timeline(new KeyFrame(Duration.millis(60), ev -> {
			if (this.direction.equals(HORIZONTAL)) {
				if (getX() == startX) {
					M = 1;
				}
				if (getX() == startX + pl - 1) {
					M = -1;
				}
				setX(getX() + 0.25 * M);
			} else if (this.direction.equals(VERTICAL)) {
				if (getY() == startY) {
					M = 1;
				}
				if (getY() == startY + pl - 1) {
					M = -1;
				}
				setY(getY() + 0.25 * M);
			}
			if (isOnPlayer(w.getPlayer()) || (inRound)){
				if (w.getPlayer().psx == null && w.getPlayer().psy == null && inRound){
					inRound = false;
				} else {
					if (this.direction.equals(VERTICAL)) {
						w.getPlayer().psy = this.getY();
						w.getPlayer().psx = this.getX();
					} else if (this.direction.equals(HORIZONTAL)) {
						w.getPlayer().psx = this.getX();
						w.getPlayer().psy = this.getY();
					}
					inRound = true;
				}
			} else {
				inRound = false;
			}
			if (w.getPlayerView()){
				if (w.getPlayer().psx != null && w.getPlayer().psy != null){
					w.update((int)Math.round(w.getPlayer().psx)-PWS,(int)Math.round(w.getPlayer().psy)-PWS, (int)Math.round(w.getPlayer().psx)+PWS, (int)Math.round(w.getPlayer().psy)+PWS, true);
				} else {
					w.update(w.getPlayer().getX()-PWS,w.getPlayer().getY()-PWS, w.getPlayer().getX()+PWS, w.getPlayer().getY()+PWS, true);
				}
			} else {
				w.update(0, 0, 0, 0, true);
			}
		}));
		this.t.setCycleCount(Animation.INDEFINITE);
	}

	public void start() {
		super.start();
		this.t.play();
	}

	public void stop() {
		super.stop();
		this.t.stop();
	}
	
	public void draw(GraphicsContext p, double px, double py) {
		if (M != 0)
			p.drawImage(new Image("file://" + Editor.changeSlash(PATH) + ".labyrinthgame/Images/entities/move_block.png"), px * World.BLOCK_WIDTH, py * World.BLOCK_WIDTH, World.BLOCK_WIDTH, World.BLOCK_WIDTH);
	}
}
