package com.orangomango.labyrinth.engineering;

import javafx.scene.canvas.*;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;

import com.orangomango.labyrinth.World;
import com.orangomango.labyrinth.Block;
import com.orangomango.labyrinth.menu.editor.Editor;
import static com.orangomango.labyrinth.menu.editor.Editor.PATH;

public class EngBlock {
	private int x;
	private int y;
	private String type;
	private boolean active = false;
	private boolean activable = false;
	private EngWorld world;
	private String category;
	private String path;
	private String info;

	//Categories
	public static final String SIGNAL_EXTENDER = "signal_extender";
	public static final String SIGNAL_INPUT = "signal_input";
	public static final String SIGNAL_OUTPUT = "signal_output";
	public static final String SIGNAL_GENERATOR = "signal_generator";
	public static final String NO_SIGNAL = "no_signal";

	// Blocks
	public static final String CABLE = "cable";
	public static final String LEVER = "lever";
	public static final String LED = "led";
	public static final String GENERATOR = "generator";
	public static final String AIR = "air";
	public static final String DOOR = "door";

	public String getType() {
		return this.type;
	}
	
	public int getX(){
		return this.x;
	}
	
	public int getY(){
		return this.y;
	}

	public void setWorld(EngWorld w) {
		this.world = w;
		this.path = getType().equals(CABLE) ? w.getAtt(getX(), getY())[2] : ""; // Get image path
		if (this.info == null && getType().equals(CABLE)) {
			this.info = "attachments#" + w.getAtt(getX(), getY())[1];
		}
	}

	public String getCategory() {
		return this.category;
	}
	
	public void setInfo(String i){
		this.info = i;
	}

	private boolean containsDirection(EngBlock b, String d) {
		if (b.getCategory().equals(SIGNAL_OUTPUT)) {
			return true;
		}
		if (b.getInfo() != null) {
			if (!b.getInfo().split("#")[0].equals("attachments")) {
				return false;
			}
			for (char c: b.getInfo().split("#")[1].toCharArray()) {
				if (Character.toString(c).equals(d)) {
					return true;
				}
			}
		}
		return false;
	}

	private void setActiveNESW(int x, int y, boolean a, String f) {
		if (this.world.getBlockAt(x, y - 1) != null && !f.equals("n")) {
			if (this.world.getBlockAt(x, y - 1).isActivable()) {
				this.world.getBlockAt(x, y - 1).toggleActive();
				if (this.world.getBlockAt(x, y - 1).getCategory().equals(SIGNAL_EXTENDER))
					setActiveNESW(x, y - 1, a, "s");
			}
		}
		if (this.world.getBlockAt(x + 1, y) != null && !f.equals("e")) {
			if (this.world.getBlockAt(x + 1, y).isActivable()) {
				this.world.getBlockAt(x + 1, y).toggleActive();
				if (this.world.getBlockAt(x + 1, y).getCategory().equals(SIGNAL_EXTENDER))
					setActiveNESW(x + 1, y, a, "w");
			}
		}
		if (this.world.getBlockAt(x, y + 1) != null && !f.equals("s")) {
			if (this.world.getBlockAt(x, y + 1).isActivable()) {
				this.world.getBlockAt(x, y + 1).toggleActive();
				if (this.world.getBlockAt(x, y + 1).getCategory().equals(SIGNAL_EXTENDER))
					setActiveNESW(x, y + 1, a, "n");
			}
		}
		if (this.world.getBlockAt(x - 1, y) != null && !f.equals("w")) {
			if (this.world.getBlockAt(x - 1, y).isActivable()) {
				this.world.getBlockAt(x - 1, y).toggleActive();
				if (this.world.getBlockAt(x - 1, y).getCategory().equals(SIGNAL_EXTENDER))
					setActiveNESW(x - 1, y, a, "e");
			}
		}
	}

	public void toggleActive() {
		if (isActive()) {
			setActive(false);
		} else {
			setActive(true);
		}
	}

	public void setActive(boolean a) {
		// Later on, put switch instead of else if

		if (getCategory().equals(SIGNAL_GENERATOR) && isActive()) {
			return;
		}
		if (getCategory().equals(SIGNAL_OUTPUT)) {
			this.world.getConnected(getX(), getY(), "");
			boolean found = false;
			for (EngBlock b: this.world.getFoundBlocks()) {
				if (b.getCategory().equals(SIGNAL_GENERATOR)) {
					found = true;
				}
			}
			if (!found) {
				return;
			}
		}
		this.active = a;
		if (getCategory().equals(SIGNAL_INPUT)) {
			setActiveNESW(getX(), getY(), a, "");
		}
	}
	
	public void toggleType(String t){
		if (getType().equals(AIR)){
			this.type = t;
		} else {
			this.type = AIR;
			setInfo(null);
		}
	}

	public boolean isActive() {
		return this.active;
	}

	public boolean isActivable() {
		return this.activable;
	}

	public String getInfo() {
		return this.info;
	}

	@Override
	public String toString() {
		return "Block:"+this.type+" at x:"+this.x+" y:"+this.y+" Active:"+isActive()+" Cat:"+getCategory()+" Info:"+getInfo();
	}

	public EngBlock(int x, int y, String type, String info) {
		this.info = info;
		this.x = x;
		this.y = y;
		this.type = type;
		switch (this.type) {
			case AIR:
				this.category = NO_SIGNAL;
				break;
			case CABLE:
				this.category = SIGNAL_EXTENDER;
				this.activable = true;
				break;
			case LEVER:
				this.category = SIGNAL_INPUT;
				break;
			case GENERATOR:
				this.category = SIGNAL_GENERATOR;
				this.activable = true;
				setActive(true);
				break;
			case LED:
				this.category = SIGNAL_OUTPUT;
				this.activable = true;
				break;
			case DOOR:
				this.category = SIGNAL_OUTPUT;
				this.activable = true;
				break;
		}
	}
	
	public void draw(GraphicsContext pen){
		switch (getType()){
			case AIR:
				Block.drawAirBlock(pen, getX(), getY());
				break;
			case CABLE:
				Block.drawAirBlock(pen, getX(), getY());
				pen.drawImage(new Image("file://" + Editor.changeSlash(PATH) + ".labyrinthgame/Images/engineering/blocks/"+this.world.getAtt(getX(), getY())[2]), getX() * World.BLOCK_WIDTH, getY() * World.BLOCK_WIDTH, World.BLOCK_WIDTH, World.BLOCK_WIDTH);
				break;
			case LEVER:
				Block.drawAirBlock(pen, getX(), getY());
				pen.drawImage(new Image("file://" + Editor.changeSlash(PATH) + ".labyrinthgame/Images/engineering/blocks/lever_off.png"), getX() * World.BLOCK_WIDTH, getY() * World.BLOCK_WIDTH, World.BLOCK_WIDTH, World.BLOCK_WIDTH);
				break;
			case GENERATOR:
				pen.drawImage(new Image("file://" + Editor.changeSlash(PATH) + ".labyrinthgame/Images/engineering/blocks/generator_1.png"), getX() * World.BLOCK_WIDTH, getY() * World.BLOCK_WIDTH, World.BLOCK_WIDTH, World.BLOCK_WIDTH);
				break;
			case DOOR:
				Block.drawAirBlock(pen, getX(), getY());
				pen.drawImage(new Image("file://" + Editor.changeSlash(PATH) + ".labyrinthgame/Images/engineering/blocks/door_4.png"), getX() * World.BLOCK_WIDTH, getY() * World.BLOCK_WIDTH, World.BLOCK_WIDTH, World.BLOCK_WIDTH);
				break;
			default:
				pen.setFill(Color.RED);
				pen.fillRect(getX() * World.BLOCK_WIDTH, getY() * World.BLOCK_WIDTH, World.BLOCK_WIDTH, World.BLOCK_WIDTH);
		}
	}

	public static EngBlock fromInt(int x, int x1, int y1, String i) {
		switch (x) {
			case 0:
				return new EngBlock(x1, y1, AIR, i);
			case 1:
				return new EngBlock(x1, y1, CABLE, i);
			case 2:
				return new EngBlock(x1, y1, LEVER, i);
			case 3:
				return new EngBlock(x1, y1, GENERATOR, i);
			case 4:
				return new EngBlock(x1, y1, LED, i);
			case 5:
				return new EngBlock(x1, y1, DOOR, i);
			default:
				return null;
		}
	}
	
	public Integer toInt(){
		switch(getType()){
			case AIR:
				return 0;
			case CABLE:
				return 1;
			case LEVER:
				return 2;
			case GENERATOR:
				return 3;
			case LED:
				return 4;
			case DOOR:
				return 5;
			default:
				return null;
		}
	}
}
