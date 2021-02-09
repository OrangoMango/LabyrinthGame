package com.orangomango.labyrinth;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.layout.GridPane;
import javafx.scene.canvas.*;
import javafx.scene.paint.Color;
import javafx.geometry.Insets;

public class LabyrinthMain extends Application{
   public static void main(String[] args) {
   	launch(args);
   }

  @Override
  public void start(Stage stage){
     stage.setTitle("com.orangomango.labyrinth");
     
      // Create a simple world
    World world = new World("../lib/world1.wld");
    
     Canvas canvas = new Canvas(50*world.width, 50*world.height);
     GridPane layout = new GridPane();
     layout.setPadding(new Insets(10, 10, 10, 10));
     layout.add(canvas, 0, 0);
     
     GraphicsContext pen = canvas.getGraphicsContext2D();
     world.setPen(pen);
     
    System.out.println(world);
    Block block = world.getBlockAt(3, 1); // Get block at X:3 Y:1
    //block.draw(world.pen);
    System.out.println("\n"+block+"\n");
    
    world.draw();
     
     // Make border
     pen.setStroke(Color.BLACK);
     pen.setLineWidth(4);
     pen.strokeRect(0, 0, canvas.getWidth(), canvas.getHeight());
     
     
     Scene scene = new Scene(layout, 50*world.width+20, 50*world.height+20);
     stage.setScene(scene);

    // Create a player on start position
    Player player = new Player(world.start[0], world.start[1], world);
    System.out.println(player);

    // Move player example from start to end
    player.moveOn(player.Y, player.NEGATIVE);
    player.moveOn(player.X, player.NEGATIVE);
    player.moveOn(player.Y, player.POSITIVE);
    player.moveOn(player.X, player.NEGATIVE);
    player.moveOn(player.Y, player.NEGATIVE);
    player.moveOn(player.X, player.POSITIVE);
    player.moveOn(player.Y, player.NEGATIVE);
    player.moveOn(player.X, player.POSITIVE);

    System.out.println(player); // Show current player state
    
    stage.show();
  }
}
