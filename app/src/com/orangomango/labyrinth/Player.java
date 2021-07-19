package com.orangomango.labyrinth;

import javafx.scene.canvas.*;
import javafx.scene.paint.Color;
import javafx.scene.image.*;
import javafx.scene.control.Alert;
import javafx.animation.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import com.orangomango.labyrinth.menu.editor.Editor;
import static com.orangomango.labyrinth.menu.editor.Editor.PATH;
import com.orangomango.labyrinth.menu.editor.LevelExe;


public class Player {
	private World world;
	private int x, y;
	private int repeat = 0;

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
		draw(pen, getX(), getY());
	}
	
	public void draw(GraphicsContext pen, int x, int y) {
		pen.drawImage(new Image("file://" + Editor.changeSlash(PATH) + ".labyrinthgame/Images/entities/player.png"), x * World.BLOCK_WIDTH, y * World.BLOCK_WIDTH, World.BLOCK_WIDTH, World.BLOCK_WIDTH);
	}

	public void moveOn(String direction, int m, Stage stage, int[] rec) {
		int x = getX();
		int y = getY();
		int rep = 0;
		this.repeat = 0;
		
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
                    System.out.println("Player went into void "+x+" "+y+" "+rep);
                    setX(x);
                    setY(y);
		}
		int rep2 = rep;
		Timeline tl = new Timeline(new KeyFrame(Duration.millis(30), event -> {
			System.out.println(LevelExe.PLAYER_MOVEMENT+" - "+this.repeat+" - "+rep2);
			if (rep2 == 0){
				LevelExe.PLAYER_MOVEMENT = true;
				return;
			}
			if (this.repeat == rep2){
				LevelExe.PLAYER_MOVEMENT = true;
				System.out.println("On end");
				if (rec == null){
					this.world.update(0, 0, 0, 0);
				} else {
					this.world.update(rec[0], rec[1], rec[2], rec[3]);
				}
				return;
			} else {
				System.out.println("Ongoing");
				LevelExe.PLAYER_MOVEMENT = false;
				if (direction == X){
					setX(getX() + m);
				} else if (direction == Y){
					setY(getY() + m);
				}
			}
			this.world.update(0, 0, 0, 0);
			if (this.isOnEnd()) {
				Alert alert = new Alert(Alert.AlertType.INFORMATION);
				alert.setHeaderText("You completed the level!");
				alert.setTitle("Level complete");
				alert.setContentText(null);
				alert.showAndWait();
				LevelExe.OPEN = false;
				stage.hide();
				if (LevelExe.exStage != null)
					LevelExe.exStage.show();
			} else if (this.isOnBlock(World.SPIKE)){
				this.die();
				this.world.update(0, 0, 0, 0);
			} else if (this.isOnBlock(World.PORTAL)){
				if (!this.world.getBlockAt(this.getX(), this.getY()).getInfo().equals("NoPointSet")){
					String[] coord = world.getBlockAt(this.getX(), this.getY()).getInfo().split("#");
					String[] numbers = coord[1].split(" ");
					int tx = Integer.parseInt(numbers[0]);
					int ty = Integer.parseInt(numbers[1]);
					this.setX(tx);
					this.setY(ty);
					this.world.update(0, 0, 0, 0);
					this.repeat = rep2;
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
		return String.format("Player at X:%s and Y:%s, %s", getX(), getY(), isOnEnd() ? "On end" : (isOnStart() ? "On start" : "Not on start or end"));
	}
}
