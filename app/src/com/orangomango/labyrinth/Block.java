package com.orangomango.labyrinth;

import javafx.scene.canvas.*;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import com.orangomango.labyrinth.menu.editor.Editor;
import com.orangomango.labyrinth.menu.editor.EditableWorld;
import static com.orangomango.labyrinth.menu.editor.Editor.PATH;
import com.orangomango.labyrinth.menu.play.entity.*;

public class Block {
	protected String type;
	private int x, y;
	private String info = null;
	public String category = "";

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
		switch (this.type){
			case World.WALL:
			case World.SHOOTER:
			case World.VOID:
				this.category = World.WALL;
				break;
			case World.AIR:
			case World.PORTAL:
			case World.SPIKE:
			case World.BAT_GEN:
			case World.MOVABLE:
				this.category = World.AIR;
				break;
		}
	}

	/**
	  Get block type
	  @return Block type
	*/
	public String getType() {
		return this.type;
	}
	
	public String getCategory(){
		return this.category;
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
			case 7:
				return new Block(World.MOVABLE, x1, y1, i);
			case 8:
				return new Block(World.C_SPIKE, x1, y1, i);
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
				String d = Character.toString(this.getInfo().split("#")[1].charAt(0));
				switch (d){
					case World.NORTH:
						pen.drawImage(new Image("file://" + Editor.changeSlash(PATH) + ".labyrinthgame/Images/blocks/block_shooter_v.png"), px * World.BLOCK_WIDTH, py * World.BLOCK_WIDTH, World.BLOCK_WIDTH, World.BLOCK_WIDTH);
						break;
					case World.SOUTH:
						pen.drawImage(new Image("file://" + Editor.changeSlash(PATH) + ".labyrinthgame/Images/blocks/block_shooter_v.png"), 0, 0, World.BLOCK_WIDTH, World.BLOCK_WIDTH, 0+px*World.BLOCK_WIDTH, py * World.BLOCK_WIDTH + World.BLOCK_WIDTH, World.BLOCK_WIDTH, -World.BLOCK_WIDTH);
						break;
					case World.EST:
						pen.drawImage(new Image("file://" + Editor.changeSlash(PATH) + ".labyrinthgame/Images/blocks/block_shooter_h.png"), 0, 0, World.BLOCK_WIDTH, World.BLOCK_WIDTH, px * World.BLOCK_WIDTH + World.BLOCK_WIDTH, 0+py*World.BLOCK_WIDTH, -World.BLOCK_WIDTH, World.BLOCK_WIDTH);
						break;
					case World.WEST:
						pen.drawImage(new Image("file://" + Editor.changeSlash(PATH) + ".labyrinthgame/Images/blocks/block_shooter_h.png"), px * World.BLOCK_WIDTH, py * World.BLOCK_WIDTH, World.BLOCK_WIDTH, World.BLOCK_WIDTH);
						break;
				}
				break;
			case World.BAT_GEN:
				drawAirBlock(pen, px, py);
				if (w instanceof EditableWorld){
					String dir;
					if (!this.info.equals("NoDataSet")){
						dir = this.info.split("#")[1].split(" ")[1];
					} else {
						dir = Entity.HORIZONTAL;
					}
					pen.drawImage(new Image("file://" + Editor.changeSlash(PATH) + ".labyrinthgame/Images/entities/"+((dir.equals(Entity.HORIZONTAL)) ? "bat_side_1.png" : "bat_front_3.png")),  0, 0, World.BLOCK_WIDTH, World.BLOCK_WIDTH, World.BLOCK_WIDTH+px*World.BLOCK_WIDTH, 0+py*World.BLOCK_WIDTH, -World.BLOCK_WIDTH, World.BLOCK_WIDTH);
					if (this.info.equals("NoDataSet")){
						drawWarningSign(pen, px, py);
					}
				}
				break;
			case World.MOVABLE:
				drawAirBlock(pen, px, py);
				if (w instanceof EditableWorld){
				  pen.drawImage(new Image("file://" + Editor.changeSlash(PATH) + ".labyrinthgame/Images/entities/move_block.png"), px * World.BLOCK_WIDTH, py * World.BLOCK_WIDTH, World.BLOCK_WIDTH, World.BLOCK_WIDTH);
					if (this.info.equals("NoDataSet")){
				  	drawWarningSign(pen, px, py);
					} else {
						String direction = this.info.split("#")[1].split(" ")[1];
						switch (direction){
							case "h":
								pen.drawImage(new Image("file://" + Editor.changeSlash(PATH) + ".labyrinthgame/Images/editor/arrow_sign_h.png"), px * World.BLOCK_WIDTH, py * World.BLOCK_WIDTH, World.BLOCK_WIDTH, World.BLOCK_WIDTH);
								break;
							case "v":
								pen.drawImage(new Image("file://" + Editor.changeSlash(PATH) + ".labyrinthgame/Images/editor/arrow_sign_v.png"), px * World.BLOCK_WIDTH, py * World.BLOCK_WIDTH, World.BLOCK_WIDTH, World.BLOCK_WIDTH);
								break;
						}
					}
				}
				break;
			case World.C_SPIKE:
				drawAirBlock(pen, px, py);
				pen.drawImage(new Image("file://" + Editor.changeSlash(PATH) + ".labyrinthgame/Images/blocks/block_spike_closed.png"), px * World.BLOCK_WIDTH, py * World.BLOCK_WIDTH, World.BLOCK_WIDTH, World.BLOCK_WIDTH);
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
				case World.MOVABLE:
					return 7;
				case World.C_SPIKE:
					return 8;
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
