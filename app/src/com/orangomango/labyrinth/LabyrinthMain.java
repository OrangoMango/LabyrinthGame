package com.orangomango.labyrinth;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.layout.GridPane;
import javafx.scene.canvas.*;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.geometry.Insets;
import javafx.event.EventHandler;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.KeyCode;

import com.orangomango.labyrinth.menu.Menu;

public class LabyrinthMain extends Application{

  public final static String FILE_PATH = "../lib/world1.wld";
  public final static String FILE_PATH_2 = "../lib/world2.wld";

  public static void main(String[] args) {
   	launch(args);
   }

  @Override
  public void start(Stage stage){

    System.out.println(System.getProperty("user.home")); // Know user's home

    // Start Menu
    Menu menu = new Menu();
    menu.start();
    menu.setStageExt(stage);
    stage.setTitle("com.orangomango.labyrinth");
     
    // Create a simple world
    final World world = new World(FILE_PATH);
    world.setStage(stage);
    
    Canvas canvas = new Canvas(World.BLOCK_WIDTH*world.width, World.BLOCK_WIDTH*world.height);
    Label label = new Label(FILE_PATH);
    world.setAttributes(label, canvas);
 
    canvas.setFocusTraversable(true);

     GridPane layout = new GridPane();
     layout.setPadding(new Insets(10, 10, 10, 10));
     layout.add(label, 0, 0);
     layout.add(canvas, 0, 1);
     
     GraphicsContext pen = canvas.getGraphicsContext2D();
     world.setPen(pen);
     
    //System.out.println(world);
    Block block = world.getBlockAt(3, 1); // Get block at X:3 Y:1

    //block.draw(world.pen);
    //System.out.println("\n"+block+"\n");
    
     // Make border
     pen.setStroke(Color.BLACK);
     pen.setLineWidth(4);
     pen.strokeRect(0, 0, canvas.getWidth(), canvas.getHeight());
     
     
     Scene scene = new Scene(layout, World.BLOCK_WIDTH*world.width+20, World.BLOCK_WIDTH*world.height+20);
     stage.setScene(scene);

    // Create a player on start position
    final Player player = new Player(world.start[0], world.start[1], world);
    player.draw(pen);
    world.setPlayer(player);
    //System.out.println(player);

    world.draw();
    canvas.setOnKeyPressed(new EventHandler<KeyEvent>(){
            @Override
            public void handle(KeyEvent event){
                  if (event.getCode() == KeyCode.UP){
                          player.moveOn(player.Y, player.NEGATIVE);
                  } else if (event.getCode() == KeyCode.DOWN){
                          player.moveOn(player.Y, player.POSITIVE);
                  } else if (event.getCode() == KeyCode.RIGHT){
                          player.moveOn(player.X, player.POSITIVE);
                  } else if (event.getCode() == KeyCode.LEFT){
                          player.moveOn(player.X, player.NEGATIVE);
                  } else {
	                        System.out.println(event.getCode());
                  }
                  System.out.println(player);
                  if (player.isOnEnd()){
                    try{
                      Thread.sleep(500);
                    } catch (InterruptedException ex){
                      ex.printStackTrace();
                    }
                    world.changeToWorld(FILE_PATH_2);
                  }
            }
      });
    //stage.setResizable(false);


    /* 
      Move player example from start to end (WORLD1)

      player.moveOn(player.Y, player.NEGATIVE);
      player.moveOn(player.X, player.NEGATIVE);
      player.moveOn(player.Y, player.POSITIVE);
      player.moveOn(player.X, player.NEGATIVE);
      player.moveOn(player.Y, player.NEGATIVE);
      player.moveOn(player.X, player.POSITIVE);
      player.moveOn(player.Y, player.NEGATIVE);
      player.moveOn(player.X, player.POSITIVE);
   */

    //System.out.println(player); // Show current player state
  }
}
