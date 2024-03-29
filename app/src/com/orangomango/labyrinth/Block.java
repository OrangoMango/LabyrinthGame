package com.orangomango.labyrinth;

import javafx.scene.canvas.*;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.effect.ColorAdjust;

import com.orangomango.labyrinth.menu.editor.Editor;
import com.orangomango.labyrinth.menu.editor.EditableWorld;
import static com.orangomango.labyrinth.menu.editor.Editor.PATH;
import com.orangomango.labyrinth.menu.play.entity.*;
import com.orangomango.labyrinth.engineering.EngBlock;

public class Block {
	protected String type;
	private int x, y;
	private String info = null;
	public String category = "";
	public String[] parallelBlockData = null;
	private boolean water = false;
	private boolean wallAttach = false;
	
	public static final double DARK = -0.25;
	public static final double LIGHT = 0;
	public static final int LIGHT_AREA = 1;

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
			case World.ELEVATOR:
			case World.BAT_GEN:
			case World.D_WARNING:
			case World.D_ARROW:
			case World.D_PLANT:
			case World.D_BUSH:
			case World.D_CONE:
			case World.D_STONES:
			case World.OXYGEN_POINT:
				this.category = World.AIR;
				break;
		}
		if (this.type.equals(World.WALL) || this.type.equals(World.SHOOTER)){
			this.wallAttach = true;
		}
		if (this.info != null){
			if (checkInfoKey("data") > 0){
				if (this.info.split(";")[checkInfoKey("data")].split("#")[1].equals("NoDataSet")){
					this.category = World.AIR;
				}
			}
			if (checkInfoKey("point") > 0){
				if (this.info.split(";")[checkInfoKey("point")].split("#")[1].equals("NoPointSet")){
					this.category = World.AIR;
				}
			}
			
			int wc = 0;
			boolean found = false;
			for (String ik : this.info.split(";")){
				if (ik.split("#")[0].equals("water")){
					found = true;
					break;
				}
				wc++;
			}
			if (found){
				String iw = this.info.split(";")[wc];
				if (iw.equals("water#true")){
					this.water = true;
				}
			}
		}
		if (this.type.equals(World.PARALLEL_BLOCK)){
			if (this.info != null){
				int counter = 0;
				parallelBlockData = new String[this.info.split(";").length];
				for (String infoPart : this.info.split(";")){
					parallelBlockData[counter] = infoPart.split("#")[1];
					counter++;
				}
			}
			this.category = parallelBlockData[checkInfoKey("category")];
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
	
	public boolean getWallAttach(){
		return this.wallAttach;
	}

	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	// Attention
	public void setX(int xv) {
		this.x = xv;
	}
	public void setY(int yv){
		this.y = yv;
	}
	
	public boolean isWater(){
		return this.water;
	}
	
	public void addInfoParam(String param){
		if (this.info == null){
			setInfo(param);
			return;
		}
		StringBuilder sb = new StringBuilder();
		int counter=0, counter2=0, toS=0;
		String prefix = "";
		for (String oldP : this.info.split(";")){
			if (!param.contains(oldP.split("#")[0])){
				sb.append(prefix);
				prefix = ";";
				sb.append(oldP);
				counter++;
			} else {
				toS++;
			}
		}
		if (toS != this.info.split(";").length){
			sb.append(";");
		}
		for (String pm : param.split(";")){
			String key = pm.split("#")[0];
			String value = pm.split("#")[1];
			sb.append(key+"#"+value);
			if (counter2+1 != param.split(";").length){
				sb.append(";");
			}
			counter2++;
		}
		setInfo(sb.toString());
	}
	
	public int checkInfoKey(String key){
		int counter = 0;
		for (String pair : this.getInfo().split(";")){
			String Ckey = pair.split("#")[0];
			if (Ckey.equals(key)){
				return counter;
			}
			counter++;
		}
		return -1;
	}
	
	public static int checkInfoKey(String inf, String key){
		int counter = 0;
		for (String pair : inf.split(";")){
			String Ckey = pair.split("#")[0];
			if (Ckey.equals(key)){
				return counter;
			}
			counter++;
		}
		return -1;
	}
	
	public void setInfo(String i){
		this.info = i;
		if (this.type.equals(World.PARALLEL_BLOCK)){
			if (this.info != null){
				int counter = 0;
				parallelBlockData = new String[this.info.split(";").length];
				for (String infoPart : this.info.split(";")){
					parallelBlockData[counter] = infoPart.split("#")[1];
					counter++;
				}
			}
			this.category = parallelBlockData[checkInfoKey("category")];
		}
	}
	
	public String getInfo(){
		return this.info;
	}

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
				return new Block(World.ELEVATOR, x1, y1, i);
			case 8:
				return new Block(World.C_SPIKE, x1, y1, i);
			case 9:
				return new Block(World.D_WARNING, x1, y1, i);
			case 10:
				return new Block(World.PARALLEL_BLOCK, x1, y1, i);
			case 11:
				return new Block(World.D_ARROW, x1, y1, i);
			case 12:
				return new Block(World.OXYGEN_POINT, x1, y1, i);
			case 13:
				return new Block(World.D_PLANT, x1, y1, i);
			case 14:
				return new Block(World.D_CONE, x1, y1, i);
			case 15:
				return new Block(World.D_STONES, x1, y1, i);
			case 16:
				return new Block(World.D_BUSH, x1, y1, i);
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
	
	public boolean activeBlockAround(World w){
		if (w.previewMode || w.getAllLights()){
			return true;
		}
		for (int y = getY()-LIGHT_AREA*2; y <= getY()+LIGHT_AREA*2; y++){
			for (int x = getX()-LIGHT_AREA*2; x <= getX()+LIGHT_AREA*2; x++){
				if (w.getEngineeringWorld().getBlockAt(x, y) != null){
					if (w.getEngineeringWorld().getBlockAt(x, y).getType().equals(EngBlock.LED) && w.getEngineeringWorld().getBlockAt(x, y).isActive()){
						for (int y1 = y-LIGHT_AREA; y1 <= y+LIGHT_AREA; y1++){
							for (int x1 = x-LIGHT_AREA; x1 <= x+LIGHT_AREA; x1++){
								if (w.getBlockAt(x1, y1) == this){
									return true;
								}
							}
						}
					}
				}
			}
		}
		return false;
	}
	
	public static void drawAirBlock(GraphicsContext pen, int px, int py){
		pen.drawImage(new Image("file://" + Editor.changeSlash(PATH) + ".labyrinthgame/Images/blocks/block_air.png"), px * World.BLOCK_WIDTH, py * World.BLOCK_WIDTH, World.BLOCK_WIDTH, World.BLOCK_WIDTH);
	}
	
	public static void drawWarningSign(GraphicsContext pen, int px, int py){
		pen.drawImage(new Image("file://" + Editor.changeSlash(PATH) + ".labyrinthgame/Images/editor/warning.png"), px * World.BLOCK_WIDTH, py * World.BLOCK_WIDTH, World.BLOCK_WIDTH, World.BLOCK_WIDTH);
	}
	
	public void addConn(String d){
		if (!getType().equals(World.WALL)){
			return;      //Method only available for wall block
		}
		if (getInfo().split(";")[checkInfoKey("conn")].split("#")[1].equals("null")){
			addInfoParam("conn#"+d);
			return;
		}
		
		StringBuilder builder = new StringBuilder();
		String[] directions = new String[]{"n", "e", "s", "w"};
		for (String c : directions){
			if (getInfo().split(";")[checkInfoKey("conn")].split("#")[1].contains(c) || d.equals(c)){
				builder.append(c);
			}
		}
		addInfoParam("conn#"+builder.toString());
	}
	
	public void removeConn(String d){
		if (!getType().equals(World.WALL)){
			return;      //Method only available for wall block
		}
		if (d.equals(getInfo().split(";")[checkInfoKey("conn")].split("#")[1])){
                    addInfoParam("conn#null");
                    return;
		}
		if (!getInfo().split(";")[checkInfoKey("conn")].split("#")[1].contains(d)){
                    return;
		}
                if (getInfo().split(";")[checkInfoKey("conn")].split("#")[1].equals("null")){
                    return;
                }
		StringBuilder sb = new StringBuilder(getInfo().split(";")[checkInfoKey("conn")].split("#")[1]);
		sb.deleteCharAt(getInfo().split(";")[checkInfoKey("conn")].split("#")[1].indexOf(d));
		addInfoParam("conn#"+sb.toString());
	}
	
	public static int getSpriteCoords(String data, boolean complete, boolean sort){
		String[] names;
		if (complete){
			if (!sort){
				names = new String[]{"e", "es", "esw", "ew", "n", "ne", "nes", "nesw", "new", "ns", "nsw", "null", "nw", "s", "sw", "w"};
			} else {
				names = new String[]{"n", "e", "s", "w", "ne", "es", "sw", "nw", "nes", "esw", "nws", "new", "nesw", "null", "ns", "ew"};
			}
		} else {
			if (!sort){
				names = new String[]{"e", "n", "s", "w"};
			} else {
				names = new String[]{"n", "e", "s", "w"};
			}
		}
		int foundAt = -1, foundNow = 1;
		for (String name : names){
			if (data.equals(name)){
				foundAt = foundNow;
				break;
			}
			foundNow += World.DEFAULT_BLOCK_WIDTH+2;
		}
		return foundAt;
	}
	
	public void draw(GraphicsContext pen, int px, int py, World w) {
		
		/*
		 * underwater:
		 * 
		 * pen.setFill(Color.CYAN);
		 * pen.setGlobalAlpha(0.5);
		 * pen.fillRect(px * World.BLOCK_WIDTH, py * World.BLOCK_WIDTH, World.BLOCK_WIDTH, World.BLOCK_WIDTH);
		 * pen.setGlobalAlpha(1);
		 */
		 
		 ColorAdjust effect = new ColorAdjust();
		 if (w.getEngineeringWorld() != null){
			if (!getType().equals(World.PARALLEL_BLOCK)){
				if (!activeBlockAround(w)){
					effect.setBrightness(DARK);
				} else {
					effect.setBrightness(LIGHT);
				}
			} else if ((parallelBlockData[checkInfoKey("type")].equals(EngBlock.LED) && w.getEngineeringWorld().getBlockAt(getX(), getY()).isActive()) || activeBlockAround(w)){
				effect.setBrightness(LIGHT);
			} else {
				effect.setBrightness(DARK);
			}
		 } else {
			effect.setBrightness(LIGHT);
	 	}
		
	 	if (w instanceof EditableWorld){
			pen.setEffect(null);
		} else {
	 		pen.setEffect(effect);
	 	}
		
		switch (getType()){
			case World.WALL:
				pen.drawImage(new Image("file://" + Editor.changeSlash(PATH) + ".labyrinthgame/Images/blocks/block_wall"+(checkInfoKey("plant") >= 0 && this.info.split(";")[checkInfoKey("plant")].split("#")[1].equals("y") ? "_plant" : "")+".png"), getSpriteCoords(getInfo().split(";")[checkInfoKey("conn")].split("#")[1], true, false), 1, World.DEFAULT_BLOCK_WIDTH, World.DEFAULT_BLOCK_WIDTH, px * World.BLOCK_WIDTH, py * World.BLOCK_WIDTH, World.BLOCK_WIDTH, World.BLOCK_WIDTH);
				break;
			case World.AIR:
				drawAirBlock(pen, px, py);
				break;
			case World.VOID:
				pen.drawImage(new Image("file://" + Editor.changeSlash(PATH) + ".labyrinthgame/Images/blocks/block_void.png"), px * World.BLOCK_WIDTH, py * World.BLOCK_WIDTH, World.BLOCK_WIDTH, World.BLOCK_WIDTH);
				break;
			case World.SPIKE:
				drawAirBlock(pen, px, py);
				pen.drawImage(new Image("file://" + Editor.changeSlash(PATH) + ".labyrinthgame/Images/blocks/block_spike.png"), px * World.BLOCK_WIDTH, py * World.BLOCK_WIDTH, World.BLOCK_WIDTH, World.BLOCK_WIDTH);
				break;
			case World.PORTAL:
				drawAirBlock(pen, px, py);
				pen.drawImage(new Image("file://" + Editor.changeSlash(PATH) + ".labyrinthgame/Images/blocks/block_portal.png"), px * World.BLOCK_WIDTH, py * World.BLOCK_WIDTH, World.BLOCK_WIDTH, World.BLOCK_WIDTH);
				if (this.info.split(";")[checkInfoKey("point")].split("#")[1].equals("NoPointSet") && w instanceof EditableWorld){
					drawWarningSign(pen, px, py);
				}
				break;
			case World.SHOOTER:
				String d = Character.toString(this.getInfo().split(";")[checkInfoKey("direction")].split("#")[1].charAt(0));
				StringBuilder attachBuilder = new StringBuilder();
				boolean isAny = false;
				if (w.getBlockAt(getX(), getY()-1) != null && w.getBlockAt(getX(), getY()-1).getCategory().equals(World.WALL)){
					attachBuilder.append("n");
					isAny = true;
				}
				if (w.getBlockAt(getX()+1, getY()) != null && w.getBlockAt(getX()+1, getY()).getCategory().equals(World.WALL)){
					attachBuilder.append("e");
					isAny = true;
				}
				if (w.getBlockAt(getX(), getY()+1) != null && w.getBlockAt(getX(), getY()+1).getCategory().equals(World.WALL)){
					attachBuilder.append("s");
					isAny = true;
				}
				if (w.getBlockAt(getX()-1, getY()) != null && w.getBlockAt(getX()-1, getY()).getCategory().equals(World.WALL)){
					attachBuilder.append("w");
					isAny = true;
				}
				String attach;
				if (isAny){
					attach = attachBuilder.toString();
				} else {
					attach = "null";
				}
				World.drawRotatedImage(pen, "file://" + Editor.changeSlash(PATH) + ".labyrinthgame/Images/blocks/block_shooter", px * World.BLOCK_WIDTH, py * World.BLOCK_WIDTH, World.BLOCK_WIDTH, d, true, true, true, attach);
				break;
			case World.BAT_GEN:
				drawAirBlock(pen, px, py);
				if (w instanceof EditableWorld || w.previewMode){
					String dir;
					if (!this.info.split(";")[checkInfoKey("data")].split("#")[1].equals("NoDataSet")){
						dir = this.info.split("#")[1].split(" ")[1];
					} else {
						dir = Entity.HORIZONTAL;
					}
					Image batImg = new Image("file://" + Editor.changeSlash(PATH) + ".labyrinthgame/Images/entities/"+((dir.equals(Entity.HORIZONTAL)) ? "bat_side.png" : "bat_front.png"));
					pen.drawImage(batImg, 1+(World.DEFAULT_BLOCK_WIDTH+2)*(dir.equals(Entity.HORIZONTAL) ? 0 : 2), 1, World.DEFAULT_BLOCK_WIDTH, World.DEFAULT_BLOCK_WIDTH, px*World.BLOCK_WIDTH, py*World.BLOCK_WIDTH, World.BLOCK_WIDTH, World.BLOCK_WIDTH);
					if (this.info.split(";")[checkInfoKey("data")].split("#")[1].equals("NoDataSet")){
						drawWarningSign(pen, px, py);
					}
				}
				break;
			case World.ELEVATOR:
				drawAirBlock(pen, px, py);
				if (w instanceof EditableWorld || w.previewMode){
				  pen.drawImage(new Image("file://" + Editor.changeSlash(PATH) + ".labyrinthgame/Images/entities/move_block.png"), px * World.BLOCK_WIDTH, py * World.BLOCK_WIDTH, World.BLOCK_WIDTH, World.BLOCK_WIDTH);
					if (this.info.split(";")[checkInfoKey("data")].split("#")[1].equals("NoDataSet")){
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
				if (w instanceof EditableWorld || w.previewMode){
					pen.drawImage(new Image("file://" + Editor.changeSlash(PATH) + ".labyrinthgame/Images/blocks/block_spike_closed.png"), px * World.BLOCK_WIDTH, py * World.BLOCK_WIDTH, World.BLOCK_WIDTH, World.BLOCK_WIDTH);
				}
				break;
			case World.D_WARNING:
				drawAirBlock(pen, px, py);
				pen.drawImage(new Image("file://" + Editor.changeSlash(PATH) + ".labyrinthgame/Images/blocks/decoration_warning.png"), px * World.BLOCK_WIDTH, py * World.BLOCK_WIDTH, World.BLOCK_WIDTH, World.BLOCK_WIDTH);
				break;
			case World.PARALLEL_BLOCK:
				drawAirBlock(pen, px, py);
				if (!parallelBlockData[checkInfoKey("type")].equals(EngBlock.DOOR) || (parallelBlockData[checkInfoKey("type")].equals(EngBlock.DOOR) && (w instanceof EditableWorld || w.previewMode))){
					pen.drawImage(new Image("file://" + Editor.changeSlash(PATH) + ".labyrinthgame/Images/"+parallelBlockData[checkInfoKey("imagePath")]), 1+(w.getEngineeringWorld().getBlockAt(getX(), getY()).isActive() ? 1 : 0)*(World.DEFAULT_BLOCK_WIDTH + 2), 1, World.DEFAULT_BLOCK_WIDTH, World.DEFAULT_BLOCK_WIDTH, px * World.BLOCK_WIDTH, py * World.BLOCK_WIDTH, World.BLOCK_WIDTH, World.BLOCK_WIDTH);
				}
				break;
			case World.D_ARROW:
				drawAirBlock(pen, px, py);
				String direct = Character.toString(this.getInfo().split(";")[checkInfoKey("direction")].split("#")[1].charAt(0));
				World.drawRotatedImage(pen, "file://" + Editor.changeSlash(PATH) + ".labyrinthgame/Images/blocks/decoration_arrow", px * World.BLOCK_WIDTH, py * World.BLOCK_WIDTH, World.BLOCK_WIDTH, direct, true, false, false, null);
				break;
			case World.OXYGEN_POINT:
				drawAirBlock(pen, px, py);
				pen.drawImage(new Image("file://" + Editor.changeSlash(PATH) + ".labyrinthgame/Images/blocks/oxygen_point.png"), px * World.BLOCK_WIDTH, py * World.BLOCK_WIDTH, World.BLOCK_WIDTH, World.BLOCK_WIDTH);
				break;
			case World.D_PLANT:
				drawAirBlock(pen, px, py);
				pen.drawImage(new Image("file://" + Editor.changeSlash(PATH) + ".labyrinthgame/Images/blocks/decoration_plant.png"), px * World.BLOCK_WIDTH, py * World.BLOCK_WIDTH, World.BLOCK_WIDTH, World.BLOCK_WIDTH);
				break;
			case World.D_CONE:
				drawAirBlock(pen, px, py);
				pen.drawImage(new Image("file://" + Editor.changeSlash(PATH) + ".labyrinthgame/Images/blocks/decoration_cone.png"), px * World.BLOCK_WIDTH, py * World.BLOCK_WIDTH, World.BLOCK_WIDTH, World.BLOCK_WIDTH);
				break;
			case World.D_STONES:
				drawAirBlock(pen, px, py);
				pen.drawImage(new Image("file://" + Editor.changeSlash(PATH) + ".labyrinthgame/Images/blocks/decoration_stones.png"), px * World.BLOCK_WIDTH, py * World.BLOCK_WIDTH, World.BLOCK_WIDTH, World.BLOCK_WIDTH);
				break;
			case World.D_BUSH:
				drawAirBlock(pen, px, py);
				pen.drawImage(new Image("file://" + Editor.changeSlash(PATH) + ".labyrinthgame/Images/blocks/decoration_bush.png"), px * World.BLOCK_WIDTH, py * World.BLOCK_WIDTH, World.BLOCK_WIDTH, World.BLOCK_WIDTH);
				break;
			default:
				pen.setFill(Color.RED);
				pen.fillRect(px * World.BLOCK_WIDTH, py * World.BLOCK_WIDTH, World.BLOCK_WIDTH, World.BLOCK_WIDTH);
				break;
		}
		if (this.water){
			pen.setFill(Color.CYAN);
			pen.setGlobalAlpha(0.5);
			pen.fillRect(px * World.BLOCK_WIDTH, py * World.BLOCK_WIDTH, World.BLOCK_WIDTH, World.BLOCK_WIDTH);
			pen.setGlobalAlpha(1);
		}
		pen.setEffect(null);
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
				case World.ELEVATOR:
					return 7;
				case World.C_SPIKE:
					return 8;
				case World.D_WARNING:
					return 9;
				case World.PARALLEL_BLOCK:
					return 10;
				case World.D_ARROW:
					return 11;
				case World.OXYGEN_POINT:
					return 12;
				case World.D_PLANT:
					return 13;
				case World.D_CONE:
					return 14;
				case World.D_STONES:
					return 15;
				case World.D_BUSH:
					return 16;
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
		return "Block Type: " + this.type + " X:" + this.x + " Y:" + this.y + " Info: " + ((this.info == null) ? "No info" : this.info) + " Water: " + this.water;
	}
}
