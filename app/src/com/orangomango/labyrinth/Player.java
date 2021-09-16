package com.orangomango.labyrinth;

import javafx.scene.canvas.*;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.scene.paint.Color;
import javafx.scene.image.*;
import javafx.scene.control.Alert;
import javafx.animation.*;
import javafx.util.Duration;

import com.orangomango.labyrinth.menu.editor.Editor;
import static com.orangomango.labyrinth.menu.editor.Editor.PATH;
import com.orangomango.labyrinth.menu.editor.LevelExe;
import com.orangomango.labyrinth.menu.play.entity.*;


public class Player {
	private World world;
	private int x, y;
	private int repeat = 0;
	public Double psx = null;
	public Double psy = null;

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
		if (this.psx == null && this.psy == null){
			draw(pen, getX(), getY());
		} else {
			draw(pen, psx, psy);
		}
	}
	
	public void draw(GraphicsContext pen, double x, double y) {
		pen.drawImage(new Image("file://" + Editor.changeSlash(PATH) + ".labyrinthgame/Images/entities/player.png"), x * World.BLOCK_WIDTH, y * World.BLOCK_WIDTH, World.BLOCK_WIDTH, World.BLOCK_WIDTH);
	}

	public void moveOn(String direction, int m, Stage stage, int[] rec) {
		int x = getX();
		int y = getY();
		int rep = 0;
		this.repeat = 0;
		this.psx = null;
		this.psy = null;
		
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
		Timeline tl = new Timeline(new KeyFrame(Duration.millis(30), event -> {
			if (rep2 == 0){
				LevelExe.PLAYER_MOVEMENT = true;
				return;
			}
			if (this.repeat == rep2){
				LevelExe.PLAYER_MOVEMENT = true;
				if (this.isOnEnd()) {
					Alert alert = new Alert(Alert.AlertType.INFORMATION);
					alert.setHeaderText("You completed the level!");
					alert.setTitle("Level complete");
					alert.setContentText(null);
					alert.show();
					LevelExe.OPEN = false;
					stage.hide();
					if (LevelExe.exStage != null)
						LevelExe.exStage.show();
					return;
				} else if (this.isOnBlock(World.PORTAL)){
					if (!world.getBlockAt(this.getX(), this.getY()).getInfo().equals("NoPointSet")){
						String[] coord = world.getBlockAt(this.getX(), this.getY()).getInfo().split("#");
						String[] numbers = coord[1].split(" ");
						int tx = Integer.parseInt(numbers[0]);
						int ty = Integer.parseInt(numbers[1]);
						this.setX(tx);
						this.setY(ty);
					}
				}
				if (rec[0] == 1){
					this.world.update(getX()-rec[1], getY()-rec[1], getX()+rec[1], getY()+rec[1]);
				} else {
					this.world.update(0, 0, 0, 0);
				}
				return;
			} else {
				LevelExe.PLAYER_MOVEMENT = false;
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
			if (this.isOnBlock(World.SPIKE)){
				this.die();
				if (rec[0] == 1){
					this.world.update(getX()-rec[1], getY()-rec[1], getX()+rec[1], getY()+rec[1]);
				} else {
					this.world.update(0, 0, 0, 0);
				}
				this.repeat = rep2;
				return;
			}
			for (Entity ent : this.world.getEnts()){
				if (ent.isOnPlayer(this) && (ent instanceof Bat || ent instanceof Arrow)){
					if (rec[0] == 1){
						this.world.update(getX()-rec[1], getY()-rec[1], getX()+rec[1], getY()+rec[1]);
					}
					this.repeat = rep2;
					return;
				} else if (ent.isOnPlayer(this) && ent instanceof Elevator){
					this.repeat = rep2;
					return;
				} else if (ent.isOnPlayer(this) && ent instanceof CSpike){
					if (((CSpike)ent).isOpened()){
						this.die();
						this.repeat = rep2;
						return;
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
	
	public boolean isOnBlock(String block){
		if (this.world.getBlockAt(getX(), getY()).getType() == block){
			return true;
		}
		return false;
	}

	@Override
	public String toString() {
		return String.format("Player at X:%s and Y:%s (psx, psy: %s %s), %s", getX(), getY(), this.psx, this.psy, isOnEnd() ? "On end" : (isOnStart() ? "On start" : "Not on start or end"));
	}
}
