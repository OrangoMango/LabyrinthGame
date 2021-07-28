package com.orangomango.labyrinth.menu.editor;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
import javafx.stage.Stage;
import javafx.scene.control.Label;
import javafx.scene.canvas.*;
import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.control.Alert;

import com.orangomango.labyrinth.World;
import com.orangomango.labyrinth.Player;
import com.orangomango.labyrinth.menu.play.entity.*;

public class LevelExe {
	public static Stage exStage = null;
	public static boolean OPEN = false;
	public static boolean PLAYER_MOVEMENT;
	private int xGap = 0;
	private int yGap = 0;
	public static final int PWS = 2;  // Player World Space(right)

	public LevelExe(String path, String filename, boolean saved) {
		if (OPEN) {
			return;
		}
		PLAYER_MOVEMENT = true;
		Stage stage = new Stage();
		stage.setTitle(filename);
		OPEN = true;

		final World world = new World(path);
		world.setPlayerView(true);
		
		stage.setOnCloseRequest(event -> {
			if (LevelExe.exStage != null) {
				LevelExe.exStage.show();
			}
			for (Entity e : world.getEnts()){
				e.stop();
			}
			OPEN = false;
		});

		Canvas canvas = new Canvas(World.BLOCK_WIDTH * world.width, World.BLOCK_WIDTH * world.height);
		Label label = new Label(filename + ((saved) ? " (Level is currently synchronized)" : " (Level not synchronized, unsaved)"));
		label.setWrapText(true);
		world.setCanvas(canvas);

		canvas.setFocusTraversable(true);

		GridPane layout = new GridPane();
		layout.setPadding(new Insets(10, 10, 10, 10));
		layout.add(label, 0, 0);
		layout.add(canvas, 0, 1);

		GraphicsContext pen = canvas.getGraphicsContext2D();
		world.setPen(pen);

		Scene scene = new Scene(layout, World.BLOCK_WIDTH * world.width + 20, World.BLOCK_WIDTH * world.height + 40);
		stage.setScene(scene);

		final Player player = new Player(world.start[0], world.start[1], world);
		player.draw(pen);
		world.setPlayer(player);
		
		for (Entity e : world.getEnts()){
			e.start();
		}
		
		if (world.getPlayerView()){
			world.draw(world.start[0]-PWS, world.start[1]-PWS, world.start[0]+PWS, world.start[1]+PWS);
		} else {
			world.draw();
		}
		
		// Handle movement
		canvas.setOnKeyPressed(new EventHandler<KeyEvent> () {
			@Override
			public void handle(KeyEvent event) {
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
					if (world.getBlockAt((int)Math.round(player.psx+xGap), (int)Math.round(player.psy+yGap)).getCategory() == World.WALL){
						System.out.println("Next block is a wall");
						xGap = 0;
						yGap = 0;
					}
					player.setX((int)Math.round(player.psx+xGap));
					player.setY((int)Math.round(player.psy+yGap));
				}
				if (event.getCode() == KeyCode.UP) {
					player.moveOn(Player.Y, Player.NEGATIVE, stage, new int[]{world.getPlayerView() ? 1 : -1, PWS});
				} else if (event.getCode() == KeyCode.DOWN) {
					player.moveOn(Player.Y, Player.POSITIVE, stage,   new int[]{world.getPlayerView() ? 1 : -1, PWS});
				} else if (event.getCode() == KeyCode.RIGHT) {
					player.moveOn(Player.X, Player.POSITIVE, stage,  new int[]{world.getPlayerView() ? 1 : -1, PWS});
				} else if (event.getCode() == KeyCode.LEFT) {
					player.moveOn(Player.X, Player.NEGATIVE, stage,  new int[]{world.getPlayerView() ? 1 : -1, PWS});
				} else {
					System.out.println(event.getCode());
				}
			}
		});

		stage.setScene(scene);
		stage.setResizable(false);
		stage.show();
	}

	public static void setOnFinish(Stage stage) {
		exStage = stage;
	}
}
