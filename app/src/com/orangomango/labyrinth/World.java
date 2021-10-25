/**
   Labirinth game - world class
   @author OrangoMango
   @version 3.4
*/

package com.orangomango.labyrinth;

import java.io.*;
import java.util.Arrays;

import javafx.scene.canvas.*;
import javafx.stage.Stage;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.image.Image;

import com.orangomango.labyrinth.menu.editor.Editor;
import com.orangomango.labyrinth.menu.editor.EditableWorld;
import static com.orangomango.labyrinth.menu.editor.Editor.PATH;
import com.orangomango.labyrinth.menu.play.entity.*;
import com.orangomango.labyrinth.menu.editor.LevelStats;

// Engineering mode
import com.orangomango.labyrinth.engineering.*;

public class World {
	protected Block[][] world;
	protected String filePath;
	public int height, width;
	public int[] start, end;
	private GraphicsContext pen;
	private Player player;
	protected Canvas canvas;
	private Entity[] ents = new Entity[0];
	private boolean playerView = false;
	private EngWorld engW = null;
	private String drawingMode = "normal";
	public boolean previewMode = false;
	private LevelStats levelStats = null;
	public boolean allLights = false;

	public final static String NORTH = "n";
	public final static String SOUTH = "s";
	public final static String EAST = "e";
	public final static String WEST = "w";

	public final static String WALL = "wall";
	public final static String AIR = "air";
	public final static String VOID = "void";
	public final static String SPIKE = "spike";
	public final static String C_SPIKE = "closable_spike";
	public final static String PORTAL = "portal";
	public final static String SHOOTER = "shooter";
	public final static String BAT_GEN = "bat_generator";
	public final static String ELEVATOR = "elevator";
	public final static String D_WARNING = "decoration_warning";
	public final static String PARALLEL_BLOCK = "parallel_block";

	public static int BLOCK_WIDTH = 32;

	public World(String path) {
		filePath = path;
		world = readWorld(filePath);
	}

	public void setLevelStats(LevelStats ls){
		this.levelStats = ls;
	}
	
	public void updateLevelStats(){
		if (this.levelStats != null){
			this.levelStats.update();
		}
	}

	public void setPlayerView(boolean value) {
		this.playerView = value;
	}

	public boolean getPlayerView() {
		return this.playerView;
	}
	
	public EngWorld getEngineeringWorld(){
		return this.engW;
	}
	
	public void setEngineeringWorld(EngWorld w){
		this.engW = w;
	}

	public void setPen(GraphicsContext pen) {
		this.pen = pen;
	}

	public void setPlayer(Player pl) {
		this.player = pl;
	}

	public Player getPlayer() {
		return this.player;
	}

	public void setCanvas(Canvas canvas) {
		this.canvas = canvas;
	}

	public void setEnts(Entity...e) {
		ents = e;
	}

	public void addEnt(Entity e) {
		this.ents = Arrays.copyOf(ents, ents.length + 1);
		this.ents[ents.length - 1] = e;
	}

	public Entity[] getEnts() {
		return this.ents;
	}
	
	public String getDrawingMode(){
		return this.drawingMode;
	}
	
	public void setDrawingMode(String d){
		this.drawingMode = d;
	}

	public void changeToWorld(String path) {
		filePath = path;
		this.ents = new Entity[0];
		world = readWorld(filePath);
		try {
			this.canvas.setHeight(this.height * BLOCK_WIDTH);
			this.canvas.setWidth(this.width * BLOCK_WIDTH);
		} catch (NullPointerException e) {
			Logger.warning("World canvas is null");
		}
		try {
			this.player.setX(start[0]);
			this.player.setY(start[1]);
		} catch (NullPointerException e) {
			Logger.warning("World player is null");
		}
		update(0, 0, 0, 0);
	}
	
	public void updateParallelBlocks(){
		for (Block[] blockRow : this.world){
			for (Block b : blockRow){
				if (b.getType().equals(World.PARALLEL_BLOCK)){
					String bType = b.parallelBlockData[2];
					EngBlock eb = engW.getBlockAt(b.getX(), b.getY());
					if (bType.equals(EngBlock.LEVER) || bType.equals(EngBlock.LED)){
						b.setInfo("imagePath#engineering/blocks/"+bType+"_"+(eb.isActive() ? "on" : "off")+".png;category#air;type#"+bType);
					}
				}
			}
		}
	}

	public void update(int x, int y, int x1, int y1) {
		try {
			if (x == 0 && y == 0 && x1 == 0 && y1 == 0) {
				this.pen.clearRect(0, 0, this.width * BLOCK_WIDTH, this.height * BLOCK_WIDTH);
				draw();
				this.updateLevelStats();
			} else {
				if (getDrawingMode().equals("engineering")){
					return;
				}
				this.pen.clearRect(0, 0, this.width * BLOCK_WIDTH, this.height * BLOCK_WIDTH); //(x+y) * BLOCK_WIDTH, (x+y) * BLOCK_WIDTH);
				draw(x, y, x1, y1);
			}
		} catch (NullPointerException e) {
			Logger.warning("World pen is null");
		}
	}

	private Block[][] readWorld(String path) {
		File file = new File(path);
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));

			// Get world width and height from file
			String data = reader.readLine();
			this.width = Integer.parseInt(data.split("x")[0]);
			this.height = Integer.parseInt(data.split("x")[1]);

			// Get world layout
			String wData = reader.readLine();
			Block[][] Fworld = parseWorldData(wData, height, width);

			String startData = reader.readLine();
			String endData = reader.readLine();

			start = configureFromString(startData);
			end = configureFromString(endData);
			
			String lightsData = reader.readLine();
			if (lightsData.equals("1")){
				this.allLights = true;
			} else if (lightsData.equals("0")){
				this.allLights = false;
			} else {
				System.out.println("No light data available");
			}

			String engData = reader.readLine();
			if (engData != null) {
				if (engData.equals("engineering_mode")) {
					System.out.println("Eng mode available");
					String engWorldData = reader.readLine();
					EngBlock[][] engWorld = parseEngWorldData(engWorldData, this.height, this.width);  // array, y, x
					this.engW = new EngWorld(this, engWorld, this.width, this.height);
				} else {
					System.out.println("Engineering mode not available (missing string)");
				}
			} else {
				System.out.println("Engineering mode not available (null)");
			}

			reader.close();
			return Fworld;

		} catch (IOException ex) {
			Logger.error("Could not read world");
			return null;
		}
	}

	private int[] configureFromString(String data) {
		String[] split = data.split(",");
		int[] output = new int[2];
		int x = 0;

		for (String i: split) {
			output[x] = Integer.parseInt(i);
			x++;
		}

		return output;
	}
	
	private EngBlock[][] parseEngWorldData(String data, int h, int w){
		
		// Very similar method to parseWorldData()
		
		String[] current = data.split(",");
		EngBlock[][] output = new EngBlock[h][w];
		
		int iterator = 0;
		int counter = 0;
		for (String i: current) {
			EngBlock[] x = new EngBlock[w];
			int it2 = 0;

			if (iterator + w > current.length) { // If iterator is bigger than the list length then stop
				break;
			}
			for (String v: Arrays.copyOfRange(current, iterator, iterator + w)) {
				x[it2] = EngBlock.fromInt(Integer.parseInt(v.split(":")[0]), it2, counter, v.split(":").length > 1 ? v.split(":")[1] : null);
				it2++;
			}
			output[counter] = x;
			iterator += w;
			counter++;
		}

		return output;
	}

	/**
	 * Parse give string from file and return an array
	 * @param data - string with all file data
	 * @param h - world height
	 * @param w - world width
	 */
	private Block[][] parseWorldData(String data, int h, int w) {
		String[] current = data.split(",");
		Block[][] output = new Block[h][w];

		int iterator = 0;
		int counter = 0;
		for (String i: current) {
			Block[] x = new Block[w];
			int it2 = 0;

			if (iterator + w > current.length) { // If iterator is bigger than the list length then stop
				break;
			}
			for (String v: Arrays.copyOfRange(current, iterator, iterator + w)) {
				x[it2] = Block.fromInt(Integer.parseInt(v.split(":")[0]), it2, counter, v.split(":").length > 1 ? v.split(":")[1] : null);
				if (x[it2].getType() == BAT_GEN && !x[it2].getInfo().equals("NoDataSet")) {
					String[] d = x[it2].getInfo().split("#")[1].split(" ");
					addEnt(new Bat(this, x[it2].getX(), x[it2].getY(), Integer.parseInt(d[0]), d[1], Integer.parseInt(d[2]), d[3].equals("t") ? true : false));
				} else if (x[it2].getType() == SHOOTER) {
					String d = Character.toString(x[it2].getInfo().split("#")[1].charAt(0));
					addEnt(new Arrow(this, x[it2].getX(), x[it2].getY(), d));
				} else if (x[it2].getType() == ELEVATOR && !x[it2].getInfo().equals("NoDataSet")) {
					String[] d = x[it2].getInfo().split("#")[1].split(" ");
					addEnt(new Elevator(this, x[it2].getX(), x[it2].getY(), Integer.parseInt(d[0]), d[1]));
				} else if (x[it2].getType() == C_SPIKE) {
					addEnt(new CSpike(this, x[it2].getX(), x[it2].getY()));
				} else if (x[it2].getType() == PARALLEL_BLOCK){
					if (x[it2].parallelBlockData[2].equals(EngBlock.DOOR)){
						addEnt(new ParallelBlock(this, x[it2].getX(), x[it2].getY(), x[it2].getInfo(), new String[][]{{"engineering/blocks/door_1.png", "engineering/blocks/door_2.png", "engineering/blocks/door_3.png", "engineering/blocks/door_4.png"},{"engineering/blocks/door_4.png", "engineering/blocks/door_3.png", "engineering/blocks/door_2.png", "engineering/blocks/door_1.png"}}, "engineering/blocks/door_4.png", "engineering/blocks/door_1.png"));
					}
				}
				it2++;
			}
			output[counter] = x;
			iterator += w;
			counter++;
		}
		return output;
	}

	public void draw() {
		if (getDrawingMode().equals("normal")){
			for (Block[] blocks: world) {
				for (Block block: blocks) {
					block.draw(this.pen, this);
				}
			}
			drawStart(0, 0);
			drawEnd(0, 0);
			this.player.draw(this.pen);
			for (Entity e: this.ents) {
				e.draw(this.pen);
			}
		} else if (getDrawingMode().equals("engineering")){
			for (EngBlock[] blocks: engW.getWorld()) {
				for (EngBlock block: blocks) {
					block.draw(this.pen, this);
				}
			}
			if (!(this instanceof EditableWorld)){
				for (Entity e: this.ents) {
					if (e.engineering){
						e.draw(this.pen);
					}
				}
			}
		}
	}

	public void draw(int x, int y, int x1, int y1) {
		if (getDrawingMode().equals("engineering")){
			return;
		}
		int coux = 0;
		int couy = 0;
		for (int cy = y; cy<= y1; cy++) {
			for (int cx = x; cx<= x1; cx++) {
				Block b = getBlockAt(cx, cy);
				if (b != null) {
					b.draw(this.pen, coux, couy, this);
				} else {
					new Block(VOID, coux, couy, null).draw(this.pen, this);
				}
				coux++;
			}
			coux = 0;
			couy++;
		}
		if ((start[0] >= x && start[0]<= x1) && (start[1] >= y && start[1]<= y1)) {
			drawStart(x, y);
		}
		if ((end[0] >= x && end[0]<= x1) && (end[1] >= y && end[1]<= y1)) {
			drawEnd(x, y);
		}
		for (Entity e: this.ents) {
			if ((e.getX() >= x && e.getX()<= x1) && (e.getY() >= y && e.getY()<= y1)) {
				e.draw(this.pen, e.getX() - x, e.getY() - y);
			}
		}
		if ((this.player.getX() >= x && this.player.getX()<= x1) && (this.player.getY() >= y && this.player.getY()<= y1)) {
			if (this.player.psx == null && this.player.psy == null) {
				this.player.draw(this.pen, this.player.getX() - x, this.player.getY() - y);
			}
		}

		if (this.player.psx != null && this.player.psy != null) {
			if ((this.player.psx >= x && this.player.psx<= x1) && (this.player.psy >= y && this.player.psy<= y1)) {
				this.player.draw(this.pen, this.player.psx - x, this.player.psy - y);
			}
		}
	}

	private void drawStart(int x, int y) {
		this.pen.setStroke(Color.GREEN);
		this.pen.setFont(new Font("Arial", 23/32 * World.BLOCK_WIDTH));
		this.pen.strokeText("S", (start[0] - x) * BLOCK_WIDTH + 2, (start[1] - y) * BLOCK_WIDTH + 22);
	}

	private void drawEnd(int x, int y) {
		this.pen.drawImage(new Image("file://" + Editor.changeSlash(PATH) + ".labyrinthgame/Images/blocks/end.png"), (end[0] - x) * BLOCK_WIDTH, (end[1] - y) * BLOCK_WIDTH, BLOCK_WIDTH, BLOCK_WIDTH);
	}

	public Block getBlockAt(int x, int y) {
		try {
			Block item = world[y][x];
			return item;
		} catch (ArrayIndexOutOfBoundsException e) {
			return null;
		}
	}

	public Block[] getXRow(int y) {
		return world[y];
	}

	public Block[] getYRow(int x) {
		Block[] output = new Block[height];
		int counter = 0;
		for (Block[] i: world) {
			output[counter] = i[x];
			counter++;
		}

		return output;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("World:\n");
		for (Block[] x: world) {
			for (Block b: x) {
				builder.append("|" + b + "|").append(" ");
			}
			builder.append("\n");
		}
		builder.append(String.format("Width: %s, Height: %s.\nStart at: %s, End at: %s", width, height, Arrays.toString(start), Arrays.toString(end)));

		return builder.toString();
	}
}
