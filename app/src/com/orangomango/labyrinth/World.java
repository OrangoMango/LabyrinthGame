/**
   Labirinth game - world class
   @author OrangoMango
   @version 3.6
*/

package com.orangomango.labyrinth;

import java.io.*;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Random;

import javafx.scene.canvas.*;
import javafx.stage.Stage;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.image.Image;
import javafx.animation.*;
import javafx.util.Duration;

import com.orangomango.labyrinth.menu.editor.Editor;
import com.orangomango.labyrinth.menu.editor.EditableWorld;
import static com.orangomango.labyrinth.menu.editor.Editor.PATH;
import com.orangomango.labyrinth.menu.play.entity.*;
import com.orangomango.labyrinth.menu.editor.LevelExe;
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
	private Entity[] ents = new Entity[0];
	private boolean playerView = false;
	private EngWorld engW = null;
	private String drawingMode = "normal";
	protected Canvas canvas;
	public boolean previewMode = false;
	private LevelStats levelStats = null;
	private boolean allLights = false;
	private boolean canUpdate = true;
	public WorldList worldList;
	private boolean showEnd = true;
	public int[] combinedLines;
	private static File lastCreatedFile;
	private static int X_MOVE, Y_MOVE;
	public Timeline viewTime;
	private static boolean VIEWING = false;
	private Stage psStage;
	public boolean warningOnEnd = false;
	private String information = "";

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
	public final static String D_ARROW = "decoration_arrow";
	public final static String D_PLANT = "decoration_plant";
	public final static String D_CONE = "decoration_cone";
	public final static String D_STONES = "decoration_stones";
	public final static String D_BUSH = "decoration_bush";
	public final static String PARALLEL_BLOCK = "parallel_block";
	public final static String OXYGEN_POINT = "oxygen_point";

	public static final int DEFAULT_BLOCK_WIDTH = 32;
	public static int BLOCK_WIDTH = DEFAULT_BLOCK_WIDTH;
	
	public static class WorldList{
		private World[] worlds;
		public WorldList(World... ws){
			this.worlds = ws;
		}
		
		public void addWorld(World w){
			worlds = Arrays.copyOf(worlds, worlds.length+1);
			worlds[worlds.length-1] = w;
		}
		
		public void deleteWorld(int index){
			World[] output = new World[getLength()-1];
			int counter = 0, counter2 = 0;
			for (World wld : this.worlds){
				if (counter != index){
					output[counter2] = wld;
                                        counter2++;
                                }
				counter++;
			}
			this.worlds = output;
		}
		
		public World getWorldAt(int index){
			return worlds[index];
		}
                
		public int getLength(){
			return worlds.length;
		}
		
		@Override
		public String toString(){
			StringBuilder output = new StringBuilder();
				output.append("List length: "+getLength()+"\n");
			for (World wld : worlds){
				output.append(wld).append("\n");
			}
			return output.toString();
		}
		
		public void sync(){
			for (int count = 0; count < worlds.length; count++){
				worlds[count] = new World(worlds[count].getFilePath());
			}
		}
		
		public void updateOnFile(String filePath){
			try {
				updateOnFile(new BufferedWriter(new FileWriter(filePath)));
			} catch (IOException ioe){}
		}
			
		public void updateOnFile(BufferedWriter writer){
			try {
				int cont = 1;
				writer.write("#NumWorlds:"+worlds.length);
				for (World world : worlds){
					writer.newLine();
					writer.write("#World "+cont+"\n");
					world.writeToFile(writer);
					cont++;
				}
				writer.close();
			} catch (IOException e) {
				Logger.error(e.getMessage());
			}
		}
	}
	
	public void updateWorldList(String p){
		this.worldList = new WorldList(new World(p, 0));
		for (int x = 0; x < getArcadeLevels(p); x++){
			if (x == 0){
				continue;
			}
			//System.out.println("FP: "+p);
			World tWorld = new World(p, getFilePathIndex("#World "+(x+1), p));
			tWorld.setFilePath(tWorld.createTempCopyFilePath());
			this.worldList.addWorld(tWorld);
		}
		//System.out.println(this.worldList);
	}

	public World(String path) {
		filePath = path;
		world = readWorld(filePath);
		updateWorldList(filePath);
        this.combinedLines = new int[]{this.height-1};
	}
        
        public World(String path, int index){
            filePath = path;
            world = readWorld(filePath, index);
            // here, worldList = null
        	this.combinedLines = new int[]{this.height-1};
        }
        
        public World(Block[][] blocks, int[] start, int[] end, boolean lights, EngBlock[][] ew, boolean delBefore){
        	world = blocks;
        	this.width = blocks[0].length;
        	this.height = blocks.length;
        	this.start = start;
        	this.end = end;
        	this.setAllLights(lights);
        	this.engW = ew != null ? new EngWorld(this, ew, this.width, this.height) : null;
        	filePath = createTempCopyFilePath(delBefore);
        	this.combinedLines = new int[]{this.height-1};
        	this.updateOnFile();
        }
        
        public void viewFrom(int x, int y, int x1, int y1){
            X_MOVE = x;
            Y_MOVE = y;
            LevelExe.PLAYER_MOVEMENT = false;
            VIEWING = true;
            viewTime = new Timeline(new KeyFrame(Duration.millis(150), evt -> {
                update(X_MOVE-LevelExe.PWS, Y_MOVE-LevelExe.PWS, X_MOVE+LevelExe.PWS, Y_MOVE+LevelExe.PWS, true, true);
                if (X_MOVE != x1){
                    X_MOVE += x1 > x ? 1 : -1;
                } else {
                    if (Y_MOVE != y1){
                        Y_MOVE += y1 > y ? 1 : -1;
                    } else {
                        if (getPlayerView()){
                            update(player.getX()-LevelExe.PWS, player.getY()-LevelExe.PWS, player.getX()+LevelExe.PWS, player.getY()+LevelExe.PWS, true, true);
                        } else {
                            update(0, 0, 0, 0, true, true);
                        }
                        LevelExe.PLAYER_MOVEMENT = true;
                        VIEWING = false;
                    }
                }
            }));
            viewTime.setCycleCount(Math.abs(y1-y)+Math.abs(x1-x)+1);
            viewTime.play();
        }
        
        public String createTempCopyFilePath(){ return createTempCopyFilePath(false); }
        public String createTempCopyFilePath(boolean dbefore){
        	try {
				if (lastCreatedFile != null && dbefore){
					lastCreatedFile.delete();
				}
        		File file = File.createTempFile("temp-world-"+(new Random()).nextInt(), ".wld");
				lastCreatedFile = file;
				file.deleteOnExit();
        		BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        		writeToFile(writer);
        		writer.close();
        		return file.getAbsolutePath();
        	} catch (IOException ioe){
        		Logger.error("Error when creating temp file: "+ioe.getMessage());
        		return null;
        	}
        }
        	
	public void writeToFile(BufferedWriter writer){
		try {
			writer.write(this.information);
			writer.newLine();
			writer.write(this.width + "x" + this.height + "\n");
			int counter = 0;
			for (Block[] bArr: this.world) {
				for (Block block: bArr) {
					writer.write(Integer.toString(block.toInt())+((block.getInfo() == null) ? "" : ":"+block.getInfo()));
					if (counter + 1 != this.height * this.width) {
						writer.write(",");
					}
					counter++;
				}
			}
			writer.newLine();
			writer.write(this.start[0] + "," + this.start[1] + "\n");
			writer.write(this.end[0] + "," + this.end[1] + "\n");
			writer.write(this.getAllLights() ? "1" : "0");
			if (this.getEngineeringWorld() != null){
				writer.write("\nengineering_mode\n");
				for (EngBlock[] bArr: this.getEngineeringWorld().getWorld()) {
					for (EngBlock block: bArr) {
						writer.write(Integer.toString(block.toInt())+((block.getInfo() == null) ? "" : ":"+block.getInfo()));
						if (counter + 1 != this.height * this.width) {
							writer.write(",");
						}
						counter++;
					}
				}
			}
		} catch (IOException ioe){
			Logger.error(ioe.getMessage());
		}
	}
	
	public static void writeNewFile(BufferedWriter writer, int w, int h, int[] startP, int[] endP, boolean lights, String desc){
		try {
			writer.write(desc);
			writer.newLine();
			writer.write(String.format("%sx%s", w, h));
			writer.newLine();
			for (int i = 0; i<w * h; i++) {
				if (i != w * h - 1) {
					writer.write("0,");
				} else {
					writer.write("0");
				}
			}
			writer.newLine();
			writer.write(String.format("%s,%s\n", startP[0], startP[1]));
			writer.write(String.format("%s,%s\n", endP[0], endP[1]));
			writer.write(lights ? "1" : "0");
		} catch (IOException ioe){}
	}
	
	public static int getFilePathIndex(String search, String path){
		try {
			BufferedReader reader = new BufferedReader(new FileReader(path));
			String found = "";
			int counter = 0;
			int index = 0;
			boolean f = false;
			while (found != null){
				found = reader.readLine();
				counter++;
				if (found != null && found.contains(search) && !f){ // The first in the file
					index = counter;
					f = true;
				}
			}
            reader.close();
			return index == 0 ? -1 : index;
		} catch (IOException ex){
           return -1;
        }
	}
	
	public static int getArcadeLevels(String fileName){
		try {
			BufferedReader reader = new BufferedReader(new FileReader(fileName));
			String line1 = reader.readLine();
			if (line1.startsWith("#NumWorlds")){
				int num = Integer.parseInt(line1.split(":")[1]);
				return num;
			}
        	reader.close();
		} catch (IOException ex){}
		return -1;
	}
	
	public static World combineWorlds(World world1, World world2){
		Block[][] output = new Block[world1.height + world2.height][world1.width > world2.width ? world1.width : world2.width];
		int x = 0, y = 0;
		for (Block[] blockRow : world1.getWorld()){
			for (Block block : blockRow){
				output[y][x] = block;
				x++;
			}
			x = 0;
			y++;
		}
		for (Block[] blockRow : world2.getWorld()){
			for (Block block : blockRow){
				output[y][x] = block;
				output[y][x].setY(world1.height+output[y][x].getY());
				if (block.getType().equals(World.PORTAL)){
					String info = block.getInfo().split(";")[block.checkInfoKey("point")].split("#")[1];
					int xC = Integer.parseInt(info.split(" ")[0]), yC = Integer.parseInt(info.split(" ")[1]);
					yC += world1.height;
					block.addInfoParam("point#"+xC+" "+yC);
				}
				x++;
			}
			x = 0;
			y++;
		}
		x = 0;
		y = 0;
		for (Block[] blockRow : output){
			for (Block block : blockRow){
				if (block == null){
					output[y][x] = new Block(VOID, x, y, null);
				}
				x++;
			}
			x = 0;
			y++;
		}
		
		EngBlock[][] eOut;
		if (world1.getEngineeringWorld() != null && world2.getEngineeringWorld() != null){
			eOut = new EngBlock[world1.height + world2.height][world1.width > world2.width ? world1.width : world2.width];
			x = 0;
			y = 0;
			for (EngBlock[] blockRow : world1.getEngineeringWorld().getWorld()){
				for (EngBlock block : blockRow){
					eOut[y][x] = block;
					x++;
				}
				x = 0;
				y++;
			}
			for (EngBlock[] blockRow : world2.getEngineeringWorld().getWorld()){
				for (EngBlock block : blockRow){
					eOut[y][x] = block;
					x++;
				}
				x = 0;
				y++;
			}
			x = 0;
			y = 0;
			for (EngBlock[] blockRow : eOut){
				for (EngBlock block : blockRow){
					if (block == null){
						eOut[y][x] = new EngBlock(x, y, EngBlock.AIR, null);
					}
					x++;
				}
				x = 0;
				y++;
			}
		} else {
			eOut = null;
		}
		World w = new World(output, world1.start, world1.end, world1.getAllLights(), eOut, true);
		int[] cl = new int[world1.combinedLines.length+world2.combinedLines.length];
		int cont = 0;
		for (int i : world1.combinedLines){
			cl[cont] = i;
			cont++;
		}
		for (int i : world2.combinedLines){
			cl[cont] = i+world1.height;
			cont++;
		}
		w.combinedLines = cl;
		w.setShowEnd(false);
		w.updateWalls();
		return w;
	}
	
	public void updateOnFile(){
		updateOnFile(true);
	}
	public void updateOnFile(boolean change) {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(this.filePath));
			writeToFile(writer);
			writer.close();
		} catch (IOException e) {
			Logger.error(e.getMessage());
		}
		if (change){
			int[] temp = this.combinedLines;
			changeToWorld(this.filePath);
			this.combinedLines = temp;
		}
	}
	
	public void updateWalls(){
		for (Block[] blockRow : this.world){
			for (Block b : blockRow){
				if (b.getWallAttach()){
					if (this.getBlockAt(b.getX(), b.getY()-1) != null){
						if (this.getBlockAt(b.getX(), b.getY()-1).getWallAttach()){
						       this.getBlockAt(b.getX(), b.getY()-1).addConn("s");
						} else {
							b.removeConn("n");
						}
					} else {
						b.removeConn("n");
					}
					if (this.getBlockAt(b.getX()+1, b.getY()) != null){
						if (this.getBlockAt(b.getX()+1, b.getY()).getWallAttach()){
						    this.getBlockAt(b.getX()+1, b.getY()).addConn("w");
						} else {
							b.removeConn("e");
						}
					} else {
						b.removeConn("e");
					}
					if (this.getBlockAt(b.getX(), b.getY()+1) != null){
						if (this.getBlockAt(b.getX(), b.getY()+1).getWallAttach()){
						    this.getBlockAt(b.getX(), b.getY()+1).addConn("n");
						} else {
							b.removeConn("s");
						}
					} else {
						b.removeConn("s");
					}
					if (this.getBlockAt(b.getX()-1, b.getY()) != null){
						if (this.getBlockAt(b.getX()-1, b.getY()).getWallAttach()){
						    this.getBlockAt(b.getX()-1, b.getY()).addConn("e");
						} else {
							b.removeConn("w");
						}
					} else {
						b.removeConn("w");
					}
				}
			}
		}
		updateOnFile();
	}

	public void setLevelStats(LevelStats ls){
		this.levelStats = ls;
	}
	
	public LevelStats getLevelStats(){
		return this.levelStats;
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

	public void setCanvas(Canvas canvas) {
		this.canvas = canvas;
	}
	
	public void setPsStage(Stage s){
		this.psStage = s;
	}
	
	public Stage getPsStage(){
		return this.psStage;
	}


	public void setPlayer(Player pl) {
		this.player = pl;
	}

	public Player getPlayer() {
		return this.player;
	}
	
	// Warning: Make attention when calling this method
	public void setFilePath(String path){
		this.filePath = path;
	}
	
	public String getFilePath(){
		return this.filePath;
	}
	
	public Block[][] getWorld(){
		return this.world;
	}

	public void setEnts(Entity... e) {
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
	
	public void setShowEnd(boolean v){
		this.showEnd = v;
	}
	
	public boolean getShowEnd(){
		return this.showEnd;
	}
	
	public void setAllLights(boolean v){
		this.allLights = v;
	}
	
	public boolean getAllLights(){
		return this.allLights;
	}

	public void changeToWorld(String path) {
		this.filePath = path;
		this.ents = new Entity[0];
		world = readWorld(filePath);
		this.combinedLines = new int[]{this.height-1};
        try {
			this.canvas.setHeight(this.height * BLOCK_WIDTH);
			this.canvas.setWidth(this.width * BLOCK_WIDTH);
		} catch (NullPointerException e) {
		}
		/*try {
			this.player.setX(start[0]);
			this.player.setY(start[1]);
		} catch (NullPointerException e) {
			Logger.warning("World player is null");
		}*/
		if (getPlayerView()){
			update(player.getX()-LevelExe.PWS, player.getY()-LevelExe.PWS, player.getX()+LevelExe.PWS, player.getY()+LevelExe.PWS);
		} else {
			update(0, 0, 0, 0);
		}
    }
        
	public void changeToWorld(World wld){
		changeToWorld(wld.getFilePath());
		this.combinedLines = wld.combinedLines;
		setShowEnd(wld.getShowEnd());
	}
        
	public void updateParallelBlocks(){
		for (Block[] blockRow : this.world){
			for (Block b : blockRow){
				if (b.getType().equals(World.PARALLEL_BLOCK)){
					String bType = b.parallelBlockData[b.checkInfoKey("type")];
					EngBlock eb = engW.getBlockAt(b.getX(), b.getY());
					if (bType.equals(EngBlock.LEVER) || bType.equals(EngBlock.LED)){
						b.addInfoParam("imagePath#engineering/blocks/"+bType+".png;category#air;type#"+bType);
					}
				}
			}
		}
	}

	public void update(int x, int y, int x1, int y1, boolean skip, boolean invu) {
		if ((!canUpdate && !(this instanceof EditableWorld) && !skip) || (VIEWING && !invu)){
                    return;
		}
		canUpdate = false;
		new Timer().schedule(new TimerTask(){
			@Override
			public void run(){
				canUpdate = true;
			}
		}, 50); // 50 Seconds cooldown to avoid lag
		
		try {
			if (x == 0 && y == 0 && x1 == 0 && y1 == 0) {
				this.pen.clearRect(0, 0, this.width * BLOCK_WIDTH, this.height * BLOCK_WIDTH);
				draw();
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
	public void update(int x, int y, int x1, int y1){ update(x, y, x1, y1, false, false); }
    public void update(int x, int y, int x1, int y1, boolean s){ update(x, y, x1, y1, s, false); }
	
	private static String readData(BufferedReader reader){
		try {
			String line;
			do {
				line = reader.readLine();
				if (line == null){
					return null;
				}
			} while (line.startsWith("#"));
			return line;
		} catch (IOException ex){
			return null;
		}
	}

	private Block[][] readWorld(String path, int position) {
		File file = new File(path);
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));

			for (int i = 0; i < position; i++){
				reader.readLine();
			}
			
			// Get world information
			this.information = readData(reader);

			// Get world width and height from file
			String data = readData(reader);
			this.width = Integer.parseInt(data.split("x")[0]);
			this.height = Integer.parseInt(data.split("x")[1]);

			// Get world layout
			String wData = readData(reader);
			Block[][] Fworld = parseWorldData(wData, height, width);

			String startData = readData(reader);
			String endData = readData(reader);

			start = configureFromString(startData);
			end = configureFromString(endData);
			
			String lightsData = readData(reader);
			if (lightsData.equals("1")){
				this.setAllLights(true);
			} else if (lightsData.equals("0")){
				this.setAllLights(false);
			} else {
				System.out.println("No light data available");
			}

			String engData = readData(reader);
			if (engData != null) {
				if (engData.equals("engineering_mode")) {
					Logger.info("Eng mode available");
					String engWorldData = readData(reader);
					EngBlock[][] engWorld = parseEngWorldData(engWorldData, this.height, this.width);  // array, y, x
					this.engW = new EngWorld(this, engWorld, this.width, this.height);
				} else {
					Logger.info("Engineering mode not available (missing string)");
					this.engW = null;
				}
			} else {
				Logger.info("Engineering mode not available (null)");
				this.engW = null;
			}

			reader.close();
			return Fworld;

		} catch (IOException ex) {
			Logger.error("Could not read world");
			return null;
		}
	}
	private Block[][] readWorld(String path){
		return readWorld(path, 0);
	}
	
	public String getWorldInformation(){
		return this.information;
	}
	
	public void setWorldInformation(String i){
		this.information = i;
		this.updateOnFile();
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
				if (x[it2].getType() == BAT_GEN && !x[it2].getInfo().split(";")[x[it2].checkInfoKey("data")].split("#")[1].equals("NoDataSet")) {
					String[] d = x[it2].getInfo().split(";")[x[it2].checkInfoKey("data")].split("#")[1].split(" ");
					addEnt(new Bat(this, x[it2].getX(), x[it2].getY(), Integer.parseInt(d[0]), d[1], Integer.parseInt(d[2]), d[3].equals("t") ? true : false, (d.length >= 5) ? Integer.parseInt(d[4]) : 30));
				} else if (x[it2].getType() == SHOOTER) {
					String d = Character.toString(x[it2].getInfo().split(";")[x[it2].checkInfoKey("direction")].split("#")[1].charAt(0));
					addEnt(new Arrow(this, x[it2].getX(), x[it2].getY(), d, x[it2].checkInfoKey("damage") >= 0 ? Integer.parseInt(x[it2].getInfo().split(";")[x[it2].checkInfoKey("damage")].split("#")[1]) : 30));
				} else if (x[it2].getType() == ELEVATOR && !x[it2].getInfo().split(";")[x[it2].checkInfoKey("data")].split("#")[1].equals("NoDataSet")) {
					String[] d = x[it2].getInfo().split("#")[1].split(" ");
					addEnt(new Elevator(this, x[it2].getX(), x[it2].getY(), Integer.parseInt(d[0]), d[1]));
				} else if (x[it2].getType() == C_SPIKE) {
					addEnt(new CSpike(this, x[it2].getX(), x[it2].getY()));
				} else if (x[it2].getType() == PARALLEL_BLOCK){
					if (x[it2].parallelBlockData[x[it2].checkInfoKey("type")].equals(EngBlock.DOOR)){
						addEnt(new ParallelBlock(this, x[it2].getX(), x[it2].getY(), x[it2].getInfo(), new int[][]{{0, 1, 2, 3},{3, 2, 1, 0}}, 3, 0));
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
	
	public static void drawRotatedImage(GraphicsContext pen, String img, double x, double y, int w, String d, boolean isContained, boolean exRotation, boolean complete, String attach){
		// NORTH: 0 - EAST: 90 - SOUTH: 180 - WEST: -90 (Square images only)
		// isContained: in spriteSheet ?
		// exRotation: true: give attach value and d for filename, false: no attach value needed
		// complete: use n e s w or complete form (n e s w ne es sw ...) ?
		Image imgFile = new Image(img+(exRotation ? "-"+d: "")+".png");
		if (exRotation){
			pen.drawImage(imgFile, isContained ? Block.getSpriteCoords(attach, complete, true) : 0,  isContained ? 1 : 0, isContained ? DEFAULT_BLOCK_WIDTH : imgFile.getWidth(), isContained ? DEFAULT_BLOCK_WIDTH : imgFile.getHeight(), x, y, w, w);
		} else {
			switch (d){
				case NORTH:
					if (isContained){
						pen.drawImage(imgFile, Block.getSpriteCoords(d, complete, false),  1, DEFAULT_BLOCK_WIDTH, DEFAULT_BLOCK_WIDTH, x, y, w, w);
					} else {
						pen.drawImage(imgFile, x, y, w, w);
					}
					break;
				case EAST:
					if (isContained){
						pen.drawImage(imgFile, Block.getSpriteCoords(d, complete, false),  1, DEFAULT_BLOCK_WIDTH, DEFAULT_BLOCK_WIDTH, x, y, w, w);
					} else {
						pen.translate(x+w, y);
						pen.rotate(90);
						pen.drawImage(imgFile, 0, 0, w, w);
						pen.rotate(-90);
						pen.translate(-x-w, -y);
					}
					break;
				case SOUTH:
					if (isContained){
						pen.drawImage(imgFile, Block.getSpriteCoords(d, complete, false),  1, DEFAULT_BLOCK_WIDTH, DEFAULT_BLOCK_WIDTH, x, y, w, w);
					} else {
						pen.translate(x+w, y+w);
						pen.rotate(180);
						pen.drawImage(imgFile, 0, 0, w, w);
						pen.rotate(-180);
						pen.translate(-x-w, -y-w);
					}
					break;
				case WEST:
					if (isContained){
						pen.drawImage(imgFile, Block.getSpriteCoords(d, complete, false),  1, DEFAULT_BLOCK_WIDTH, DEFAULT_BLOCK_WIDTH, x, y, w, w);
					} else {
						pen.translate(x, y+w);
						pen.rotate(-90);
						pen.drawImage(imgFile, 0, 0, w, w);
						pen.rotate(90);
						pen.translate(-x, -y-w);
					}
					break;
			}
		}
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
			for (Entity e: this.ents) {
				if (!e.layer){
					e.draw(this.pen);
				}
			}
			this.player.draw(this.pen);
			for (Entity e: this.ents) {
				if (e.layer){
					e.draw(this.pen);
				}
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
			if (((e.getX() >= x && e.getX()<= x1) && (e.getY() >= y && e.getY()<= y1)) || (e instanceof PoisonCloud)) {
				if (!e.layer){
					e.draw(this.pen, e.getX() - x, e.getY() - y);
				}
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
		for (Entity e: this.ents) {
			if (((e.getX() >= x && e.getX()<= x1) && (e.getY() >= y && e.getY()<= y1)) || (e instanceof PoisonCloud)) {
				if (e.layer){
					e.draw(this.pen, e.getX() - x, e.getY() - y);
				}
			}
		}
	}

	private void drawStart(int x, int y) {
		this.pen.setStroke(Color.GREEN);
		this.pen.setFont(new Font("Arial", 23/32 * World.BLOCK_WIDTH));
		this.pen.strokeText("S", (start[0] - x) * BLOCK_WIDTH + 2, (start[1] - y) * BLOCK_WIDTH + 22);
	}

	private void drawEnd(int x, int y) {
		if (this.showEnd){
			this.pen.drawImage(new Image("file://" + Editor.changeSlash(PATH) + ".labyrinthgame/Images/blocks/end.png"), (end[0] - x) * BLOCK_WIDTH, (end[1] - y) * BLOCK_WIDTH, BLOCK_WIDTH, BLOCK_WIDTH);
			if (this.warningOnEnd){
				this.pen.drawImage(new Image("file://" + Editor.changeSlash(PATH) + ".labyrinthgame/Images/editor/warning.png"), (end[0] - x) * BLOCK_WIDTH, (end[1] - y) * BLOCK_WIDTH, BLOCK_WIDTH, BLOCK_WIDTH);
			}
		}
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
		builder.append(String.format("Width: %s, Height: %s.\nStart at: %s, End at: %s\nFile path: %s", width, height, Arrays.toString(start), Arrays.toString(end), this.filePath));

		return builder.toString();
	}
}
