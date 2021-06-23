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

public class LevelExe {
  private static Stage exStage = null;
  private static boolean OPEN = false;

  public LevelExe(String path, String filename, boolean saved) {
    if (OPEN){
    	return;
    }
    Stage stage = new Stage();
    stage.setTitle(filename);
    stage.setOnCloseRequest(event -> {
    	if (LevelExe.exStage != null){
          	LevelExe.exStage.show();
      }
      OPEN = false;
    });
    OPEN = true;

    final World world = new World(path);

    Canvas canvas = new Canvas(World.BLOCK_WIDTH * world.width, World.BLOCK_WIDTH * world.height);
    Label label = new Label(filename + ((saved) ? " (Level is currently synchronized)" : " (Level not synchronized, unsaved)"));
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

    world.draw();

    // Handle movement
    canvas.setOnKeyPressed(new EventHandler < KeyEvent > () {
      @Override
      public void handle(KeyEvent event) {
        int currentx, currenty = 0;
        // Save old x and y position
        currentx = player.getX();
        currenty = player.getY();
        try {
          if (event.getCode() == KeyCode.UP) {
            player.moveOn(player.Y, player.NEGATIVE);
          } else if (event.getCode() == KeyCode.DOWN) {
            player.moveOn(player.Y, player.POSITIVE);
          } else if (event.getCode() == KeyCode.RIGHT) {
            player.moveOn(player.X, player.POSITIVE);
          } else if (event.getCode() == KeyCode.LEFT) {
            player.moveOn(player.X, player.NEGATIVE);
          } else {
            System.out.println(event.getCode());
          }
        } catch (ArrayIndexOutOfBoundsException ex) {
          onVoidAlert();
          player.setX(currentx);
          player.setY(currenty);
        }
        if (player.isOnEnd()) {
          try {
            Thread.sleep(500);
          } catch (InterruptedException ex) {
            ex.printStackTrace();
          }
          Alert alert = new Alert(Alert.AlertType.INFORMATION);
          alert.setHeaderText("You completed the level!");
          alert.setTitle("Level complete");
          alert.setContentText(null);
          alert.showAndWait();
          OPEN = false;
          stage.hide();
          if (LevelExe.exStage != null)
          	LevelExe.exStage.show();
          }
    }});

    stage.setScene(scene);
    stage.setResizable(false);
    stage.show();
  }
  
  public static void setOnFinish(Stage stage){
     exStage = stage;
  }

  private void onVoidAlert() {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setHeaderText("Player went into void!");
    alert.setTitle("Player out of world");
    alert.setContentText(null);
    alert.showAndWait();
  }
}
