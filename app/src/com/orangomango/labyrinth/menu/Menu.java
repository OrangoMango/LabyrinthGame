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

    public Menu(double version){
      this.stage = new Stage();
      this.stage.setTitle("Menu v"+version);

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
        startEditor(null);
        stop();
      });

      Button levelsBtn = new Button("My levels");
      levelsBtn.setOnAction(event -> {
        HomeWindow hw = new HomeWindow();
        stop();
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
    }

    public void stop(){
      this.stage.hide();
    }
}