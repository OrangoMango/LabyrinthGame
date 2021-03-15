package com.orangomango.labyrinth.menu.editor;

import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.control.Label;
import javafx.scene.canvas.*;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.Slider;
import javafx.scene.control.Alert;
import javafx.scene.paint.Color;
import javafx.geometry.Insets;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import java.io.*;

public class NewWidget{
  private Stage stage;
  private static Scene SCENE_1;
  private static Scene SCENE_2;
  private static Scene SCENE_3;
  private static Scene SCENE_4;

  private Spinner spinner1, spinner2, spinner3, spinner4;
  private Label pathL;

  private int pWidth, pHeight = 2;
  private int sX, sY, eX, eY;

  private File file;
  
  public NewWidget(){
    stage = new Stage();
    
    stage.focusedProperty().addListener(new ChangeListener<Boolean>(){
      @Override
      public void changed(ObservableValue<? extends Boolean> o, Boolean v, Boolean v1){
        System.out.println(v1.booleanValue() == false);
        if (v1.booleanValue() == false){
          stage.requestFocus();
        }
      }
    });

    GridPane layout = new GridPane();
    
    HBox[] boxes = new HBox[4];
    Button[] pres = new Button[4];
    Button[] nexts = new Button[4];
    Button[] finish = new Button[4];

    // HBoxes
    for (int x = 0; x<4; x++){
      boxes[x] = new HBox();
      pres[x] = new Button("<--");
      pres[x].setOnAction(event -> switchScene(-1));
      if (x == 0) {
        pres[x].setDisable(true);
      }
      nexts[x] = new Button("-->");
      nexts[x].setOnAction(event -> switchScene(1));
      if (x == 3){
        nexts[x].setDisable(true);
      }
      finish[x] = new Button("Finish");
      finish[x].setOnAction(event -> finishWidget());
      if (x != 3){
        finish[x].setDisable(true);
      }
      boxes[x].getChildren().addAll(pres[x], nexts[x], finish[x]);
    }
    
    // Scene 1
    GridPane l1 = new GridPane();
    l1.setPadding(new Insets(10, 10, 10, 10));
    l1.setHgap(10);
    l1.setVgap(10);
    Label sel = new Label("Select file path: null");
    Button browse = new Button("Browse");
    browse.setOnAction(event -> {
      FileChooser chooser = new FileChooser();
      chooser.setTitle("Select new level file path");
      chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("World file", "*.wld"));
      try {
           this.file = chooser.showSaveDialog(this.stage);
           sel.setText("Select file path: \n"+this.file.getAbsolutePath());
           this.pathL.setText("Select file path: \n"+this.file.getAbsolutePath());
      } catch (Exception e) {
           this.file = null;
           sel.setText("Select file path: null");
           this.pathL.setText("Select file path: null");
     }
    });
    l1.add(sel, 0, 0);
    l1.add(browse, 1, 0);
    l1.add(boxes[0], 0, 1, 2, 1);
    
    // Scene 2
    GridPane l2 = new GridPane();
    l2.setPadding(new Insets(10, 10, 10, 10));
    l2.setHgap(10);
    l2.setVgap(10);
    Label lvlSize = new Label("Select level size:");
    Label sX = new Label("x:");
    Label sY = new Label("y:");
    Canvas preview = new Canvas(100, 100);
    Slider sl1 = new Slider(2, 30, 2);
    Slider sl2 = new Slider(2, 30, 2);
    sl1.setMaxWidth(90);
    sl2.setMaxWidth(90);
    sl1.setShowTickMarks(true);
    sl2.setShowTickMarks(true);
    sl1.setShowTickLabels(true);
    sl2.setShowTickLabels(true);
    sl1.setMajorTickUnit(5);
    sl2.setMajorTickUnit(5);
    sl1.setSnapToTicks(true);
    sl2.setSnapToTicks(true);
    GraphicsContext pen = preview.getGraphicsContext2D();
    pen.setFill(Color.WHITE);
    pen.fillRect(0,0,100,100);
    Label cPreview = new Label("preview (0x0)");
    sl1.setOnMouseDragged(event -> {this.pWidth = (int) sl1.getValue(); updateCanvas(pen, cPreview); });
    sl2.setOnMouseDragged(event -> {this.pHeight = (int) sl2.getValue(); updateCanvas(pen, cPreview); });
    l2.add(lvlSize, 0, 0, 2, 2);
    l2.add(sX, 0, 1);
    l2.add(sY, 0, 2);
    l2.add(sl1, 1, 1);
    l2.add(sl2, 1, 2);
    l2.add(preview, 2, 0);
    l2.add(cPreview, 2, 1);
    l2.add(boxes[1], 0, 3, 2, 1);
    
    // Scene 3
    GridPane l3 = new GridPane();
    l3.setPadding(new Insets(10, 10, 10, 10));
    l3.setHgap(10);
    l3.setVgap(10);
    CheckBox allow = new CheckBox("Set start and end pos");
    Label lab1 = new Label("Start X:");
    Label lab2 = new Label("Start Y:");
    Label lab3 = new Label("Y:");
    Label lab4 = new Label("Y:");
    this.spinner1 = new Spinner(0, 30, 0);
    this.spinner2 = new Spinner(0, 30, 0);
    this.spinner3 = new Spinner(0, 30, 0);
    this.spinner4 = new Spinner(0, 30, 0);
    // Default: Disable all buttons:
    lab1.setDisable(true);
    lab2.setDisable(true);
    lab3.setDisable(true);
    lab4.setDisable(true);
    this.spinner1.setDisable(true);
    this.spinner2.setDisable(true);
    this.spinner3.setDisable(true);
    this.spinner4.setDisable(true);
    // When checkbox is clicked able all buttons
    allow.setOnAction(event -> {
      if (!allow.isSelected()){
        lab1.setDisable(true);
        lab2.setDisable(true);
        lab3.setDisable(true);
        lab4.setDisable(true);
        this.spinner1.setDisable(true);
        this.spinner2.setDisable(true);
        this.spinner3.setDisable(true);
        this.spinner4.setDisable(true);
      } else {
        lab1.setDisable(false);
        lab2.setDisable(false);
        lab3.setDisable(false);
        lab4.setDisable(false);
        this.spinner1.setDisable(false);
        this.spinner2.setDisable(false);
        this.spinner3.setDisable(false);
        this.spinner4.setDisable(false);
      }
    });
    this.spinner1.setMaxWidth(90);
    this.spinner2.setMaxWidth(90);
    this.spinner3.setMaxWidth(90);
    this.spinner4.setMaxWidth(90);
    l3.add(allow, 0, 1, 3, 1);
    l3.add(lab1, 0, 2);
    l3.add(lab2, 0, 3);
    l3.add(lab3, 2, 2);
    l3.add(lab4, 2, 3);
    l3.add(this.spinner1, 1, 2);
    l3.add(this.spinner2, 3, 2);
    l3.add(this.spinner3, 1, 3);
    l3.add(this.spinner4, 3, 3);
    l3.add(boxes[2], 0, 4, 2, 1);
    
    // Scene 4
    GridPane l4 = new GridPane();
    l4.setPadding(new Insets(10, 10, 10, 10));
    l4.setHgap(10);
    l4.setVgap(10);
    Label success = new Label("Level created successfully");
    Label toDo = new Label("To change your settings use the \"<--\"\nbutton");
    this.pathL = new Label("Select file path: null");
    l4.add(success, 0, 0);
    l4.add(toDo, 0, 1);
    l4.add(pathL, 0, 2);
    l4.add(boxes[3], 0, 3, 2, 1);

    SCENE_1 = new Scene(l1, 300, 250);
    SCENE_2 = new Scene(l2, 300, 250);
    SCENE_3 = new Scene(l3, 300, 250);
    SCENE_4 = new Scene(l4, 300, 250);
    
    stage.setTitle("Create a new world");
    stage.setScene(SCENE_1);
    stage.show();
  }

  public void updateCanvas(GraphicsContext pen, Label preview){
    preview.setText(String.format("%sx%s", this.pWidth, this.pHeight));
    pen.setFill(Color.WHITE);
    pen.fillRect(0,0,100,100);
    pen.setStroke(Color.RED);
    pen.strokeRect(10, 10, this.pWidth*80/30, this.pHeight*80/30);    // x : 80 = width : 30
  }

  public void finishWidget(){
    try {
      String path = this.file.getAbsolutePath();
      System.out.println(path);
      System.out.println(String.format("%sx%s", this.pWidth, this.pHeight));
      System.out.println(this.pWidth*this.pHeight);
      this.sX = (int)this.spinner1.getValue();
      this.sY = (int)this.spinner2.getValue();
      this.eX = (int)this.spinner3.getValue();
      this.eY = (int)this.spinner4.getValue();
      if (sX == 0 || sY == 0 || eX == 0 || eY == 0){
        sX = 1;
        eX = 1;
        eY = 2;
      }
      if (this.pWidth == 0 || this.pHeight == 0){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText("No dimension defined for world");
        alert.setTitle("Error");
        alert.setContentText("Choose width and height for your new world!");
        alert.showAndWait();
        return;
      }
      System.out.println(String.format("%s %s     %s %s", sX, sY, eX, eY));
      if (sX > this.pWidth || sY > this.pHeight || eX > this.pWidth || eY > this.pHeight){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText("Start or end position is outside world");
        alert.setTitle("Error");
        alert.setContentText("Check start and end positon!");
        alert.showAndWait();
        return;
      }
    } catch (Exception e){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText("Insert a path for new file");
        alert.setTitle("Error");
        alert.setContentText("Please choose a path for your new world location!");
        alert.showAndWait();
        return;
    }
    try {
        BufferedWriter writer = new BufferedWriter(new FileWriter(this.file));
        writer.write(String.format("%sx%s", this.pWidth, this.pHeight));
        writer.newLine();
        for (int i = 0; i < this.pWidth*this.pHeight; i++){
           if (i != this.pWidth*this.pHeight-1){
              writer.write("0,");
           } else {
              writer.write("0");
           }
        }
	writer.newLine();
	writer.write(String.format("%s,%s\n", this.sX, this.sY));
	writer.write(String.format("%s,%s", this.eX, this.eY));
	writer.close();
    } catch (IOException ex){}
    this.stage.hide();
}

  public void switchScene(int move){
    System.out.println(move);
    if (move == 1){
      if (this.stage.getScene() == SCENE_1){
        this.stage.setScene(SCENE_2);
      } else if (this.stage.getScene() == SCENE_2){
        this.stage.setScene(SCENE_3);
      } else if (this.stage.getScene() == SCENE_3){
        this.stage.setScene(SCENE_4);
      } else if (this.stage.getScene() == SCENE_4){
        this.stage.setScene(SCENE_1);
      }
    } else if (move == -1){
      if (this.stage.getScene() == SCENE_1){
        this.stage.setScene(SCENE_4);
      } else if (this.stage.getScene() == SCENE_2){
        this.stage.setScene(SCENE_1);
      } else if (this.stage.getScene() == SCENE_3){
        this.stage.setScene(SCENE_2);
      } else if (this.stage.getScene() == SCENE_4){
        this.stage.setScene(SCENE_3);
      }
    }
  }
}
