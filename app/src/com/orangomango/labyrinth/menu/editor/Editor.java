package com.orangomango.labyrinth.menu.editor;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.TilePane;
import javafx.geometry.*;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.canvas.*;
import javafx.scene.control.*;
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
import com.orangomango.labyrinth.Logger;

public class Editor {
	private Stage stage;
	private EditableWorld edworld;
	public final static String PATH = System.getProperty("user.home") + File.separator;
	private static String WORKING_FILE_PATH = "";
	private static String CURRENT_FILE_PATH = "";
	private boolean saved = true;
	public static CreatedWorldFiles worldList;
	public static boolean DONE = true;
	private static int SELECTED_BLOCK = 1;
	private TabPane tabs;
	private static boolean EDITOR = false;
  public static Editor EDITOR_INSTANCE = null;

	private static String[] WORKING_FILE_PATHS = new String[0];
	private static String[] CURRENT_FILE_PATHS = new String[0];
	private static int OPENED_TABS = 0;
	private static boolean[] SAVES = new boolean[0];
	private static EditableWorld[] WORLDS = new EditableWorld[0];
    
    /**
     * Change the slash of the path from \ to / to make a valid URL under windows.
     * @param input the Path String
     * @return the converted path
    */
	public static String changeSlash(String input) {
		StringBuilder output = new StringBuilder();
		if (input.contains("\\")) {
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
    
    /**
     * Function that creates the editable world inside a tabs
     * @return the GridPane containing the canvas with the world
    */
	private GridPane getEditorTabContent() {
		GridPane layout = new GridPane();

		ScrollPane scrollpane = new ScrollPane();
		scrollpane.requestFocus();

		EditableWorld editableworld;

		if (!WORKING_FILE_PATH.equals("")) {
			editableworld = new EditableWorld(WORKING_FILE_PATH);
		} else {
			editableworld = this.edworld;
		}

		Canvas canvas = new Canvas(editableworld.width * EditableWorld.BLOCK_WIDTH, editableworld.height * EditableWorld.BLOCK_WIDTH);
		canvas.setFocusTraversable(true);

		canvas.setOnMousePressed(new EventHandler<MouseEvent> () {
			@Override
			public void handle(MouseEvent event) {
				EditableBlock edblock = EditableBlock.fromBlock(editableworld.getBlockAtCoord((int) event.getX(), (int) event.getY()));
				if (event.getButton() == MouseButton.SECONDARY){
					ContextMenu contextMenu = new ContextMenu();
					Menu item1 = new Menu("Set other portal end...");
					MenuItem rmPoint = new MenuItem("Remove pointing");
					rmPoint.setOnAction(rmEvent -> {
						String[] data = editableworld.getBlockAtCoord((int)event.getX(), (int)event.getY()).getInfo().split("#")[1].split(" ");
						editableworld.getBlockAtCoord((int) event.getX(), (int) event.getY()).setInfo("NoPointSet");
						editableworld.getBlockAt(Integer.parseInt(data[0]), Integer.parseInt(data[1])).setInfo("NoPointSet");
						editableworld.updateOnFile();
						unsaved();
					});
					rmPoint.setDisable(edblock.getInfo().equals("NoPointSet"));
					item1.getItems().add(rmPoint);
					for (int y = 0; y<editableworld.height; y++){
						for (int x = 0; x<editableworld.width; x++){
							Block b = editableworld.getBlockAt(x, y);
							if (b.getType() == EditableWorld.PORTAL){
								MenuItem menuitem = new MenuItem(b.toString());
								menuitem.setOnAction(itemEvent -> {
									if (!edblock.getInfo().equals("NoPointSet")){
										String[] data = edblock.getInfo().split("#")[1].split(" ");
										editableworld.getBlockAt(Integer.parseInt(data[0]), Integer.parseInt(data[1])).setInfo("NoPointSet");
									}
									if (!b.getInfo().equals("NoPointSet")){
										String[] data = b.getInfo().split("#")[1].split(" ");
										editableworld.getBlockAt(Integer.parseInt(data[0]), Integer.parseInt(data[1])).setInfo("NoPointSet");
									}
									editableworld.getBlockAtCoord((int) event.getX(), (int) event.getY()).setInfo(String.format("point#%s %s", b.getX(), b.getY()));
									b.setInfo(String.format("point#%s %s", edblock.getX(), edblock.getY()));
									editableworld.updateOnFile();
									unsaved();
								});
								if (b.getX() == edblock.getX() && b.getY() == edblock.getY() && b.getX() == edblock.getX()){
									menuitem.setDisable(true);
								}
								item1.getItems().add(menuitem);
							}
						}
					}
					item1.setDisable(edblock.getType() != EditableWorld.PORTAL);
					MenuItem item2 = new Menu("Set bat data");
					item2.setOnAction(batEvent -> {
						Stage st = new Stage();
						st.setTitle("Bat preferences");
						GridPane pane = new GridPane();
						pane.setPadding(new Insets(10,10,10,10));
						pane.setHgap(20);
						pane.setVgap(20);
						Label l1 = new Label("Set path length: ");
						Spinner sp = new Spinner(0,NewWidget.MAX_WORLD_SIZE,0);
						Label l2 = new Label("Set direction:");
						ToggleGroup gr = new ToggleGroup();
						RadioButton b1 = new RadioButton("Vertical");
						RadioButton b2 = new RadioButton("Horizontal");
						b2.setSelected(true);
						b1.setToggleGroup(gr);
						b2.setToggleGroup(gr);
						Button ok = new Button("Save changes");
						ok.setOnAction(ev -> {
							int pl = (int) sp.getValue();
							edblock.setInfo(String.format("data#%s", pl));
							editableworld.setBlockOn(edblock);
							editableworld.updateOnFile();
							unsaved();
							st.hide();
						});
						Button canc = new Button("Cancel");
						canc.setOnAction(e -> st.hide());
						pane.add(l1, 0, 0);
						pane.add(sp, 1, 0);
						pane.add(l2, 0, 1);
						pane.add(b1, 0, 2);
						pane.add(b2, 0, 3);
						pane.add(ok, 0, 4);
						pane.add(canc, 1, 4);
						st.setScene(new Scene(pane, 350, 250));
						st.show();
					});
					item2.setDisable(edblock.getType() != EditableWorld.BAT_GEN);
					contextMenu.getItems().addAll(item1, item2);
					contextMenu.show(canvas, event.getScreenX(), event.getScreenY());
				} else if (event.getButton() == MouseButton.PRIMARY){
					if (edblock.getType() == EditableWorld.AIR && (edblock.isOnStart(editableworld) || edblock.isOnEnd(editableworld))) {
						Logger.warning("Could not place block on start or end position");
						Alert alert = new Alert(Alert.AlertType.ERROR);
						alert.setHeaderText("Could not place block on start or end position");
						alert.setTitle("SSE Error");
						alert.setContentText(null);
						alert.showAndWait();
						return;
					}
					switch (SELECTED_BLOCK) {
						case 1:
							edblock.toggleType(EditableWorld.WALL);
							break;
						case 2:
							edblock.toggleType(EditableWorld.VOID);
							break;
						case 3:
							edblock.toggleType(EditableWorld.SPIKE);
							break;
						case 4:
							if (edblock.getType() == EditableWorld.PORTAL && !edblock.getInfo().equals("NoPointSet")){
								String[] data = editableworld.getBlockAtCoord((int)event.getX(), (int)event.getY()).getInfo().split("#")[1].split(" ");
								editableworld.getBlockAt(Integer.parseInt(data[0]), Integer.parseInt(data[1])).setInfo("NoPointSet");
								
							}
							edblock.setInfo("NoPointSet");
							edblock.toggleType(EditableWorld.PORTAL);
							break;
						case 5:
							edblock.setInfo("direction#w");
							edblock.toggleType(EditableWorld.SHOOTER);
							break;
						case 6:
							edblock.setInfo("NoDataSet");
							edblock.toggleType(EditableWorld.BAT_GEN);
							break;
					}
					editableworld.setBlockOn(edblock);
					editableworld.updateOnFile();
					unsaved();
				}
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
				pointingOn.setText("Mouse on block: " + block + " | " + ((block.isOnStart(editableworld)) ? "On start position" : ((block.isOnEnd(editableworld)) ? "On end position" : "Not on start or end position")));
			}
		});

		scrollpane.setPrefSize(this.stage.getWidth(), this.stage.getHeight());

		this.stage.widthProperty().addListener((obs, oldVal, newVal) -> scrollpane.setPrefSize((double) newVal, this.stage.getHeight()));
		this.stage.heightProperty().addListener((obs, oldVal, newVal) -> scrollpane.setPrefSize(this.stage.getWidth(), (double) newVal));

		layout.add(scrollpane, 0, 0);
		layout.add(pointingOn, 0, 1, 2, 1);
		this.edworld = editableworld;
		if (OPENED_TABS > 0) {
			WORLDS = Arrays.copyOf(WORLDS, OPENED_TABS);
			WORLDS[OPENED_TABS - 1] = editableworld;
		}

		return layout;
	}

    /**
     * Editor class constructor. Setups all editor window (toolbar, canvas, tabs, ...)
     * @param editorFilePath the file path to open
    */
	public Editor(String editorFilePath) {
		if (EDITOR){
			return;
		}
        EDITOR_INSTANCE = this;
		EDITOR = true;
		worldList = new CreatedWorldFiles();
		this.stage = new Stage();
		this.stage.setTitle("LabyrinthGame - Editor (" + getFileName() + ((saved) ? "" : "*") + ")");
		this.stage.setOnCloseRequest(event -> {
			EDITOR = false;
			WORKING_FILE_PATHS = new String[1];
			CURRENT_FILE_PATHS = new String[1];
			SAVES = new boolean[1];
			OPENED_TABS = 0;
            EDITOR_INSTANCE = null;
		});

		GridPane layout = new GridPane();

		// Setup the toolbar
		ToolBar toolbar = new ToolBar();
		toolbar.setOrientation(Orientation.HORIZONTAL);

		createNewWorld("testSystemWorld-DefaultName_NoCopy");
		edworld = new EditableWorld(PATH + ".labyrinthgame" + File.separator + "Editor" + File.separator + "Levels" + File.separator + "testSystemWorld-DefaultName_NoCopy.wld.sys");
		Button newBtn = new Button("New");
		newBtn.setGraphic(new ImageView(new Image("file://" + changeSlash(PATH) + ".labyrinthgame/Images/editor/new.png")));
                newBtn.setTooltip(new Tooltip("Create a new world file"));
		newBtn.setOnAction(event -> {
			NewWidget wid = new NewWidget(false);
			wid.setEDW(edworld);
			wid.setEditor(this);
		});
		Button saveBtn = new Button("Save");
		saveBtn.setGraphic(new ImageView(new Image("file://" + changeSlash(PATH) + ".labyrinthgame/Images/editor/save.png")));
                saveBtn.setTooltip(new Tooltip("Save file"));
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
		openBtn.setGraphic(new ImageView(new Image("file://" + changeSlash(PATH) + ".labyrinthgame/Images/editor/open.png")));
        openBtn.setTooltip(new Tooltip("Open a world file"));
		openBtn.setOnAction(event -> {
			try {
				FileChooser chooser = new FileChooser();
				chooser.setInitialDirectory(new File(PATH + ".labyrinthgame" + File.separator + "Editor" + File.separator + "Levels" + File.separator));
				chooser.setTitle("Open world");
				chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("World file", "*.wld"));
				File f = chooser.showOpenDialog(this.stage);
				if (f.equals(null)) {
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
        addCBtn.setTooltip(new Tooltip("Add column to world"));
		addCBtn.setGraphic(new ImageView(new Image("file://" + changeSlash(PATH) + ".labyrinthgame/Images/editor/ac.png")));
		addCBtn.setOnAction(event -> {
			if (checkValidityMax("w")) {
				edworld.addColumn();
				unsaved();
			}
		});
		Button addRBtn = new Button();
        addRBtn.setTooltip(new Tooltip("Add row to world"));
		addRBtn.setGraphic(new ImageView(new Image("file://" + changeSlash(PATH) + ".labyrinthgame/Images/editor/ar.png")));
		addRBtn.setOnAction(event -> {
			if (checkValidityMax("h")) {
				edworld.addRow();
				unsaved();
			}
		});
		Button rmCBtn = new Button();
        rmCBtn.setTooltip(new Tooltip("Remove column from world"));
		rmCBtn.setGraphic(new ImageView(new Image("file://" + changeSlash(PATH) + ".labyrinthgame/Images/editor/rc.png")));
		rmCBtn.setOnAction(event -> {
			checkValidity(edworld.removeColumn());
			unsaved();
		});
		Button rmRBtn = new Button();
        rmRBtn.setTooltip(new Tooltip("Remove row from world"));
		rmRBtn.setGraphic(new ImageView(new Image("file://" + changeSlash(PATH) + ".labyrinthgame/Images/editor/rr.png")));
		rmRBtn.setOnAction(event -> {
			checkValidity(edworld.removeRow());
			unsaved();
		});

		Button runBtn = new Button("Run");
        runBtn.setTooltip(new Tooltip("Run current level"));
		runBtn.setGraphic(new ImageView(new Image("file://" + changeSlash(PATH) + ".labyrinthgame/Images/editor/run.png")));
		runBtn.setOnAction(event -> {
			new LevelExe(CURRENT_FILE_PATH, getFileName(), saved);
			LevelExe.setOnFinish(null);
		});

		Button sseBtn = new Button();
        sseBtn.setTooltip(new Tooltip("Change start and end position"));
		sseBtn.setGraphic(new ImageView(new Image("file://" + changeSlash(PATH) + ".labyrinthgame/Images/editor/sse.png")));
		sseBtn.setOnAction(event -> {new SESetup(edworld, edworld.width, edworld.height, edworld.start, edworld.end); unsaved();});

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

		SplitPane splitpane = new SplitPane();

		this.tabs = new TabPane();
		this.stage.widthProperty().addListener((obs, oldVal, newVal) -> tabs.setPrefSize((double) newVal, this.stage.getHeight()));
		this.stage.heightProperty().addListener((obs, oldVal, newVal) -> tabs.setPrefSize(this.stage.getWidth(), (double) newVal));
		Tab editTab = new Tab(getFileName());
		editTab.setClosable(false);
		editTab.setContent(getEditorTabContent());

		this.tabs.getSelectionModel().selectedItemProperty().addListener((ov, ot, nt) -> {
			if (CURRENT_FILE_PATHS.length > 0 || WORKING_FILE_PATHS.length > 0) {
				int index = this.tabs.getSelectionModel().getSelectedIndex();
				CURRENT_FILE_PATH = CURRENT_FILE_PATHS[index];
				WORKING_FILE_PATH = WORKING_FILE_PATHS[index];
				saved = SAVES[index];
				edworld = WORLDS[index];
				this.stage.setTitle("LabyrinthGame - Editor (" + getFileName() + ((saved) ? "" : "*") + ")");
			}
		});

		tabs.getTabs().add(editTab);
		splitpane.getItems().add(tabs);

                Accordion blockSelect = new Accordion();
                ToggleGroup tg = new ToggleGroup();
                
                // Default blocks
                TilePane db = new TilePane();
                db.setHgap(5);
                db.setVgap(2);
                ToggleButton wallB = new ToggleButton();
                wallB.setGraphic(new ImageView(new Image("file://" + changeSlash(PATH) + ".labyrinthgame/Images/blocks/block_wall.png")));
                wallB.setTooltip(new Tooltip("Wall block. ID:1"));
                wallB.setToggleGroup(tg);
                wallB.setOnAction(event -> SELECTED_BLOCK = 1);
                wallB.setSelected(true);
                ToggleButton portalB = new ToggleButton();
                portalB.setGraphic(new ImageView(new Image("file://" + changeSlash(PATH) + ".labyrinthgame/Images/blocks/block_portal.png")));
                portalB.setTooltip(new Tooltip("Portal block. ID:4"));
                portalB.setToggleGroup(tg);
                portalB.setOnAction(event -> SELECTED_BLOCK = 4);
                db.getChildren().addAll(wallB, portalB);
                TitledPane defaultBlocks = new TitledPane("Default blocks", db);
                
                // Decoration blocks
                TilePane deb = new TilePane();
                ToggleButton voidB = new ToggleButton("VOID");
                voidB.setTooltip(new Tooltip("VOID block. ID:2"));
                voidB.setToggleGroup(tg);
                voidB.setOnAction(event -> SELECTED_BLOCK = 2);
                deb.getChildren().add(voidB);
                TitledPane decorationBlocks = new TitledPane("Decoration blocks", deb);
                
                // Damage blocks
                TilePane dab = new TilePane();
                dab.setHgap(5);
                dab.setVgap(2);
                ToggleButton spikeB = new ToggleButton();
                spikeB.setGraphic(new ImageView(new Image("file://" + changeSlash(PATH) + ".labyrinthgame/Images/blocks/block_spike.png")));
                spikeB.setTooltip(new Tooltip("Spike block. ID:3"));
                spikeB.setToggleGroup(tg);
                spikeB.setOnAction(event -> SELECTED_BLOCK = 3);
                ToggleButton shootB = new ToggleButton();
                shootB.setGraphic(new ImageView(new Image("file://" + changeSlash(PATH) + ".labyrinthgame/Images/blocks/block_shooter.png")));
                shootB.setTooltip(new Tooltip("Shooter block. ID:5"));
                shootB.setToggleGroup(tg);
                shootB.setOnAction(event -> SELECTED_BLOCK = 5);
                ToggleButton batB = new ToggleButton();
                batB.setGraphic(new ImageView(new Image("file://" + changeSlash(PATH) + ".labyrinthgame/Images/entities/bat_side_1.png")));
                batB.setTooltip(new Tooltip("Bat."));
                batB.setToggleGroup(tg);
                batB.setOnAction(event -> SELECTED_BLOCK = 6);
                dab.getChildren().addAll(spikeB, shootB, batB);
                TitledPane damageBlocks = new TitledPane("Damage blocks", dab);
                
                blockSelect.getPanes().addAll(defaultBlocks, decorationBlocks, damageBlocks);
                blockSelect.setExpandedPane(defaultBlocks);
                ScrollPane blockSelector = new ScrollPane(blockSelect);
		splitpane.getItems().add(blockSelector);

		// Set the divider on 80%
		splitpane.setDividerPositions(0.8f);
		this.stage.widthProperty().addListener((obs, oldVal, newVal) -> splitpane.setDividerPositions(0.8f));
		this.stage.heightProperty().addListener((obs, oldVal, newVal) -> splitpane.setDividerPositions(0.8f));

		layout.add(toolbar, 0, 0);
		layout.add(splitpane, 0, 1);

		this.stage.setScene(new Scene(layout, 800, 550));
	}
    
    /**
     * Show the window
    */
	public void start() {
		if (DONE && this.stage != null) {
			this.stage.show();
		}
	}

    /**
     * Update the file that contains last opened world
     * @param currentPath String to write into the file
    */
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
    
    /**
     * get last opened file path
     * @return the file path as a String
    */
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
    
    /**
     * Open a world file by opening a new tab containing the opened world. Will be created a copy of the world file in the cache folder. When the user saves the cache file (where the user edits the world) will be replaced with the original file.
     * @param f the world File
    */
	public void open(File f) {
		try {
			Random r = new Random();
			int number = r.nextInt();

			if (Arrays.asList(CURRENT_FILE_PATHS).contains(f.getAbsolutePath())) {
				this.tabs.getSelectionModel().select(Arrays.asList(CURRENT_FILE_PATHS).indexOf(f.getAbsolutePath()));
				return;
			}

			CURRENT_FILE_PATH = f.getAbsolutePath();
			WORKING_FILE_PATH = PATH + ".labyrinthgame" + File.separator + "Editor" + File.separator + "Cache" + File.separator + "cache[" + getFileName() + "]" + number + ".wld.ns"; // ns = not saved

			CURRENT_FILE_PATHS = Arrays.copyOf(CURRENT_FILE_PATHS, OPENED_TABS + 1);
			WORKING_FILE_PATHS = Arrays.copyOf(WORKING_FILE_PATHS, OPENED_TABS + 1);
			SAVES = Arrays.copyOf(SAVES, OPENED_TABS + 1);
			CURRENT_FILE_PATHS[OPENED_TABS] = CURRENT_FILE_PATH;
			WORKING_FILE_PATHS[OPENED_TABS] = WORKING_FILE_PATH;
			SAVES[OPENED_TABS] = true;
			OPENED_TABS++;

			Logger.info(Arrays.toString(CURRENT_FILE_PATHS) + " " + Arrays.toString(WORKING_FILE_PATHS));

			copyWorld(CURRENT_FILE_PATH, WORKING_FILE_PATH);
			if (this.tabs != null && getCurrentFilePath() != null) {
				Tab newTab = new Tab(f.getName());
				newTab.setClosable(false);
				newTab.setContent(getEditorTabContent());

				this.tabs.getTabs().add(newTab);
				this.tabs.getSelectionModel().select(newTab);
			} else {
				if (this.tabs != null) {
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
    
    /**
     * Check if it's possible to remove a column or a row.
    */
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
    
    /**
     * Check if it's possible to add a row/column. A row or a column can only be added if the world size is not bigger than @link MAX_WORLD_SIZE .
     * @return true or false
    */
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
    
    /**
     * When something is edited this method is called and the tab/window title is modified
    */
	private void unsaved() {
		this.saved = false;
		try {
			SAVES[this.tabs.getSelectionModel().getSelectedIndex()] = saved;
			this.tabs.getSelectionModel().getSelectedItem().setText(getFileName() + ((saved) ? "" : "*"));
		} catch (NullPointerException e) {
			SAVES[0] = saved;
		}
		this.stage.setTitle("LabyrinthGame - Editor (" + getFileName() + ((saved) ? "" : "*") + ")");
	}

    /**
     * When the user clicks the <pre>Save</pre> button, this method is called and updates the window and the tab title
    */
	private void saved() {
		this.saved = true;
		try {
			SAVES[this.tabs.getSelectionModel().getSelectedIndex()] = saved;
			this.tabs.getSelectionModel().getSelectedItem().setText(getFileName() + ((saved) ? "" : "*"));
		} catch (NullPointerException e) {
			SAVES[0] = saved;
		}
		this.stage.setTitle("LabyrinthGame - Editor (" + getFileName() + ((saved) ? "" : "*") + ")");
	}

    /**
     * This method creates a default world 2x2
    */
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

    /**
     * Creates a directory if it does not exists
     * @param path the directory path
    */
	private static void checkAndCreateDir(String path) {
		File f = new File(path);
		if (!f.exists()) {
			f.mkdir();
		}
	}

    /**
     * Creates the necessary application directories
    */
	public static void setupDirectory() {
		checkAndCreateDir(PATH + ".labyrinthgame");
		checkAndCreateDir(PATH + ".labyrinthgame" + File.separator + "SystemLevels");
		checkAndCreateDir(PATH + ".labyrinthgame" + File.separator + "Editor");
		checkAndCreateDir(PATH + ".labyrinthgame" + File.separator + "Editor" + File.separator + "Cache");
		checkAndCreateDir(PATH + ".labyrinthgame" + File.separator + "Editor" + File.separator + "Levels");
		checkAndCreateDir(PATH + ".labyrinthgame" + File.separator + "Images");
		checkAndCreateDir(PATH + ".labyrinthgame" + File.separator + "Images" + File.separator + "editor");
		checkAndCreateDir(PATH + ".labyrinthgame" + File.separator + "Images" + File.separator + "blocks");
		checkAndCreateDir(PATH + ".labyrinthgame" + File.separator + "Images" + File.separator + "entities");
	}

    /**
     * Copies a world to a given path
     * @param path1 the original world file path
     * @param path2 the destination path
    */
	private void copyWorld(String path1, String path2) {
		try {
			File second = new File(path2); // Delete second file if exists for replacement
			if (second.exists()) {
				second.delete();
			}
			Files.copy(new File(path1).toPath(), new File(path2).toPath());
			Logger.info("World copied from cache to file");
		} catch (IOException e) {
			Logger.warning("Unable to copy world from cache to file");
		}
	}
    
    /**
     * Get the current file path where the user is editing
     * @return the file path
    */
	private String getFileName() {
		Path path = Paths.get(CURRENT_FILE_PATH);
		Path fileName = path.getFileName();
		return fileName.toString();
	}
}
