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

	public LevelExe(String path, String filename, boolean saved) {
		if (OPEN) {
			return;
		}
		PLAYER_MOVEMENT = true;
		Stage stage = new Stage();
		stage.setTitle(filename);
		OPEN = true;

		final World world = new World(path);
		
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
		
		world.draw(); //player.getX()-2, player.getY()-2, player.getX()+2, player.getY()+2);
		
		// Handle movement
		canvas.setOnKeyPressed(new EventHandler<KeyEvent> () {
			@Override
			public void handle(KeyEvent event) {
				if (!PLAYER_MOVEMENT){
					return;
				}
				if (event.getCode() == KeyCode.UP) {
					player.moveOn(Player.Y, Player.NEGATIVE, null); // new int[]{player.getX()-2, player.getY()-2, player.getX()+2, player.getY()+2});
				} else if (event.getCode() == KeyCode.DOWN) {
					player.moveOn(Player.Y, Player.POSITIVE, null); // new int[]{player.getX()-2, player.getY()-2, player.getX()+2, player.getY()+2});
				} else if (event.getCode() == KeyCode.RIGHT) {
					player.moveOn(Player.X, Player.POSITIVE, null); //new int[]{player.getX()-2, player.getY()-2, player.getX()+2, player.getY()+2});
				} else if (event.getCode() == KeyCode.LEFT) {
					player.moveOn(Player.X, Player.NEGATIVE, null); //new int[]{player.getX()-2, player.getY()-2, player.getX()+2, player.getY()+2});
				} else {
					System.out.println(event.getCode());
				}
				if (player.isOnEnd()) {
					Alert alert = new Alert(Alert.AlertType.INFORMATION);
					alert.setHeaderText("You completed the level!");
					alert.setTitle("Level complete");
					alert.setContentText(null);
					alert.showAndWait();
					LevelExe.OPEN = false;
					stage.hide();
					if (LevelExe.exStage != null)
						LevelExe.exStage.show();
					return;
				} else if (player.isOnBlock(World.PORTAL)){
					if (!world.getBlockAt(player.getX(), player.getY()).getInfo().equals("NoPointSet")){
						String[] coord = world.getBlockAt(player.getX(), player.getY()).getInfo().split("#");
						String[] numbers = coord[1].split(" ");
						int tx = Integer.parseInt(numbers[0]);
						int ty = Integer.parseInt(numbers[1]);
						player.setX(tx);
						player.setY(ty);
						world.update(0, 0, 0, 0);
					}
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
