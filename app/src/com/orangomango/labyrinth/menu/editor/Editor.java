package com.orangomango.labyrinth.menu.editor;

import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.BorderPane;
import javafx.geometry.Orientation;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.canvas.*;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.control.ScrollPane;
import javafx.stage.FileChooser;

import java.io.*;

import com.orangomango.labyrinth.Player;
import com.orangomango.labyrinth.Block;

public class Editor{
  private Stage stage;
  private final static String PATH = "/home/runner/";

  public Editor(){
    setupDirectory();
    this.stage = new Stage();
    this.stage.setTitle("LabyrinthGame - Editor");

    GridPane layout = new GridPane();

    // Setup the toolbar
    ToolBar toolbar = new ToolBar();
    toolbar.setOrientation(Orientation.HORIZONTAL);

    createNewWorld("test");
    final EditableWorld edworld = new EditableWorld(PATH+".labyrinthgame/Editor/Levels/test.wld");

    Button newBtn = new Button("New");
    Button saveBtn = new Button("Save");
    saveBtn.setOnAction(event -> {
      FileChooser chooser = new FileChooser();
      chooser.setInitialDirectory(new File(PATH));
      chooser.setTitle("Save world");
      chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("World file", "*.wld"));
      chooser.showSaveDialog(this.stage);
    });
    Button openBtn = new Button("Open");
    openBtn.setOnAction(event -> {
      FileChooser chooser = new FileChooser();
      chooser.setInitialDirectory(new File(PATH));
      chooser.setTitle("Open world");
      chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("World file", "*.wld"));
      File f = chooser.showOpenDialog(this.stage);
      edworld.changeToWorld(f.getAbsolutePath());
    });

    Button addCBtn = new Button("AC");
    Button addRBtn = new Button("AR");
    Button rmCBtn = new Button("RC");
    Button rmRBtn = new Button("RR");

    Button runBtn = new Button("Run");
    Button stopBtn = new Button("Stop");

    Button delBtn = new Button("Del");

    toolbar.getItems().addAll(newBtn, saveBtn, openBtn, new Separator(), addCBtn, addRBtn, rmCBtn, rmRBtn, new Separator(), runBtn, stopBtn, new Separator(), delBtn);

    //Setup world editor
    ScrollPane scrollpane = new ScrollPane();
    scrollpane.setMaxWidth(515);
    scrollpane.setMaxHeight(400);

    

    BorderPane pane = new BorderPane();
    Canvas canvas = new Canvas(edworld.width*EditableWorld.BLOCK_WIDTH, edworld.height*EditableWorld.BLOCK_WIDTH);
    canvas.setFocusTraversable(true);
    pane.setCenter(canvas);
    scrollpane.setContent(pane);

    GraphicsContext pen = canvas.getGraphicsContext2D();
    edworld.setPen(pen);
    edworld.setPlayer(new Player(edworld.start[0], edworld.start[1], edworld));
    edworld.setCanvas(canvas);
    edworld.draw();

    canvas.setOnMousePressed(new EventHandler<MouseEvent>(){
      @Override
      public void handle(MouseEvent event){
        EditableBlock edblock = EditableBlock.fromBlock(edworld.getBlockAtCoord((int)event.getX(), (int)event.getY()));
        // edblock.toggleType();
        // edworld.update();
        System.out.println(edblock);
      }
    });

    layout.add(toolbar, 0, 0);
    layout.add(scrollpane, 0, 1);
    this.stage.setScene(new Scene(layout, 515, 420));
    //this.stage.setResizable(false);
  }

  public void start(){
    this.stage.show();
  }

  private void createNewWorld(String name){
    File f = new File(PATH+".labyrinthgame/Editor/Levels/"+name+".wld");
    try {
      f.createNewFile();
      BufferedWriter writer = new BufferedWriter(new FileWriter(f));
      writer.write("2x2\n");
      writer.write("1,0,0,0\n");
      writer.write("1,0\n");
      writer.write("1,1");
      writer.close();
    } catch (IOException e){
      e.printStackTrace();
    }
  }

  private void checkAndCreateDir(String path){
    File f = new File(path);
    if (!f.exists()){
      f.mkdir();
    }
  }

  private void setupDirectory(){
    checkAndCreateDir(PATH+".labyrinthgame");
    checkAndCreateDir(PATH+".labyrinthgame/Editor");
    checkAndCreateDir(PATH+".labyrinthgame/Editor/Levels");

  }
}