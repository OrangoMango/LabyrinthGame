package com.orangomango.labyrinth;

import javafx.scene.canvas.*;
import javafx.scene.paint.Color;
import javafx.scene.image.*;

import com.orangomango.labyrinth.menu.editor.Editor;
import static com.orangomango.labyrinth.menu.editor.Editor.PATH;


public class Player {
	private World world;
	private int x, y;

	public static final String X = "x";
	public static final String Y = "y";
	public static final int POSITIVE = 1;
	public static final int NEGATIVE = -1;

	public Player(int x, int y, World w) {
		this.x = x;
		this.y = y;
		world = w;
	}
	
	public void die(){
		setX(this.world.start[0]);
		setY(this.world.start[1]);
	}

	public int getX() {
		return x;
	}
	public int getY() {
		return y;
	}

	public void setX(int x) {
		this.x = x;
	}
	public void setY(int y) {
		this.y = y;
	}

	public void draw(GraphicsContext pen) {
		draw(pen, getX(), getY());
	}
	
	public void draw(GraphicsContext pen, int x, int y) {
		pen.drawImage(new Image("file://" + Editor.changeSlash(PATH) + ".labyrinthgame/Images/entities/player.png"), x * World.BLOCK_WIDTH, y * World.BLOCK_WIDTH, World.BLOCK_WIDTH, World.BLOCK_WIDTH);
	}

	public void moveOn(String direction, int m, int[] rec) {
		if (direction == X) {
			Block[] xrow = this.world.getXRow(getY());
			while (this.world.getBlockAt(getX() + m, getY()).getType() != this.world.WALL && this.world.getBlockAt(getX() + m, getY()).getType() != this.world.SHOOTER) {
				setX(getX() + m);
			}

		} else if (direction == Y) {
			Block[] yrow = this.world.getYRow(getX());
			while (this.world.getBlockAt(getX(), getY() + m).getType() != this.world.WALL && this.world.getBlockAt(getX(), getY() + m).getType() != this.world.SHOOTER) {
				setY(getY() + m);
			}
		} else {
			Logger.error("Unknow direction found");
		}
		if (rec == null){
			this.world.update(0, 0, 0, 0);
		} else {
			this.world.update(rec[0], rec[1], rec[2], rec[3]);
		}
	}

	public boolean isOnEnd() {
		if (getX() == world.end[0] && getY() == world.end[1]) {
			return true;
		}
		return false;
	}

	public boolean isOnStart() {
		if (getX() == world.start[0] && getY() == world.start[1]) {
			return true;
		}
		return false;
	}
	
	public boolean isOnBlock(String block){
		if (this.world.getBlockAt(getX(), getY()).getType() == block){
			return true;
		}
		return false;
	}

	@Override
	public String toString() {
		return String.format("Player at X:%s and Y:%s, %s", getX(), getY(), isOnEnd() ? "On end" : (isOnStart() ? "On start" : "Not on start or end"));
	}
}
