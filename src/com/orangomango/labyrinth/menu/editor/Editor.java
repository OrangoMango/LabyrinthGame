package com.orangomango.labyrinth.menu.editor;

import javafx.application.Platform;
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
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.control.MenuBar;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.stage.FileChooser;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.Random;

import com.orangomango.labyrinth.Player;
import com.orangomango.labyrinth.Block;
import com.orangomango.labyrinth.menu.createdlevels.CreatedWorldFiles;

public class Editor{
  private Stage stage;
  private final EditableWorld edworld;
  public final static String PATH = System.getProperty("user.home")+File.separator;
  private static String WORKING_FILE_PATH = "";
  private static String CURRENT_FILE_PATH = "";
  private boolean saved = true;
  public static CreatedWorldFiles worldList;

  public Editor(String editorFilePath){
    worldList = new CreatedWorldFiles();
    this.stage = new Stage();
    this.stage.setTitle("LabyrinthGame - Editor ("+getFileName()+((saved) ? "" : "*")+")");
    this.stage.setOnCloseRequest(event -> Platform.exit());

    GridPane layout = new GridPane();

    // Setup the toolbar
    ToolBar toolbar = new ToolBar();
    toolbar.setOrientation(Orientation.HORIZONTAL);

    createNewWorld("testSystemWorld-DefaultName_NoCopy");
    edworld = new EditableWorld(PATH+".labyrinthgame"+File.separator+"Editor"+File.separator+"Levels"+File.separator+"testSystemWorld-DefaultName_NoCopy.wld.sys");

    Button newBtn = new Button("New");
    newBtn.setOnAction(event -> {
      NewWidget wid = new NewWidget();
      wid.setEDW(edworld);
      wid.setEditor(this);
    });
    Button saveBtn = new Button("Save");
    saveBtn.setOnAction(event -> {
      try {
       saved();
       copyWorld(WORKING_FILE_PATH, CURRENT_FILE_PATH);
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText("File saved successfully");
        alert.setTitle("File saved");
        alert.setContentText("File saved successfully.");
        alert.showAndWait();
      } catch (Exception e){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText("Error while parsing file");
        alert.setTitle("Error");
        alert.setContentText("Could not save world file!");
        alert.showAndWait();
      }
      
    });
    Button openBtn = new Button("Open");
    openBtn.setOnAction(event -> {
    try {
	      FileChooser chooser = new FileChooser();
	      chooser.setInitialDirectory(new File(PATH+".labyrinthgame"+File.separator+"Editor"+File.separator+"Levels"+File.separator+""));
	      chooser.setTitle("Open world");
	      chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("World file", "*.wld"));
	      File f = chooser.showOpenDialog(this.stage);
        open(f);
      } catch (Exception e){
             Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Error while parsing file");
            alert.setTitle("Error");
            alert.setContentText("Could not open world file!");
            alert.showAndWait();
      }
    });

    Button addCBtn = new Button("AC");
    addCBtn.setOnAction(event -> {if(checkValidityMax("w")){edworld.addColumn(); unsaved();}});
    Button addRBtn = new Button("AR");
    addRBtn.setOnAction(event -> {if(checkValidityMax("h")){edworld.addRow(); unsaved();}});
    Button rmCBtn = new Button("RC");
    rmCBtn.setOnAction(event -> {checkValidity(edworld.removeColumn()); unsaved();});
    Button rmRBtn = new Button("RR");
    rmRBtn.setOnAction(event -> {checkValidity(edworld.removeRow()); unsaved();});

    Button runBtn = new Button("Run");
    runBtn.setOnAction(event -> new LevelExe(CURRENT_FILE_PATH, getFileName(), saved));

    Button sseBtn = new Button("SSE");
    sseBtn.setOnAction(event -> new SESetup(edworld, edworld.width, edworld.height, edworld.start, edworld.end));

    toolbar.getItems().addAll(newBtn, saveBtn, openBtn, new Separator(), addCBtn, addRBtn, rmCBtn, rmRBtn, new Separator(), sseBtn, new Separator(), runBtn);

    // Setup world editor
    ScrollPane scrollpane = new ScrollPane();
    scrollpane.setPrefSize(700, 460);

    if (getCurrentFilePath() == null){
    	NewWidget wid = new NewWidget();
    	wid.setEDW(edworld);
    	wid.setEditor(this);
    } else if (editorFilePath == null){
    	System.out.println("Last file: "+getCurrentFilePath());
    	open(new File(getCurrentFilePath()));
    } else {
      System.out.println("Opening: "+editorFilePath);
      open(new File(editorFilePath));
    }

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
        if (edblock.getType() == EditableWorld.AIR && (edblock.isOnStart(edworld) || edblock.isOnEnd(edworld))){
          System.out.println("SSE Error (3)");
          Alert alert = new Alert(Alert.AlertType.ERROR);
          alert.setHeaderText("Could not place block on start or end position");
          alert.setTitle("SSE Error");
          alert.setContentText(null);
          alert.showAndWait();
          return;
        }
        edblock.toggleType();
        edworld.setBlockOn(edblock);
        edworld.updateOnFile();
        unsaved();
      }
    });

    final Label pointingOn = new Label("Mouse on Block: null");

    canvas.setOnMouseMoved(new EventHandler<MouseEvent>(){
      @Override
      public void handle(MouseEvent event){
        Block block = edworld.getBlockAtCoord((int)event.getX(), (int)event.getY());
        pointingOn.setText("Mouse on block: "+block+" "+((block.isOnStart(edworld)) ? "On start position" : ((block.isOnEnd(edworld)) ? "On end position" : "Not on start or end position")));
      }
    });  

    layout.add(toolbar, 0, 0);
    layout.add(scrollpane, 0, 1);
    layout.add(pointingOn, 0, 2);
    this.stage.setScene(new Scene(layout, 705, 500));
    //this.stage.setResizable(false)
  }

  public void start(){
    this.stage.show();
  }
  
  public static void updateCurrentWorldFile(String currentPath){
  	File f = new File(PATH+".labyrinthgame"+File.separator+"Editor"+File.separator+"Cache"+File.separator+"currentFile.data");
  	try {
	  	if (!f.exists()){
	  		f.createNewFile();
	  	}
  		BufferedWriter writer = new BufferedWriter(new FileWriter(f));
  		writer.write(currentPath);
  		writer.close();
  	} catch (IOException e){
  	}
  }
  
  public String getCurrentFilePath(){
  	 File f = new File(PATH+".labyrinthgame"+File.separator+"Editor"+File.separator+"Cache"+File.separator+"currentFile.data");
  	 if (!f.exists()){
  	 	return null;
  	 }
	  try {
	  	BufferedReader reader = new BufferedReader(new FileReader(f));
	  	String p = reader.readLine();
	  	reader.close();
	  	return p;
	  } catch (IOException e){
	  }
	  return null;
  }

  public void open(File f){
    try {
      Random r = new Random();
      int number = r.nextInt();

      CURRENT_FILE_PATH = f.getAbsolutePath();
      WORKING_FILE_PATH = PATH +".labyrinthgame"+File.separator+"Editor"+File.separator+"Cache"+File.separator+"cache["+getFileName()+"]"+number+".wld.ns"; // ns = not saved
      copyWorld(CURRENT_FILE_PATH, WORKING_FILE_PATH);
      edworld.changeToWorld(WORKING_FILE_PATH);
      updateCurrentWorldFile(CURRENT_FILE_PATH);
      worldList.addToList(CURRENT_FILE_PATH);
      saved();
    } catch (Exception e){
      Alert alert = new Alert(Alert.AlertType.ERROR);
      alert.setHeaderText("Error while parsing file");
      alert.setTitle("Error");
      alert.setContentText("Could not load world file!");
      alert.showAndWait();
      e.printStackTrace();
    }
  }
  
 private void checkValidity(boolean value){
  	if (!value){
  		  Alert alert = new Alert(Alert.AlertType.ERROR);
	      alert.setHeaderText("Could not delete row/column");
	      alert.setTitle("Error");
	      Label label = new Label("You can not delete the last row/column if start\nor end position is contained in it!");
	      label.setWrapText(true);
	      alert.getDialogPane().setContent(label);
	      alert.showAndWait();
  	}
  }

  private boolean checkValidityMax(String s){
    if (this.edworld.width+1 > NewWidget.MAX_WORLD_SIZE && s == "w"){
      Alert alert = new Alert(Alert.AlertType.ERROR);
      alert.setHeaderText("You reached world maximum size!");
      alert.setTitle("MaxSizeError");
      alert.setContentText(null);
      alert.showAndWait();
      return false;
    } else if (this.edworld.height+1 > NewWidget.MAX_WORLD_SIZE && s == "h"){
      Alert alert = new Alert(Alert.AlertType.ERROR);
      alert.setHeaderText("You reached world maximum size!");
      alert.setTitle("MaxSizeError");
      alert.setContentText(null);
      alert.showAndWait();
      return false;
    } else {
      return true;
    }
  }

  private void unsaved(){
    this.saved = false;
    this.stage.setTitle("LabyrinthGame - Editor ("+getFileName()+((saved) ? "" : "*")+")");
  }

  private void saved(){
    this.saved = true;
    this.stage.setTitle("LabyrinthGame - Editor ("+getFileName()+((saved) ? "" : "*")+")");
  }

  private void createNewWorld(String name){
    File f = new File(PATH+".labyrinthgame"+File.separator+"Editor"+File.separator+"Levels"+File.separator+""+name+".wld"+ ((name == "testSystemWorld-DefaultName_NoCopy") ? ".sys" : ""));
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

  private static void checkAndCreateDir(String path){
    File f = new File(path);
    if (!f.exists()){
      f.mkdir();
    }
  }

  public static void setupDirectory(){
    checkAndCreateDir(PATH+".labyrinthgame");
    checkAndCreateDir(PATH+".labyrinthgame"+File.separator+"Editor");
    checkAndCreateDir(PATH+".labyrinthgame"+File.separator+"Editor"+File.separator+"Cache");
    checkAndCreateDir(PATH+".labyrinthgame"+File.separator+"Editor"+File.separator+"Levels");
  }

  private void copyWorld(String path1, String path2){
    try {
      File second = new File(path2);  // Delete second file if exists for replacement
      if (second.exists()) {
        second.delete();
      }
      Files.copy(new File(path1).toPath(), new File(path2).toPath());
    } catch (IOException e){
      e.printStackTrace();
    }
  }

  private String getFileName(){
    Path path = Paths.get(CURRENT_FILE_PATH);
    Path fileName = path.getFileName();
    return fileName.toString();
  }
}
