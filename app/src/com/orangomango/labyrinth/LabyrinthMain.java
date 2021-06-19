package com.orangomango.labyrinth;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.canvas.*;
import javafx.scene.control.Label;
import javafx.geometry.Insets;
import javafx.event.EventHandler;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.KeyCode;

import java.io.File;

import com.orangomango.labyrinth.menu.Menu;
import com.orangomango.labyrinth.menu.editor.Editor;

public class LabyrinthMain extends Application{

  public static String[] FILE_PATHS;
  public static int currentWorldIndex = 0;
  public final static double VERSION = 3.1;
  
  private static String ARG = null;
	private Stage stage;

  public static void main(String[] args) {
   	if (args.length >= 1){
   		ARG = args[0];
      System.out.println("File path requested");
    }
   	launch(args);
  }

	private static String[] getLevelsList(){
		File dir = new File(Editor.PATH+".labyrinthgame"+File.separator+"SystemLevels");
		return dir.list();
	}

  @Override
  public void start(Stage stage){
    this.stage = stage;
    //this.stage.getIcons().add(new Image("https://github.com/OrangoMango/LabyrinthGame/raw/main/app/lib/images/icon.png"));   TBD

    System.out.println(System.getProperty("user.home")); // Know user's home

    Editor.setupDirectory();

    // Start Menu
    Menu menu = new Menu(VERSION);
    Menu.OPEN = ARG;
    menu.setTSW(this);
    this.stage.setTitle("com.orangomango.labyrinth");
  }

 public void startShowing(){
    FILE_PATHS = getLevelsList();
    System.out.println(java.util.Arrays.toString(FILE_PATHS));
    // Create a simple world
    final World world = new World(Editor.PATH+".labyrinthgame"+File.separator+"SystemLevels"+File.separator+FILE_PATHS[currentWorldIndex]);
    world.setStage(this.stage);
    
    Canvas canvas = new Canvas(World.BLOCK_WIDTH*world.width, World.BLOCK_WIDTH*world.height);
    Label label = new Label(FILE_PATHS[currentWorldIndex]);
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
     
    Scene scene = new Scene(layout, World.BLOCK_WIDTH*world.width+40, World.BLOCK_WIDTH*world.height+80);
    this.stage.setScene(scene);

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
				if (player.isOnEnd()){
					try{
						Thread.sleep(500);
					} catch (InterruptedException ex){
						ex.printStackTrace();
					}
					if (currentWorldIndex < FILE_PATHS.length-1){
						currentWorldIndex++;
					}
					world.changeToWorld(Editor.PATH+".labyrinthgame"+File.separator+"SystemLevels"+File.separator+FILE_PATHS[currentWorldIndex]);
				}
			}
      });
		this.stage.setResizable(false);
    this.stage.show();


    /* 
      Move player (example) from start to end (WORLD1)

      player.moveOn(player.Y, player.NEGATIVE);
      player.moveOn(player.X, player.NEGATIVE);
      player.moveOn(player.Y, player.POSITIVE);
      player.moveOn(player.X, player.NEGATIVE);
      player.moveOn(player.Y, player.NEGATIVE);
      player.moveOn(player.X, player.POSITIVE);
      player.moveOn(player.Y, player.NEGATIVE);
      player.moveOn(player.X, player.POSITIVE);
   */
	}
}
