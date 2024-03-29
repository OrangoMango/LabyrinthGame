package com.orangomango.labyrinth;

import javafx.scene.canvas.*;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.scene.paint.Color;
import javafx.scene.image.*;
import javafx.scene.control.Alert;
import javafx.animation.*;
import javafx.util.Duration;

import java.util.Timer;
import java.util.TimerTask;
import java.util.Arrays;
import java.util.Random;

import com.orangomango.labyrinth.menu.editor.Editor;
import static com.orangomango.labyrinth.menu.editor.Editor.PATH;
import com.orangomango.labyrinth.menu.editor.LevelExe;
import com.orangomango.labyrinth.menu.play.entity.*;
import com.orangomango.labyrinth.engineering.EngBlock;
import static com.orangomango.labyrinth.menu.editor.LevelStats.GAP_FACTOR;
import static com.orangomango.labyrinth.menu.editor.LevelStats.ICON_SIZE;


public class Player {
	private World world;
	private int x, y;
	private int health = 100;
	private int oxygen = 100;
	private int repeat = 0;
	public Double psx = null;
	public Double psy = null;
	private boolean healthRemovingStarted = false;
	private boolean oxygenRemovingStarted = false;
	public boolean oxygenSwitch = false;
	private Timeline oxygenT = null;
	private String direction = World.EAST;
	private Timeline tl;
	private String pseudoPath;

	public static final String X = "x";
	public static final String Y = "y";
	public static final int POSITIVE = 1;
	public static final int NEGATIVE = -1;

	public Player(int x, int y, World w) {
		this.x = x;
		this.y = y;
		world = w;
	}
	
	public void die(Boolean pw, int dist){
		if (getPsFilePath() != null && World.getArcadeLevels(getPsFilePath()) > 0){
			LevelExe.OPEN = false;
			this.world.getPsStage().close();
			if (LevelExe.exStage != null)
				LevelExe.exStage.show();
			LevelExe.PLAYER_MOVEMENT = true;
			for (Entity e : world.getEnts()){
				e.stop();
			}
			Alert alert = new Alert(Alert.AlertType.INFORMATION);
			alert.setHeaderText("You died!");
			alert.setTitle("You died");
			alert.setContentText(null);
			alert.show();
			return;
		}
		this.health = 100;
		this.oxygen = 100;
		setX(this.world.start[0]);
		setY(this.world.start[1]);
		if (pw != null && dist >= 0){
			if (pw){
				world.update(getX()-dist, getY()-dist, getX()+dist, getY()+dist, true);
			} else {
				world.update(0, 0, 0, 0, true);
			}
		}
		if (oxygenT != null){
			oxygenT.stop();
		}
		if (this.tl != null){
			this.tl.stop();
		}
		LevelExe.PLAYER_MOVEMENT = true;
	}
	
	public void removeHealth(int v){
		if (!healthRemovingStarted){
			this.health -= v;
			this.healthRemovingStarted = true;
			if (this.health <= 0){
				this.die(null, -1);
			}
			new Timer().schedule(new TimerTask(){
				@Override
				public void run(){
					healthRemovingStarted = false;
					cancel();
				}
			}, 400);
		}
	}
	
	public int getHealth(){
		return this.health;
	}
	
	public int getOx(){
		return this.oxygen;
	}
	
	public void removeOx(int v){
		this.oxygen -= v;
		if (this.oxygen <= 0 || this.oxygen >= 100){
			if (this.oxygen <= 0){
				this.oxygen = 0;
				removeHealth(25);
			} else if (this.oxygen > 100){
				this.oxygen = 100;
			}
			if (getHealth() <= 0 || this.oxygen >= 100){
				oxygenT.stop();
				oxygenRemovingStarted = false;
			}
		}
		if (this.world.getPlayerView()){
			this.world.update(getX()-LevelExe.PWS, getY()-LevelExe.PWS, getX()+LevelExe.PWS, getY()+LevelExe.PWS);
		} else {
			this.world.update(0, 0, 0, 0);
		}
	}
	
	public void removeOxCont(int v, int time){
		if (!oxygenRemovingStarted){
			oxygenRemovingStarted = true;
			oxygenT = new Timeline(new KeyFrame(Duration.millis(time), evt -> {
				removeOx(v);
			}));
			oxygenT.setCycleCount(Animation.INDEFINITE);
			oxygenT.play();
		}
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
		if (this.psx == null && this.psy == null){
			draw(pen, getX(), getY());
		} else {
			draw(pen, psx, psy);
		}
	}
	
	public void draw(GraphicsContext pen, double x, double y) {
		World.drawRotatedImage(pen, "file://" + Editor.changeSlash(PATH) + ".labyrinthgame/Images/entities/player", x * World.BLOCK_WIDTH, y * World.BLOCK_WIDTH, World.BLOCK_WIDTH, this.direction, true, false, false, null);
		if (this.world.getLevelStats() != null){
			this.world.getLevelStats().draw(x*World.BLOCK_WIDTH-ICON_SIZE/2*(2+GAP_FACTOR)/2, y*World.BLOCK_WIDTH+ICON_SIZE*(2+GAP_FACTOR));
		}
	}

	public void moveOn(String direction, int m, Stage stage, int[] rec) {
		int x = getX();
		int y = getY();
		int rep = 0;
		this.repeat = 0;
		this.psx = null;
		this.psy = null;
		
		if (direction == X){
			if (m == POSITIVE){
				this.direction = World.EAST;
			} else if (m == NEGATIVE){
				this.direction = World.WEST;
			}
		} else if (direction == Y){
			if (m == POSITIVE){
				this.direction = World.SOUTH;
			} else if (m == NEGATIVE){
				this.direction = World.NORTH;
			}
		}
		
		try {
			if (direction == X) {
				Block[] xrow = this.world.getXRow(getY());
				while (this.world.getBlockAt(getX() + m, getY()).getCategory() != this.world.WALL) {
					setX(getX() + m);
					rep++;
				}
				setX(x);

			} else if (direction == Y) {
				Block[] yrow = this.world.getYRow(getX());
				while (this.world.getBlockAt(getX(), getY() + m).getCategory() != this.world.WALL) {
					setY(getY() + m);
					rep++;
				}
				setY(y);
			} else {
				Logger.error("Unknow direction found");
				return;
			}
		} catch (Exception ex) {
                    // Player went into void so it must stay on edge
                    //world.update(0, 0, 0, 0); //player.getX()-2, player.getY()-2, player.getX()+2, player.getY()+2);
                    setX(x);
                    setY(y);
		}
		int rep2 = rep;
		this.tl = new Timeline(new KeyFrame(Duration.millis(40), event -> {
			if (rep2 == 0){
				LevelExe.PLAYER_MOVEMENT = true;
				return;
			}
			if (this.repeat == rep2){
				LevelExe.PLAYER_MOVEMENT = true;
				if (this.isOnEnd() && this.world.getShowEnd()) {
					LevelExe.OPEN = false;
					stage.close();
					if (LevelExe.exStage != null)
						LevelExe.exStage.show();
					Alert alert = new Alert(Alert.AlertType.INFORMATION);
					alert.setHeaderText("You completed the level!");
					alert.setTitle("Level complete");
					alert.setContentText(null);
					alert.show();
					return;
				} else if (this.isOnBlock(World.PORTAL)){
					if (!world.getBlockAt(this.getX(), this.getY()).getInfo().split(";")[world.getBlockAt(this.getX(), this.getY()).checkInfoKey("point")].split("#")[1].equals("NoPointSet")){
						String[] coord = world.getBlockAt(this.getX(), this.getY()).getInfo().split("#");
						String[] numbers = coord[1].split(" ");
						int tx = Integer.parseInt(numbers[0]);
						int ty = Integer.parseInt(numbers[1]);
						this.setX(tx);
						this.setY(ty);
					}
				} else if (this.isOnBlock(World.OXYGEN_POINT)){
					if (oxygenT != null){
						oxygenRemovingStarted = false;
						oxygenT.stop();
						removeOxCont(-10, 500);
					}
				}
				if (rec[0] == 1){
					this.world.update(getX()-rec[1], getY()-rec[1], getX()+rec[1], getY()+rec[1], true);
				} else {
					this.world.update(0, 0, 0, 0, true);
				}
				tl.stop();
				return;
			} else {
				LevelExe.PLAYER_MOVEMENT = false;
				for (Entity ent : this.world.getEnts()){
					if (ent.isOnPlayer(this) && (ent instanceof Bat || ent instanceof Arrow)){
						if (rec[0] == 1){
							this.world.update(getX()-rec[1], getY()-rec[1], getX()+rec[1], getY()+rec[1]);
						} else if (rec[0] == 0){
							this.world.update(0, 0, 0, 0);
						}
						removeHealth(10);
					} else if (ent.isOnPlayer(this) && ent instanceof Elevator){
						this.repeat = rep2;
						return;
					} else if (ent.isOnPlayer(this) && ent instanceof CSpike){
						if (((CSpike)ent).isOpened()){
							this.die(null, -1);
							this.repeat = rep2;
							return;
						}
					} else if (ent.isOnPlayer(this, ent.getX()+(direction == X ? m*-1 : 0), ent.getY()+(direction == Y ? m*-1 : 0)) && ent instanceof ParallelBlock){
						if (!this.world.getEngineeringWorld().getBlockAt(((int)((ParallelBlock)ent).getX()), ((int)((ParallelBlock)ent).getY())).isActive() && this.world.getEngineeringWorld().getBlockAt(((int)((ParallelBlock)ent).getX()), ((int)((ParallelBlock)ent).getY())).getType().equals(EngBlock.DOOR)){
							this.repeat = rep2;
							return;
						}
					}
				}
				if (direction == X){
					setX(getX() + m);
				} else if (direction == Y){
					setY(getY() + m);
				}
				if (rec[0] == 1){
					this.world.update(getX()-rec[1], getY()-rec[1], getX()+rec[1], getY()+rec[1]);
				} else {
					this.world.update(0, 0, 0, 0);
				}
			}
			if (this.world.getBlockAt(getX(), getY()).isWater()){
				if (oxygenT != null && !oxygenSwitch){
					oxygenRemovingStarted = false;
					oxygenT.stop();
				}
				oxygenSwitch = true;
				removeOxCont(10, 1000);
			} else {
				if (oxygenT != null && oxygenSwitch){
					oxygenRemovingStarted = false;
					oxygenT.stop();
				}
				oxygenSwitch = false;
				removeOxCont(-10, 500);
			}
			if (this.isOnBlock(World.SPIKE)){
				this.die(rec[0] == 1, rec[1]);
				this.repeat = rep2;
				return;
			}
			if (getPsFilePath() != null && World.getArcadeLevels(getPsFilePath()) > 0){
				Random rnd = new Random();
				
				boolean foundEndLine = false;
				int cc = 0;
				for (int n : world.combinedLines){
					if (n == getY()){
						foundEndLine = true;
						world.combinedLines[cc] = -1;
						break;
					}
					cc++;
				}
				if (foundEndLine){
					int ph = 2;
					for (Entity e : this.world.getEnts()){
						if (e instanceof PoisonCloud){
							ph = ((PoisonCloud)e).getHeight();
						}
					}
					for (Entity e : this.world.getEnts()){
						e.stop();
					}
					this.world.changeToWorld(World.combineWorlds(this.world, (new World(getPsFilePath())).worldList.getWorldAt(rnd.nextInt(World.getArcadeLevels(getPsFilePath())))));
					this.world.addEnt(new PoisonCloud(this.world, this.world.width, ph, -2));
					for (Entity e : this.world.getEnts()){
						e.start();
					}
				}
			}
			
			this.repeat++;
		}));
		tl.setCycleCount(rep+1);
		tl.play();
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
	
	public void setPsFilePath(String pt){
		this.pseudoPath = pt;
	}
	
	public String getPsFilePath(){
		return this.pseudoPath;
	}
	
	public boolean isOnBlock(String block){
		if (this.world.getBlockAt(getX(), getY()).getType() == block){
			return true;
		}
		return false;
	}

	@Override
	public String toString() {
		return String.format("Player at X:%s and Y:%s (psx, psy: %s %s), %s. Health: %s", getX(), getY(), this.psx, this.psy, isOnEnd() ? "On end" : (isOnStart() ? "On start" : "Not on start or end"), getHealth());
	}
}
