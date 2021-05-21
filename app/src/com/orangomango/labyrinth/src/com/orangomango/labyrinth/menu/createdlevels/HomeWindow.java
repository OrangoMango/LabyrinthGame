package com.orangomango.labyrinth.menu.createdlevels;

import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.layout.GridPane;
import javafx.scene.control.Label;

import static com.orangomango.labyrinth.menu.Menu.MYLEVELS;

public class HomeWindow{
  public HomeWindow(){
    Stage stage = new Stage();
    stage.setTitle("My levels");
    
    GridPane layout = new GridPane();
    CreatedWorldFiles cwf = new CreatedWorldFiles();
    Label label = new Label(cwf.toString());
    layout.add(label, 0, 0);

    stage.setOnCloseRequest(event -> MYLEVELS = false);
    stage.setScene(new Scene(layout, 500, 100));
    
    stage.show();
  }  
}