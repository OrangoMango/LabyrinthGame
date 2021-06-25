package com.orangomango.labyrinth.menu.editor;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.TilePane;
import javafx.geometry.Orientation;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.canvas.*;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Separator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tab;
import javafx.scene.control.SplitPane;
import javafx.scene.image.*;
import javafx.stage.FileChooser;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.Random;
import java.util.Arrays;

import com.orangomango.labyrinth.Player;
import com.orangomango.labyrinth.Block;
import com.orangomango.labyrinth.menu.createdlevels.CreatedWorldFiles;
import static com.orangomango.labyrinth.menu.Menu.EDITOR;
import com.orangomango.labyrinth.Logger;

public class Editor {
	private Stage stage;
	private final EditableWorld edworld;
	public final static String PATH = System.getProperty("user.home") + File.separator;
	private static String WORKING_FILE_PATH = "";
	private static String CURRENT_FILE_PATH = "";
	private boolean saved = true;
	public static CreatedWorldFiles worldList;
	public static boolean DONE = true;
	private static int SELECTED_BLOCK = 1;
	private TabPane tabs;
        
        private static String[] WORKING_FILE_PATHS = new String[0];
        private static String[] CURRENT_FILE_PATHS = new String[0];
        private static int OPENED_TABS = 0;
        private static boolean[] SAVES = new boolean[0];

	private String changeSlash(String input) {
		StringBuilder output = new StringBuilder();
                if (input.contains("\\")){
                    output.append("/");
                }
		for (char c: input.toCharArray()) {
			if (Character.toString(c).equals("\\")) {
				output.append("/");
			} else {
				output.append(c);
			}
		}
		return output.toString();
	}
        
        private GridPane getEditorTabContent(){
            GridPane layout = new GridPane();
            
            ScrollPane scrollpane = new ScrollPane();

            EditableWorld editableworld = new EditableWorld(WORKING_FILE_PATH);
            System.out.println(CURRENT_FILE_PATH+" "+WORKING_FILE_PATH);
            Canvas canvas = new Canvas(editableworld.width * EditableWorld.BLOCK_WIDTH, editableworld.height * EditableWorld.BLOCK_WIDTH);
            canvas.setFocusTraversable(true);
            
            canvas.setOnMousePressed(new EventHandler<MouseEvent> () {
                @Override
                public void handle(MouseEvent event) {
                        EditableBlock edblock = EditableBlock.fromBlock(editableworld.getBlockAtCoord((int) event.getX(), (int) event.getY()));
                        if (edblock.getType() == EditableWorld.AIR && (edblock.isOnStart(editableworld) || edblock.isOnEnd(editableworld))) {
                                Logger.warning("Could not place block on start or end position");
                                Alert alert = new Alert(Alert.AlertType.ERROR);
                                alert.setHeaderText("Could not place block on start or end position");
                                alert.setTitle("SSE Error");
                                alert.setContentText(null);
                                alert.showAndWait();
                                return;
                        }
                        switch (SELECTED_BLOCK){
                                case 1:
                                        edblock.toggleType();
                                        break;
                                case 2:
                                        edblock.setType(EditableWorld.NULL);
                                        break;
                        }
                        editableworld.setBlockOn(edblock);
                        editableworld.updateOnFile();
                        unsaved();
                }
            });
            
            scrollpane.setContent(canvas);

            GraphicsContext pen = canvas.getGraphicsContext2D();
            editableworld.setPen(pen);
            editableworld.setPlayer(new Player(editableworld.start[0], editableworld.start[1], editableworld));
            editableworld.setCanvas(canvas);
            editableworld.draw();

            final Label pointingOn = new Label("Mouse on Block: null");
            canvas.setOnMouseMoved(new EventHandler<MouseEvent> () {
                    @Override
                    public void handle(MouseEvent event) {
                            Block block = editableworld.getBlockAtCoord((int) event.getX(), (int) event.getY());
                            pointingOn.setText("Mouse on block: " + block + " " + ((block.isOnStart(editableworld)) ? "On start position" : ((block.isOnEnd(editableworld)) ? "On end position" : "Not on start or end position")));
                    }
            });
            
            scrollpane.setPrefSize(this.stage.getWidth(), this.stage.getHeight());
            
            this.stage.widthProperty().addListener((obs, oldVal, newVal) -> scrollpane.setPrefSize((double) newVal, this.stage.getHeight()));
            this.stage.heightProperty().addListener((obs, oldVal, newVal) -> scrollpane.setPrefSize(this.stage.getWidth(), (double) newVal));
            
            layout.add(scrollpane, 0, 0);
            layout.add(pointingOn, 0, 1, 2, 1);
				
            
            return layout;
        }

	public Editor(String editorFilePath) {
		worldList = new CreatedWorldFiles();
		this.stage = new Stage();
		this.stage.setTitle("LabyrinthGame - Editor (" + getFileName() + ((saved) ? "" : "*") + ")");
		this.stage.setOnCloseRequest(event -> {
                    EDITOR = false;
                    WORKING_FILE_PATHS = new String[1];
                    CURRENT_FILE_PATHS = new String[1];
                    SAVES = new boolean[1];
                    OPENED_TABS = 0;
                });

		GridPane layout = new GridPane();

		// Setup the toolbar
		ToolBar toolbar = new ToolBar();
		toolbar.setOrientation(Orientation.HORIZONTAL);

		createNewWorld("testSystemWorld-DefaultName_NoCopy");
		edworld = new EditableWorld(PATH + ".labyrinthgame" + File.separator + "Editor" + File.separator + "Levels" + File.separator + "testSystemWorld-DefaultName_NoCopy.wld.sys");
		Button newBtn = new Button("New");
		newBtn.setGraphic(new ImageView(new Image("file://" + changeSlash(PATH) + ".labyrinthgame/Images/new.png")));
		newBtn.setOnAction(event -> {
			NewWidget wid = new NewWidget(false);
			wid.setEDW(edworld);
			wid.setEditor(this);
		});
		Button saveBtn = new Button("Save");
		saveBtn.setGraphic(new ImageView(new Image("file://" + changeSlash(PATH) + ".labyrinthgame/Images/save.png")));
		saveBtn.setOnAction(event -> {
			try {
				saved();
				copyWorld(WORKING_FILE_PATH, CURRENT_FILE_PATH);
				Alert alert = new Alert(Alert.AlertType.INFORMATION);
				alert.setHeaderText("File saved successfully");
				alert.setTitle("File saved");
				alert.setContentText("File saved successfully.");
				alert.showAndWait();
			} catch (Exception e) {
				Alert alert = new Alert(Alert.AlertType.ERROR);
				alert.setHeaderText("Error while parsing file");
				alert.setTitle("Error");
				alert.setContentText("Could not save world file!");
				alert.showAndWait();
			}

		});
		Button openBtn = new Button("Open");
		openBtn.setGraphic(new ImageView(new Image("file://" + changeSlash(PATH) + ".labyrinthgame/Images/open.png")));
		openBtn.setOnAction(event -> {
			try {
				FileChooser chooser = new FileChooser();
				chooser.setInitialDirectory(new File(PATH + ".labyrinthgame" + File.separator + "Editor" + File.separator + "Levels" + File.separator));
				chooser.setTitle("Open world");
				chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("World file", "*.wld"));
				File f = chooser.showOpenDialog(this.stage);
				if (f.equals(null)){
					throw new Exception("Null file opened");
				}
				open(f);
			} catch (Exception e) {
				Alert alert = new Alert(Alert.AlertType.ERROR);
				alert.setHeaderText("Error while parsing file");
				alert.setTitle("Error");
				alert.setContentText("Could not open world file!");
				alert.showAndWait();
			}
		});

		Button addCBtn = new Button();
		addCBtn.setGraphic(new ImageView(new Image("file://" + changeSlash(PATH) + ".labyrinthgame/Images/ac.png")));
		addCBtn.setOnAction(event -> {
			if (checkValidityMax("w")) {
				edworld.addColumn();
				unsaved();
			}
		});
		Button addRBtn = new Button();
		addRBtn.setGraphic(new ImageView(new Image("file://" + changeSlash(PATH) + ".labyrinthgame/Images/ar.png")));
		addRBtn.setOnAction(event -> {
			if (checkValidityMax("h")) {
				edworld.addRow();
				unsaved();
			}
		});
		Button rmCBtn = new Button();
		rmCBtn.setGraphic(new ImageView(new Image("file://" + changeSlash(PATH) + ".labyrinthgame/Images/rc.png")));
		rmCBtn.setOnAction(event -> {
			checkValidity(edworld.removeColumn());unsaved();
		});
		Button rmRBtn = new Button();
		rmRBtn.setGraphic(new ImageView(new Image("file://" + changeSlash(PATH) + ".labyrinthgame/Images/rr.png")));
		rmRBtn.setOnAction(event -> {
			checkValidity(edworld.removeRow());unsaved();
		});

		Button runBtn = new Button("Run");
		runBtn.setGraphic(new ImageView(new Image("file://" + changeSlash(PATH) + ".labyrinthgame/Images/run.png")));
		runBtn.setOnAction(event -> {
			new LevelExe(CURRENT_FILE_PATH, getFileName(), saved);LevelExe.setOnFinish(null);
		});

		Button sseBtn = new Button();
		sseBtn.setGraphic(new ImageView(new Image("file://" + changeSlash(PATH) + ".labyrinthgame/Images/sse.png")));
		sseBtn.setOnAction(event -> new SESetup(edworld, edworld.width, edworld.height, edworld.start, edworld.end));

		toolbar.getItems().addAll(newBtn, saveBtn, openBtn, new Separator(), addCBtn, addRBtn, rmCBtn, rmRBtn, new Separator(), sseBtn, new Separator(), runBtn);

		// Setup world editor
		ScrollPane scrollpane = new ScrollPane();
		this.stage.widthProperty().addListener((obs, oldVal, newVal) -> scrollpane.setPrefSize((double) newVal, this.stage.getHeight()));
		this.stage.heightProperty().addListener((obs, oldVal, newVal) -> scrollpane.setPrefSize(this.stage.getWidth(), (double) newVal));

		if (editorFilePath != null) {
			Logger.info("Opening: " + editorFilePath);
			open(new File(editorFilePath));
		} else if (getCurrentFilePath() == null) {
			DONE = false;
			Selection sel = new Selection(edworld, this);
		} else {
			Logger.info("Last file: " + getCurrentFilePath());
			open(new File(getCurrentFilePath()));
		}

		Canvas canvas = new Canvas(edworld.width * EditableWorld.BLOCK_WIDTH, edworld.height * EditableWorld.BLOCK_WIDTH);
		canvas.setFocusTraversable(true);
		scrollpane.setContent(canvas);

		GraphicsContext pen = canvas.getGraphicsContext2D();
		edworld.setPen(pen);
		edworld.setPlayer(new Player(edworld.start[0], edworld.start[1], edworld));
		edworld.setCanvas(canvas);
		edworld.draw();

		canvas.setOnMousePressed(new EventHandler<MouseEvent> () {
			@Override
			public void handle(MouseEvent event) {
				EditableBlock edblock = EditableBlock.fromBlock(edworld.getBlockAtCoord((int) event.getX(), (int) event.getY()));
				if (edblock.getType() == EditableWorld.AIR && (edblock.isOnStart(edworld) || edblock.isOnEnd(edworld))) {
					Logger.warning("Could not place block on start or end position");
					Alert alert = new Alert(Alert.AlertType.ERROR);
					alert.setHeaderText("Could not place block on start or end position");
					alert.setTitle("SSE Error");
					alert.setContentText(null);
					alert.showAndWait();
					return;
				}
				switch (SELECTED_BLOCK){
					case 1:
						edblock.toggleType();
						break;
					case 2:
						edblock.setType(EditableWorld.NULL);
						break;
				}
				edworld.setBlockOn(edblock);
				edworld.updateOnFile();
				unsaved();
			}
		});

		final Label pointingOn = new Label("Mouse on Block: null");

		canvas.setOnMouseMoved(new EventHandler<MouseEvent> () {
			@Override
			public void handle(MouseEvent event) {
				Block block = edworld.getBlockAtCoord((int) event.getX(), (int) event.getY());
				pointingOn.setText("Mouse on block: " + block + " " + ((block.isOnStart(edworld)) ? "On start position" : ((block.isOnEnd(edworld)) ? "On end position" : "Not on start or end position")));
			}
		});
		
		SplitPane splitpane = new SplitPane();
		
		this.tabs = new TabPane();
		this.stage.widthProperty().addListener((obs, oldVal, newVal) -> tabs.setPrefSize((double) newVal, this.stage.getHeight()));
		this.stage.heightProperty().addListener((obs, oldVal, newVal) -> tabs.setPrefSize(this.stage.getWidth(), (double) newVal));
		Tab editTab = new Tab(getFileName());
		editTab.setClosable(false);
		GridPane editorGrid = new GridPane();
		editorGrid.add(scrollpane, 0, 0);
		editorGrid.add(pointingOn, 0, 1, 2, 1);
		editTab.setContent(editorGrid);

		this.tabs.getSelectionModel().selectedItemProperty().addListener((ov, ot, nt) -> {
                        if (CURRENT_FILE_PATHS.length > 0 || WORKING_FILE_PATHS.length > 0){
                            int index = this.tabs.getSelectionModel().getSelectedIndex();
                            CURRENT_FILE_PATH = CURRENT_FILE_PATHS[index];
                            WORKING_FILE_PATH = WORKING_FILE_PATHS[index];
                            saved = SAVES[index];
                            this.stage.setTitle("LabyrinthGame - Editor (" + getFileName() + ((saved) ? "" : "*") + ")");
                        }
		});

		tabs.getTabs().add(editTab);
		splitpane.getItems().add(tabs);
		
		
		TilePane blockPane = new TilePane();
		
		ToggleGroup group = new ToggleGroup();
		ToggleButton toggleBlock = new ToggleButton("Toggle type");
		toggleBlock.setToggleGroup(group);
		toggleBlock.setSelected(true);
		toggleBlock.setOnAction(event -> SELECTED_BLOCK = 1);
		ToggleButton nullBlock = new ToggleButton("Null block");
		nullBlock.setOnAction(event -> SELECTED_BLOCK = 2);
		nullBlock.setToggleGroup(group);
		
		blockPane.getChildren().addAll(toggleBlock, nullBlock);
		splitpane.getItems().add(blockPane);
		
		// Set the divider on 80%
		splitpane.setDividerPositions(0.8f);
		this.stage.widthProperty().addListener((obs, oldVal, newVal) -> splitpane.setDividerPositions(0.8f));
		this.stage.heightProperty().addListener((obs, oldVal, newVal) -> splitpane.setDividerPositions(0.8f));

		layout.add(toolbar, 0, 0);
		layout.add(splitpane, 0, 1);

		this.stage.setScene(new Scene(layout, 775, 650));
	}

	public void start() {
		if (DONE) {
			this.stage.show();
		}
	}

	public static void updateCurrentWorldFile(String currentPath) {
		File f = new File(PATH + ".labyrinthgame" + File.separator + "Editor" + File.separator + "Cache" + File.separator + "currentFile.data");
		try {
			if (!f.exists()) {
				f.createNewFile();
			}
			BufferedWriter writer = new BufferedWriter(new FileWriter(f));
			writer.write(currentPath);
			writer.close();
		} catch (IOException e) {}
	}

	public static String getCurrentFilePath() {
		File f = new File(PATH + ".labyrinthgame" + File.separator + "Editor" + File.separator + "Cache" + File.separator + "currentFile.data");
		if (!f.exists()) {
			return null;
		}
		try {
			BufferedReader reader = new BufferedReader(new FileReader(f));
			String p = reader.readLine();
			reader.close();
			return p;
		} catch (IOException e) {}
		return null;
	}

	public void open(File f) {
		try {
			Random r = new Random();
			int number = r.nextInt();
                        
                        if (Arrays.asList(CURRENT_FILE_PATHS).contains(f.getAbsolutePath())){
                            this.tabs.getSelectionModel().select(Arrays.asList(CURRENT_FILE_PATHS).indexOf(f.getAbsolutePath()));
                            return;
                        }
                        
			CURRENT_FILE_PATH = f.getAbsolutePath();
			WORKING_FILE_PATH = PATH + ".labyrinthgame" + File.separator + "Editor" + File.separator + "Cache" + File.separator + "cache[" + getFileName() + "]" + number + ".wld.ns"; // ns = not saved
                        
                        CURRENT_FILE_PATHS = Arrays.copyOf(CURRENT_FILE_PATHS, OPENED_TABS+1);
                        WORKING_FILE_PATHS = Arrays.copyOf(WORKING_FILE_PATHS, OPENED_TABS+1);
                        SAVES = Arrays.copyOf(SAVES, OPENED_TABS+1);
                        CURRENT_FILE_PATHS[OPENED_TABS] = CURRENT_FILE_PATH;
                        WORKING_FILE_PATHS[OPENED_TABS] = WORKING_FILE_PATH;
                        SAVES[OPENED_TABS] = true;
                        OPENED_TABS++;
                        
                        Logger.info(Arrays.toString(CURRENT_FILE_PATHS)+" "+Arrays.toString(WORKING_FILE_PATHS));
                        
                        copyWorld(CURRENT_FILE_PATH, WORKING_FILE_PATH);
                        System.out.println(OPENED_TABS);
                        if (this.tabs != null && getCurrentFilePath() != null){
                        	Tab newTab = new Tab(f.getName());
                                newTab.setClosable(false);
                                newTab.setContent(getEditorTabContent());
                        	
                        	this.tabs.getTabs().add(newTab);
                                this.tabs.getSelectionModel().select(newTab);
                        } else {
                                if (this.tabs != null){
                                    this.tabs.getSelectionModel().getSelectedItem().setText(getFileName());
                                }
                        	edworld.changeToWorld(WORKING_FILE_PATH);
                        }
			updateCurrentWorldFile(CURRENT_FILE_PATH);
			worldList.addToList(CURRENT_FILE_PATH);
			saved();
		} catch (Exception e) {
			Logger.error("Could not load world file");
			Alert alert = new Alert(Alert.AlertType.ERROR);
			alert.setHeaderText("Error while parsing file");
			alert.setTitle("Error");
			alert.setContentText("Could not load world file!");
			alert.showAndWait();
			e.printStackTrace();
		}
	}

	private void checkValidity(boolean value) {
		if (!value) {
			Alert alert = new Alert(Alert.AlertType.ERROR);
			alert.setHeaderText("Could not delete row/column");
			alert.setTitle("Error");
			Label label = new Label("You can not delete the last row/column if start\nor end position is contained in it!");
			label.setWrapText(true);
			alert.getDialogPane().setContent(label);
			alert.showAndWait();
		}
	}

	private boolean checkValidityMax(String s) {
		if (this.edworld.width + 1 > NewWidget.MAX_WORLD_SIZE && s == "w") {
			Alert alert = new Alert(Alert.AlertType.ERROR);
			alert.setHeaderText("You reached world maximum size!");
			alert.setTitle("MaxSizeError");
			alert.setContentText(null);
			alert.showAndWait();
			return false;
		} else if (this.edworld.height + 1 > NewWidget.MAX_WORLD_SIZE && s == "h") {
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

	private void unsaved() {
		this.saved = false;
                try {
                    SAVES[this.tabs.getSelectionModel().getSelectedIndex()] = saved;
                    this.tabs.getSelectionModel().getSelectedItem().setText(getFileName() + ((saved) ? "" : "*"));
                } catch (NullPointerException e){
                    SAVES[0] = saved;
                }
		this.stage.setTitle("LabyrinthGame - Editor (" + getFileName() + ((saved) ? "" : "*") + ")");
	}

	private void saved() {
		this.saved = true;
                try {
                    SAVES[this.tabs.getSelectionModel().getSelectedIndex()] = saved;
                    this.tabs.getSelectionModel().getSelectedItem().setText(getFileName() + ((saved) ? "" : "*"));
                } catch (NullPointerException e){
                    SAVES[0] = saved;
                }
		this.stage.setTitle("LabyrinthGame - Editor (" + getFileName() + ((saved) ? "" : "*") + ")");
	}

	private void createNewWorld(String name) {
		File f = new File(PATH + ".labyrinthgame" + File.separator + "Editor" + File.separator + "Levels" + File.separator + "" + name + ".wld" + ((name == "testSystemWorld-DefaultName_NoCopy") ? ".sys" : ""));
		try {
			f.createNewFile();
			BufferedWriter writer = new BufferedWriter(new FileWriter(f));
			writer.write("2x2\n");
			writer.write("1,0,0,0\n");
			writer.write("1,0\n");
			writer.write("1,1");
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void checkAndCreateDir(String path) {
		File f = new File(path);
		if (!f.exists()) {
			f.mkdir();
		}
	}

	public static void setupDirectory() {
		checkAndCreateDir(PATH + ".labyrinthgame");
		checkAndCreateDir(PATH + ".labyrinthgame" + File.separator + "SystemLevels");
		checkAndCreateDir(PATH + ".labyrinthgame" + File.separator + "Editor");
		checkAndCreateDir(PATH + ".labyrinthgame" + File.separator + "Images");
		checkAndCreateDir(PATH + ".labyrinthgame" + File.separator + "Editor" + File.separator + "Cache");
		checkAndCreateDir(PATH + ".labyrinthgame" + File.separator + "Editor" + File.separator + "Levels");
	}

	private void copyWorld(String path1, String path2) {
		try {
			File second = new File(path2); // Delete second file if exists for replacement
			if (second.exists()) {
				second.delete();
			}
			Files.copy(new File(path1).toPath(), new File(path2).toPath());
                        System.out.println("COPY: "+path1+" "+path2);
                        Logger.info("World copied from cache to file");
		} catch (IOException e) {
			Logger.warning("Unable to copy world from cache to file");
		}
	}

	private String getFileName() {
		Path path = Paths.get(CURRENT_FILE_PATH);
		Path fileName = path.getFileName();
		return fileName.toString();
	}
}
