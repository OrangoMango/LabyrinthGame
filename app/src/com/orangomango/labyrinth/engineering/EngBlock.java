package com.orangomango.labyrinth.engineering;

import javafx.scene.canvas.*;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;
import javafx.animation.*;
import javafx.util.Duration;
import javafx.scene.effect.ColorAdjust;

import java.util.Random;

import com.orangomango.labyrinth.World;
import com.orangomango.labyrinth.Block;
import com.orangomango.labyrinth.menu.editor.Editor;
import com.orangomango.labyrinth.menu.editor.EditableWorld;
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
	private int imageIndex;
	private int randomImageIndexStart = -1;
	
	private int counter;
	private Timeline t;
	private int max;

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
	
	public void addInfoParam(String param){
		// See function: com.orangomango.labyrinth.Block#addInfoParam()
		if (this.info == null){
			setInfo(param);
			return;
		}
		StringBuilder sb = new StringBuilder();
		int counter=0, counter2=0, toS=0;
		for (String oldP : this.info.split(";")){
			if (!param.contains(oldP.split("#")[0])){
				sb.append(oldP);
				if (counter+1 != this.info.split(";").length-toS){
					sb.append(";");
				}
				counter++;
			} else {
				toS++;
			}
		}
		if (counter > 0){
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
		return Block.checkInfoKey(this.info, key);
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

		if (getCategory().equals(SIGNAL_GENERATOR) && isActive()) {
			return;
		}
		if (getCategory().equals(SIGNAL_OUTPUT)) {
			this.world.foundBlocks = new EngBlock[0];
			this.world.getConnected(getX(), getY(), "");
			boolean found = false;
			for (EngBlock b: this.world.getFoundBlocks()) {
				if (b.getCategory().equals(SIGNAL_GENERATOR)) {
					found = true;
					break;
				}
			}
			if (!found) {
				return;
			}
		}
		this.active = a;
		if (getCategory().equals(SIGNAL_INPUT)) {
			//doActivate = false;
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
				this.imageIndex = 0;
				this.randomImageIndexStart = 2;
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
	
	public void makeAnimation(int images, int time){
		counter = 0;
		max = images;
		t = new Timeline(new KeyFrame(Duration.millis(time), event -> {		
			this.imageIndex = counter;
			if (counter+1 != max){
				counter++;
			} else {
				int extra = 0;
				// Random start
				if (this.randomImageIndexStart > 0){
					Random rnd = new Random();
					extra = rnd.nextInt(this.randomImageIndexStart);
				}
				counter = extra * images;
				max = counter + images;
			}
			this.world.getBigWorld().update(0, 0, 0, 0);
		}));
		t.setCycleCount(Animation.INDEFINITE);
		t.play();
	}
	
	public void stopAnimation(){
		t.stop();
	}
	
	public void drawAirBlock(GraphicsContext pen, int px, int py){
		pen.drawImage(new Image("file://" + Editor.changeSlash(PATH) + ".labyrinthgame/Images/engineering/blocks/block_air.png"), px * World.BLOCK_WIDTH, py * World.BLOCK_WIDTH, World.BLOCK_WIDTH, World.BLOCK_WIDTH);
	}
	
	public void draw(GraphicsContext pen, World w){
		
		ColorAdjust effect = new ColorAdjust();
		
		if ((getType().equals(LED) && isActive()) || this.world.getBigWorld().getBlockAt(getX(), getY()).activeBlockAround(this.world.getBigWorld())){
			effect.setBrightness(Block.LIGHT);
		} else {
			effect.setBrightness(Block.DARK);
		}
		
		if (w instanceof EditableWorld){
			effect.setBrightness(Block.LIGHT);
		}
		
		pen.setEffect(effect);
		
		if (getType() == CABLE){
			this.info = "attachments#" + this.world.getAtt(getX(), getY())[1];
		}
		switch (getType()){
			case AIR:
				drawAirBlock(pen, getX(), getY());
				break;
			case CABLE:
				drawAirBlock(pen, getX(), getY());
				pen.drawImage(new Image("file://" + Editor.changeSlash(PATH) + ".labyrinthgame/Images/engineering/blocks/cable.png"), Integer.parseInt(this.world.getAtt(getX(), getY())[2]), 1, World.DEFAULT_BLOCK_WIDTH, World.DEFAULT_BLOCK_WIDTH, getX() * World.BLOCK_WIDTH, getY() * World.BLOCK_WIDTH, World.BLOCK_WIDTH, World.BLOCK_WIDTH);
				break;
			case LEVER:
				drawAirBlock(pen, getX(), getY());
				pen.drawImage(new Image("file://" + Editor.changeSlash(PATH) + ".labyrinthgame/Images/engineering/blocks/lever.png"), 1+(World.DEFAULT_BLOCK_WIDTH+2)*(isActive() ? 1 : 0), 1, World.DEFAULT_BLOCK_WIDTH, World.DEFAULT_BLOCK_WIDTH, getX() * World.BLOCK_WIDTH, getY() * World.BLOCK_WIDTH, World.BLOCK_WIDTH, World.BLOCK_WIDTH);
				break;
			case GENERATOR:
				pen.drawImage(new Image("file://" + Editor.changeSlash(PATH) + ".labyrinthgame/Images/engineering/blocks/generator.png"), 1+34*this.imageIndex, 1, World.DEFAULT_BLOCK_WIDTH, World.DEFAULT_BLOCK_WIDTH, getX() * World.BLOCK_WIDTH, getY() * World.BLOCK_WIDTH, World.BLOCK_WIDTH, World.BLOCK_WIDTH);
				break;
			case DOOR:
				drawAirBlock(pen, getX(), getY());
				if (w instanceof EditableWorld){
					pen.drawImage(new Image("file://" + Editor.changeSlash(PATH) + ".labyrinthgame/Images/engineering/blocks/door.png"), 1+(World.DEFAULT_BLOCK_WIDTH+2)*3, 1, World.DEFAULT_BLOCK_WIDTH, World.DEFAULT_BLOCK_WIDTH, getX() * World.BLOCK_WIDTH, getY() * World.BLOCK_WIDTH, World.BLOCK_WIDTH, World.BLOCK_WIDTH);
				}
				break;
			case LED:
				drawAirBlock(pen, getX(), getY());
				pen.drawImage(new Image("file://" + Editor.changeSlash(PATH) + ".labyrinthgame/Images/engineering/blocks/led.png"), 1+(World.DEFAULT_BLOCK_WIDTH+2)*(isActive() ? 1 : 0), 1, World.DEFAULT_BLOCK_WIDTH, World.DEFAULT_BLOCK_WIDTH, getX() * World.BLOCK_WIDTH, getY() * World.BLOCK_WIDTH, World.BLOCK_WIDTH, World.BLOCK_WIDTH);
				break;
			default:
				pen.setFill(Color.RED);
				pen.fillRect(getX() * World.BLOCK_WIDTH, getY() * World.BLOCK_WIDTH, World.BLOCK_WIDTH, World.BLOCK_WIDTH);
		}
		pen.setEffect(null);
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
