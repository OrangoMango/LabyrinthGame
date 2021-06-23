package com.orangomango.labyrinth;

import javafx.scene.canvas.*;
import javafx.scene.paint.Color;

public class Block {
	protected String type;
	private int x, y;

	/**
	  Block class constructor
	  @param t - Type of Block
	  @param x - X coord of Block
	  @param y - Y coord of Block
	*/
	public Block(String t, int x, int y) {
		this.type = t;
		this.x = x;
		this.y = y;
	}

	/**
	  Get block type
	  @return Block type
	*/
	public String getType() {
		return this.type;
	}

	public int getX() {
		return x;
	}
	public int getY() {
		return y;
	}

	/**
	  Create a block instance from a given int (0 or 1)
	  @param x - 1 or 0
	  @param x1 - X coord of block
	  @param y1 - Y coord of block
	*/
	public static Block fromInt(int x, int x1, int y1) {
		switch(x){
			case 0:
				return new Block(World.AIR, x1, y1);
			case 1:
				return new Block(World.WALL, x1, y1);
			case 2:
				return new Block(World.NULL, x1, y1);
			default:
				return null;
		}
	}

	/**
	  Draw the block on the screen
	  @param pen - canvas pen
	*/
	public void draw(GraphicsContext pen) {
		pen.setStroke(Color.BLACK);
		pen.setLineWidth(1);
		switch (getType()){
			case World.WALL:
				pen.setFill(Color.BLACK);
				break;
			case World.AIR:
				pen.setFill(Color.WHITE);
				break;
			case World.NULL:
				pen.setFill(Color.GRAY);
				break;
                        default:
                            pen.setFill(Color.RED);
                            break;
		}
		pen.fillRect(this.x * World.BLOCK_WIDTH, this.y * World.BLOCK_WIDTH, World.BLOCK_WIDTH, World.BLOCK_WIDTH);
	}
	
	public Integer toInt(){
		switch (getType()){
				case World.WALL:
					return 1;
				case World.AIR:
					return 0;
				case World.NULL:
					return 2;
				default:
					return null;
		}
	}

	public boolean isOnStart(World w) {
		if (getX() == w.start[0] && getY() == w.start[1]) {
			return true;
		}
		return false;
	}

	public boolean isOnEnd(World w) {
		if (getX() == w.end[0] && getY() == w.end[1]) {
			return true;
		}
		return false;
	}

	/**
	  Print block object in this format:
	 <pre>BT:wall X:6 Y.8</pre>
	*/
	@Override
	public String toString() {
		return "Block Type: " + this.type + " X:" + this.x + " Y:" + this.y;
	}
}
