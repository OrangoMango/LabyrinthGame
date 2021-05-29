package com.orangomango.labyrinth.menu;

import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import com.orangomango.labyrinth.menu.editor.Editor;
import com.orangomango.labyrinth.menu.createdlevels.HomeWindow;

public class Menu{
    private Stage stage;
    private Stage stageExt;
    
    public static boolean MYLEVELS = false;
    public static boolean EDITOR = false;
    
    public static String OPEN = null;

    public Menu(double version){
      this.stage = new Stage();
      this.stage.setTitle("Menu v"+version);

      LoadingScreen ls = new LoadingScreen();

      GridPane layout = new GridPane();
      layout.setHgap(20);
      layout.setVgap(20);

      Button playBtn = new Button("Play");
      playBtn.setOnAction(event -> {
        this.stageExt.show();
        stop();
      });

      Button editorBtn = new Button("Editor");
      editorBtn.setOnAction(event -> {
        if (!EDITOR){
            startEditor(null);
            EDITOR = true;
        }
      });

      Button levelsBtn = new Button("My levels");
      levelsBtn.setOnAction(event -> {
        if (!MYLEVELS){
            HomeWindow hw = new HomeWindow();
            MYLEVELS = true;
        }
      });

      Label sign = new Label("Game by OrangoMango (C)2021\n https://orangomango.github.io");
      layout.add(playBtn, 0, 0);
      layout.add(editorBtn, 1, 0);
      layout.add(levelsBtn, 2, 0);
      layout.add(sign, 0, 1, 3, 1);

      this.stage.setScene(new Scene(layout, 300, 200));
    }

    private void startEditor(String param){
      Editor editor = new Editor(param);
      editor.start();
    }

    public void setStageExt(Stage st){
      this.stageExt = st;
    }

    public void start(){
      this.stage.show();
      if (OPEN != null){
      		System.out.println("Opening requested file: "+OPEN);
      		Editor editor = new Editor(OPEN);
      		editor.start();
      		EDITOR = true;
      }
    }

    public void stop(){
      this.stage.hide();
    }
}
