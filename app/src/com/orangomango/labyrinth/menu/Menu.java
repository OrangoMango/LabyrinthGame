package com.orangomango.labyrinth.menu;

import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.scene.control.Button;

import com.orangomango.labyrinth.menu.editor.Editor;

public class Menu{
    private Stage stage;
    private Stage stageExt;

    public Menu(){
      this.stage = new Stage();
      this.stage.setTitle("Menu");

      GridPane layout = new GridPane();
      Button playBtn = new Button("Play");
      playBtn.setOnAction(event -> {
        this.stageExt.show();
        stop();
      });
      Button editorBtn = new Button("Editor");
      editorBtn.setOnAction(event -> {
        startEditor();
        stop();
      });
      layout.add(playBtn, 0, 0);
      layout.add(editorBtn, 1, 0);

      this.stage.setScene(new Scene(layout, 300, 200));
    }

    private void startEditor(){
      Editor editor = new Editor();
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