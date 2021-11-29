package com.orangomango.labyrinth.menu.editor;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
import javafx.stage.Stage;
import javafx.scene.control.Label;
import javafx.scene.canvas.*;
import javafx.scene.input.KeyCode;
import javafx.scene.control.Alert;

import java.io.*;
import java.util.Arrays;
import java.util.Random;

import com.orangomango.labyrinth.World;
import com.orangomango.labyrinth.Block;
import com.orangomango.labyrinth.Player;
import com.orangomango.labyrinth.menu.play.entity.*;
import com.orangomango.labyrinth.engineering.*;
import static com.orangomango.labyrinth.menu.editor.Editor.PATH;
import static com.orangomango.labyrinth.menu.editor.Editor.changeSlash;

public class LevelExe {
	public static Stage exStage = null;
	public static boolean OPEN = false;
	public static boolean PLAYER_MOVEMENT;
	private int xGap = 0;
	private int yGap = 0;
	private String mode;
	public static final int PWS = 4;  // Player World Space(right)
	private boolean pressedKeys = false;
	private boolean releasedKeys = true;
	private boolean arcade;
	private World world;
	private boolean playerViewEnabled;

	public LevelExe(String path, String filename, boolean saved, String mode) {
		this.arcade = filename.endsWith(".arc");
		this.mode = mode;
                System.out.println("MODE: "+this.mode);
		if (OPEN || (this.arcade && mode.equals("engineering"))) {
			return;
		}
		PLAYER_MOVEMENT = true;
		Stage stage = new Stage();
		stage.setTitle(filename);
		OPEN = true;
		Random rnd = new Random();
				
		if (this.arcade){
			world = World.combineWorlds((new World(path)).worldList.getWorldAt(rnd.nextInt(World.getArcadeLevels(path))), (new World(path)).worldList.getWorldAt(rnd.nextInt(World.getArcadeLevels(path))));
		} else {
			world = new World(path);
		}
                if (this.mode.equals("engineering")){
                    this.playerViewEnabled = false;
                } else {
                    this.playerViewEnabled = world.width > NewWidget.MAX_PLAYER_VIEW_SIZE || world.height > NewWidget.MAX_PLAYER_VIEW_SIZE || this.arcade;
                }
                world.setPlayerView(this.playerViewEnabled);
		world.setDrawingMode(this.mode);
		
		stage.setOnCloseRequest(event -> {
			if (LevelExe.exStage != null) {
				LevelExe.exStage.show();
			}
			for (Entity e : world.getEnts()){
				if (this.mode.equals("normal")){
					e.stop();
				} else if (this.mode.equals("engineering")){
					if (e.engineering){
						e.stop();
					}
				}
			}
			if (world.getEngineeringWorld() != null){
				world.getEngineeringWorld().stopAnimations();
			}
                        if (world.viewTime != null){
                            world.viewTime.stop();
                        }
			OPEN = false;
		});

		Canvas canvas = new Canvas((!this.playerViewEnabled ? world.width : PWS*2+1) * World.BLOCK_WIDTH, (!this.playerViewEnabled ? world.height : PWS*2+1) * World.BLOCK_WIDTH);
		Label label = new Label(filename + ((saved) ? " \n(Level is currently synchronized)" : "\n(Level not synchronized, unsaved)"));

		canvas.setFocusTraversable(true);
		
		Canvas levelStatsCanvas = new Canvas(LevelStats.WIDTH, LevelStats.HEIGHT);

		GridPane layout = new GridPane();
		layout.setVgap(5);
		layout.setPadding(new Insets(10, 10, 10, 10));
		layout.add(label, 0, 0);
		layout.add(canvas, 0, 1);
		layout.add(levelStatsCanvas, 0, 2);

		GraphicsContext pen = canvas.getGraphicsContext2D();
		world.setPen(pen);

		Scene scene;
		System.out.println(this.playerViewEnabled);
		if (!this.playerViewEnabled){
			scene = new Scene(layout, LevelStats.WIDTH < World.BLOCK_WIDTH * world.width ? World.BLOCK_WIDTH * world.width + 20 : LevelStats.WIDTH + 20, World.BLOCK_WIDTH * world.height + 60 + LevelStats.HEIGHT + 10);
		} else {
			scene = new Scene(layout, PWS*2*World.BLOCK_WIDTH+20+World.BLOCK_WIDTH, PWS*2*World.BLOCK_WIDTH+60+World.BLOCK_WIDTH+LevelStats.HEIGHT+10);
		}
		scene.getStylesheets().add("file://" + changeSlash(PATH) + ".labyrinthgame/Editor/style.css");
		stage.setScene(scene);

		final Player player = new Player(world.start[0], world.start[1], world);
		player.draw(pen);
		player.setPsFilePath(path);
		world.setPlayer(player);
		
		LevelStats levelStats = new LevelStats(world, levelStatsCanvas.getGraphicsContext2D());
		world.setLevelStats(levelStats);
				
		for (Entity e : world.getEnts()){
			if (this.mode.equals("normal")){
				e.start();
			} else if (this.mode.equals("engineering")){
				if (e.engineering){
					e.start();
				}
			}
		}
		
		if (world.getPlayerView()){
			world.draw(world.start[0]-PWS, world.start[1]-PWS, world.start[0]+PWS, world.start[1]+PWS);
		} else {
			world.draw();
		}
		
		//System.out.println(World.combineWorlds(world, new World("/home/paul/.labyrinthgame/SystemLevels/level1.wld.sys")));
		
		// Handle movement
		canvas.setOnKeyPressed(event -> {
			if (this.mode.equals("normal")){
				if (!PLAYER_MOVEMENT){
					return;
				}
				if (!(player.psx == null && player.psy == null)){
					if (event.getCode() == KeyCode.UP) {
						yGap = -1;
					} else if (event.getCode() == KeyCode.DOWN) {
						yGap = 1;
					} else if (event.getCode() == KeyCode.RIGHT) {
						xGap = 1;
					} else if (event.getCode() == KeyCode.LEFT) {
						xGap = -1;
					}
					if (world.getBlockAt((int)Math.round(player.psx+xGap), (int)Math.round(player.psy+yGap)) != null){
						if (world.getBlockAt((int)Math.round(player.psx+xGap), (int)Math.round(player.psy+yGap)).getCategory() == World.WALL){
							xGap = 0;
							yGap = 0;
						}
					} else {
						xGap = 0;
						yGap = 0;
					}
					player.setX((int)Math.round(player.psx+xGap));
					player.setY((int)Math.round(player.psy+yGap));
					xGap = 0;
					yGap = 0;
				}
				
				if (event.getCode() == KeyCode.U){
					if (world.getPlayerView()){
						world.update(world.getPlayer().getX()-PWS, world.getPlayer().getY()-PWS, world.getPlayer().getX()+PWS, world.getPlayer().getY()+PWS, true);
					} else {
						world.update(0, 0, 0, 0, true);
					}
					System.out.println("Force updated!");
				}
				
				if (releasedKeys){
					if (event.getCode() == KeyCode.UP) {
						player.moveOn(Player.Y, Player.NEGATIVE, stage, new int[]{world.getPlayerView() ? 1 : -1, PWS});
					} else if (event.getCode() == KeyCode.DOWN) {
						player.moveOn(Player.Y, Player.POSITIVE, stage, new int[]{world.getPlayerView() ? 1 : -1, PWS});
					} else if (event.getCode() == KeyCode.RIGHT) {
						player.moveOn(Player.X, Player.POSITIVE, stage, new int[]{world.getPlayerView() ? 1 : -1, PWS});
					} else if (event.getCode() == KeyCode.LEFT) {
						player.moveOn(Player.X, Player.NEGATIVE, stage, new int[]{world.getPlayerView() ? 1 : -1, PWS});
					} else if (event.getCode() == KeyCode.SPACE && player.isOnBlock(World.PARALLEL_BLOCK)){
						Block block = world.getBlockAt(player.getX(), player.getY());
						EngBlock engB = world.getEngineeringWorld().getBlockAt(block.getX(), block.getY());
						String engBlockType = block.parallelBlockData[block.checkInfoKey("type")];
						if (engBlockType.equals(EngBlock.LEVER)){
							engB.toggleActive();
							world.updateParallelBlocks();
						}
						if (world.getPlayerView()){
							world.update(world.getPlayer().getX()-PWS, world.getPlayer().getY()-PWS, world.getPlayer().getX()+PWS, world.getPlayer().getY()+PWS);
						} else {
							world.update(0, 0, 0, 0);
						}
					} else {
						System.out.println(event.getCode());
					}
					pressedKeys = true;
					releasedKeys = false;
				}
			}
		});
		
		canvas.setOnKeyReleased(event -> {
			if (pressedKeys){
				pressedKeys = false;
				releasedKeys = true;
			}
		});
		
		canvas.setOnMousePressed(event -> {
			if (this.mode.equals("engineering")){
				EngBlock engblock = world.getEngineeringWorld().getBlockAtCoord((int)event.getX(), (int)event.getY());
				if (engblock.getCategory().equals(EngBlock.SIGNAL_INPUT)){
					engblock.toggleActive();
				}
				world.update(0, 0, 0, 0);
			}
		});

		levelStats.draw();
		
		if (world.getBlockAt(player.getX(), player.getY()).isWater() && this.mode.equals("normal")){
			player.oxygenSwitch = true;
			player.removeOxCont(10, 1000);
		}

		if (world.getEngineeringWorld() != null && this.mode.equals("engineering")){
			world.getEngineeringWorld().startAnimations();
		}
                if (this.mode.equals("normal") && !this.arcade){
                    world.viewFrom(world.end[0], world.end[1], world.start[0], world.start[1]);
                }
		stage.setScene(scene);
		stage.setResizable(false);
		stage.show();
	}

	public static void setOnFinish(Stage stage) {
		exStage = stage;
	}
}
