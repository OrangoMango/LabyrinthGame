package com.orangomango.labyrinth;

import javafx.scene.canvas.*;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import com.orangomango.labyrinth.menu.editor.Editor;
import com.orangomango.labyrinth.menu.editor.EditableWorld;
import static com.orangomango.labyrinth.menu.editor.Editor.PATH;
import com.orangomango.labyrinth.menu.play.entity.Bat;

public class Block {
	protected String type;
	private int x, y;
	private String info = null;

	/**
	  Block class constructor
	  @param t - Type of Block
	  @param x - X coord of Block
	  @param y - Y coord of Block
	*/
	public Block(String t, int x, int y, String i) {
		this.type = t;
		this.x = x;
		this.y = y;
		this.info = i;
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
	
	public void setInfo(String i){this.info = i;}
	public String getInfo(){return this.info;}

	/**
	  Create a block instance from a given int (0 or 1)
	  @param x - 1 or 0
	  @param x1 - X coord of block
	  @param y1 - Y coord of block
	*/
	public static Block fromInt(int x, int x1, int y1, String i) {
		switch(x){
			case 0:
				return new Block(World.AIR, x1, y1, i);
			case 1:
				return new Block(World.WALL, x1, y1, i);
			case 2:
				return new Block(World.VOID, x1, y1, i);
			case 3:
				return new Block(World.SPIKE, x1, y1, i);
			case 4:
				return new Block(World.PORTAL, x1, y1, i);
			case 5:
				return new Block(World.SHOOTER, x1, y1, i);
			case 6:
				return new Block(World.BAT_GEN, x1, y1, i);
			default:
				return null;
		}
	}

	/**
	  Draw the block on the screen
	  @param pen - canvas pen
	*/
	public void draw(GraphicsContext pen, World w) {
		draw(pen, this.x, this.y, w);
	}
	
	private void drawAirBlock(GraphicsContext pen, int px, int py){
		pen.drawImage(new Image("file://" + Editor.changeSlash(PATH) + ".labyrinthgame/Images/blocks/block_air.png"), px * World.BLOCK_WIDTH, py * World.BLOCK_WIDTH, World.BLOCK_WIDTH, World.BLOCK_WIDTH);
	}
	
	private void drawWarningSign(GraphicsContext pen, int px, int py){
		pen.drawImage(new Image("file://" + Editor.changeSlash(PATH) + ".labyrinthgame/Images/editor/warning.png"), px * World.BLOCK_WIDTH, py * World.BLOCK_WIDTH, World.BLOCK_WIDTH, World.BLOCK_WIDTH);
	}
	
	public void draw(GraphicsContext pen, int px, int py, World w) {
		pen.setStroke(Color.BLACK);
		pen.setLineWidth(1);
		switch (getType()){
			case World.WALL:
				pen.drawImage(new Image("file://" + Editor.changeSlash(PATH) + ".labyrinthgame/Images/blocks/block_wall.png"), px * World.BLOCK_WIDTH, py * World.BLOCK_WIDTH, World.BLOCK_WIDTH, World.BLOCK_WIDTH);
				break;
			case World.AIR:
				drawAirBlock(pen, px, py);
				break;
			case World.VOID:
				pen.setFill(Color.GRAY);
				pen.fillRect(px * World.BLOCK_WIDTH, py * World.BLOCK_WIDTH, World.BLOCK_WIDTH, World.BLOCK_WIDTH);
				break;
			case World.SPIKE:
				drawAirBlock(pen, px, py);
				pen.drawImage(new Image("file://" + Editor.changeSlash(PATH) + ".labyrinthgame/Images/blocks/block_spike.png"), px * World.BLOCK_WIDTH, py * World.BLOCK_WIDTH, World.BLOCK_WIDTH, World.BLOCK_WIDTH);
				break;
			case World.PORTAL:
				drawAirBlock(pen, px, py);
				pen.drawImage(new Image("file://" + Editor.changeSlash(PATH) + ".labyrinthgame/Images/blocks/block_portal.png"), px * World.BLOCK_WIDTH, py * World.BLOCK_WIDTH, World.BLOCK_WIDTH, World.BLOCK_WIDTH);
        if (this.info.equals("NoPointSet") && w instanceof EditableWorld){
          drawWarningSign(pen, px, py);
				}
				break;
			case World.SHOOTER:
				pen.drawImage(new Image("file://" + Editor.changeSlash(PATH) + ".labyrinthgame/Images/blocks/block_shooter.png"), px * World.BLOCK_WIDTH, py * World.BLOCK_WIDTH, World.BLOCK_WIDTH, World.BLOCK_WIDTH);
				break;
			case World.BAT_GEN:
				drawAirBlock(pen, px, py);
				if (w instanceof EditableWorld){
				  pen.drawImage(new Image("file://" + Editor.changeSlash(PATH) + ".labyrinthgame/Images/entities/bat_side_1.png"), px * World.BLOCK_WIDTH, py * World.BLOCK_WIDTH, World.BLOCK_WIDTH, World.BLOCK_WIDTH);
				  if (this.info.equals("NoDataSet")){
					  drawWarningSign(pen, px, py);
				  }
				}
				break;
      default:
        pen.setFill(Color.RED);
				pen.fillRect(px * World.BLOCK_WIDTH, py * World.BLOCK_WIDTH, World.BLOCK_WIDTH, World.BLOCK_WIDTH);
        break;
		}
	}
	
	public Integer toInt(){
		switch (getType()){
				case World.WALL:
					return 1;
				case World.AIR:
					return 0;
				case World.VOID:
					return 2;
				case World.SPIKE:
					return 3;
				case World.PORTAL:
					return 4;
				case World.SHOOTER:
					return 5;
				case World.BAT_GEN:
					return 6;
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
		return "Block Type: " + this.type + " X:" + this.x + " Y:" + this.y + " Info: " + ((this.info == null) ? "No info" : this.info);
	}
}
