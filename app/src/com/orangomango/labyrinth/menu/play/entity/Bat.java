package com.orangomango.labyrinth.menu.play.entity;

import javafx.scene.canvas.*;
import javafx.animation.*;
import javafx.scene.image.*;
import javafx.util.Duration;

import com.orangomango.labyrinth.World;
import com.orangomango.labyrinth.menu.editor.Editor;
import static com.orangomango.labyrinth.menu.editor.Editor.PATH;
import static com.orangomango.labyrinth.menu.editor.LevelExe.PWS;

public class Bat extends Entity {
	private int M = 0;
	private double startX = 0;
	private double startY = 0;
	private int suff = 1;
	private Image image = new Image("file://" + Editor.changeSlash(PATH) + ".labyrinthgame/Images/entities/bat_side_1.png");
	private Timeline t;
	private Timeline t2;
	private String direction;
	private int speed;

	public Bat(World w, double x, double y, int pl, String d, int s, boolean invert, int dmg) {
		setData(w);
		this.direction = d;
		this.layer = true;
		setX(x);
		setY(y);
		startX = x;
		startY = y;
		speed = s;
		t = new Timeline(new KeyFrame(Duration.millis(this.speed), event -> {
			if (this.direction.equals(HORIZONTAL)) {
				if (getX() == startX) {
					M = invert ? -1 : 1;
				}
				if (getX() == (invert ? startX - pl + 1 : startX + pl - 1)) {
					M = invert ? 1 : -1;
				}
				setX(getX() + 0.25 * M);
			} else if (this.direction.equals(VERTICAL)) {
				if (getY() == startY) {
					M = invert ? -1 : 1;
				}
				if (getY() == (invert ? startY - pl + 1 : startY + pl - 1)) {
					M = invert ? 1 : -1;
				}
				setY(getY() + 0.25 * M);
			}
			if (isOnPlayer(w.getPlayer())){
				w.getPlayer().removeHealth(dmg);
			}
		}));
		t.setCycleCount(Animation.INDEFINITE);

		t2 = new Timeline(new KeyFrame(Duration.millis(this.speed / 2 * 3), event -> {
			if (this.direction.equals(HORIZONTAL)) {
				this.image = new Image("file://" + Editor.changeSlash(PATH) + ".labyrinthgame/Images/entities/bat_side_" + this.suff + ".png");
				this.suff = (this.suff == 1) ? 2 : 1;
			} else if (this.direction.equals(VERTICAL)) {
				this.image = this.image = new Image("file://" + Editor.changeSlash(PATH) + ".labyrinthgame/Images/entities/bat_front_" + this.suff + ".png");
				if (this.suff == 3) {
					this.suff = 1;
				} else {
					this.suff++;
				}
			}
			if (w.getPlayerView()){
				w.update(w.getPlayer().getX()-PWS,w.getPlayer().getY()-PWS, w.getPlayer().getX()+PWS, w.getPlayer().getY()+PWS);
			} else {
				w.update(0, 0, 0, 0);
			}
		}));
		t2.setCycleCount(Animation.INDEFINITE);
	}

	public void stop() {
		this.t.stop();
		this.t2.stop();
	}

	public void start() {
		this.t.play();
		this.t2.play();
	}
	
	@Override
	public void draw(GraphicsContext p){
		draw(p, getX(), getY());
	}

	public void draw(GraphicsContext p, double px, double py) {
		if (M == 1) {
			p.drawImage(this.image, 0, 0, this.image.getWidth(), this.image.getHeight(), World.BLOCK_WIDTH + px * World.BLOCK_WIDTH, 0 + py * World.BLOCK_WIDTH, -World.BLOCK_WIDTH, World.BLOCK_WIDTH);
		} else if (M == -1) {
			p.drawImage(this.image, px * World.BLOCK_WIDTH, py * World.BLOCK_WIDTH, World.BLOCK_WIDTH, World.BLOCK_WIDTH);
		}
	}
}
