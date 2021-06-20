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
import java.util.Arrays;

import com.orangomango.labyrinth.menu.Menu;
import com.orangomango.labyrinth.menu.editor.Editor;

public class LabyrinthMain extends Application {

    public static String[] FILE_PATHS;
    public static int currentWorldIndex = 0;
    public final static double VERSION = 3.2;

    private static String ARG = null;
    private Stage stage;

    public static void main(String[] args) {
        if (args.length >= 1) {
            ARG = args[0];
            System.out.println("File path requested");
        }
        launch(args);
    }

    private static String[] getLevelsList() {
        File dir = new File(Editor.PATH + ".labyrinthgame" + File.separator + "SystemLevels");
        return dir.list();
    }

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        //this.stage.getIcons().add(new Image("https://github.com/OrangoMango/LabyrinthGame/raw/main/app/lib/images/icon.png"));   TBD

        System.out.println(System.getProperty("user.home")); // Know user's home

        Editor.setupDirectory();

        // Start Menu
        Menu.OPEN = ARG;
        Menu menu = new Menu(VERSION);
        menu.setTSW(this);
        this.stage.setTitle("com.orangomango.labyrinth");
    }

    public void startShowing() {
        FILE_PATHS = getLevelsList();
        System.out.println(Arrays.toString(FILE_PATHS));

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
