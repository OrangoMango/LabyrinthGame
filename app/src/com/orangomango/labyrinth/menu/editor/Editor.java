package com.orangomango.labyrinth.menu.editor;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.geometry.*;
import javafx.event.EventHandler;
import javafx.scene.input.*;
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
import com.orangomango.labyrinth.engineering.*;

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
	private Label pointingOn;
	private String mode = "normal";
	private TilePane engp = new TilePane();
	private TilePane[] normalModePanes = new TilePane[3];
	private MenuItem[] engToDisable = new MenuItem[6];
	private Button[] engToDisableB = new Button[6];

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
				EngBlock engblock = null;
				if (mode.equals("engineering")){
					engblock = editableworld.getEngineeringWorld().getBlockAtCoord((int) event.getX(), (int) event.getY());
				}
				if (event.getButton() == MouseButton.SECONDARY && mode.equals("normal")){
					ContextMenu contextMenu = new ContextMenu();
					Menu item1 = new Menu("Set other portal end");
					MenuItem rmPoint = new MenuItem("Remove pointing");
					rmPoint.setOnAction(rmEvent -> {
						String[] data = editableworld.getBlockAtCoord((int)event.getX(), (int)event.getY()).getInfo().split("#")[1].split(" ");
						editableworld.getBlockAtCoord((int) event.getX(), (int) event.getY()).setInfo("NoPointSet");
						editableworld.getBlockAt(Integer.parseInt(data[0]), Integer.parseInt(data[1])).setInfo("NoPointSet");
						editableworld.updateOnFile();
						unsaved();
					});
					if (edblock.getInfo() != null){
					  rmPoint.setDisable(edblock.getInfo().equals("NoPointSet"));
					}
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
					MenuItem item2 = new MenuItem("Set bat data");
					item2.setOnAction(batEvent -> {
						Stage st = new Stage();
						st.setTitle("Bat preferences");
						GridPane pane = new GridPane();
						pane.setPadding(new Insets(10,10,10,10));
						pane.setHgap(20);
						pane.setVgap(20);
						Label l1 = new Label("Set path length: ");
						Spinner sp = new Spinner(2,NewWidget.MAX_WORLD_SIZE,2);
						Label l2 = new Label("Set direction:");
						ToggleGroup gr = new ToggleGroup();
						RadioButton b1 = new RadioButton("Vertical");
						RadioButton b2 = new RadioButton("Horizontal");
						b2.setSelected(true);
						b1.setToggleGroup(gr);
						b2.setToggleGroup(gr);
						Label l3 = new Label("Set speed (ms) [200]:");
						Slider slider = new Slider(80, 300, 200);
						slider.setPrefWidth(180);
						slider.setShowTickLabels(true);
						slider.setShowTickMarks(true);
						slider.setOnMouseDragged(evt -> l3.setText("Set speed (ms) ["+Math.round((int)slider.getValue())+"]"));
						CheckBox invert = new CheckBox("Invert movement");
						Button ok = new Button("Save changes");
						ok.setOnAction(ev -> {
							int pl = (int) sp.getValue();
							String dir = ((RadioButton)gr.getSelectedToggle()).getText().equals("Horizontal") ? "h" : "v";
							int speed = (int)slider.getValue();
							edblock.setInfo(String.format("data#%s %s %s %s", pl, dir, speed, invert.isSelected() ? "t" : "f"));
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
						pane.add(l3, 0, 4);
						pane.add(slider, 1, 4);
						pane.add(invert, 0, 5);
						pane.add(ok, 0, 6);
						pane.add(canc, 1, 6);
						Scene scene = new Scene(pane, 420, 300);
						scene.getStylesheets().add("file://" + changeSlash(PATH) + ".labyrinthgame/Editor/style.css");
						st.setScene(scene);
						st.show();
					});
					Menu item3 = new Menu("Rotate");
					MenuItem r = new MenuItem("Rotate 90deg right");
					r.setOnAction(rr -> {
						String d = Character.toString(edblock.getInfo().split("#")[1].charAt(0));
						switch (d) {
							case EditableWorld.NORTH:
								edblock.setInfo("direction#"+EditableWorld.EAST);
								break;
							case EditableWorld.EAST:
								edblock.setInfo("direction#"+EditableWorld.SOUTH);
								break;
							case EditableWorld.SOUTH:
								edblock.setInfo("direction#"+EditableWorld.WEST);
								break;
							case EditableWorld.WEST:
								edblock.setInfo("direction#"+EditableWorld.NORTH);
								break;
						}
						editableworld.setBlockOn(edblock);
						editableworld.updateOnFile();
						unsaved();
					});
					MenuItem l = new MenuItem("Rotate 90deg left");
					l.setOnAction(rl -> {
						String d = Character.toString(edblock.getInfo().split("#")[1].charAt(0));
						switch (d){
							case EditableWorld.NORTH:
								edblock.setInfo("direction#"+EditableWorld.WEST);
								break;
							case EditableWorld.EAST:
								edblock.setInfo("direction#"+EditableWorld.NORTH);
								break;
							case EditableWorld.SOUTH:
								edblock.setInfo("direction#"+EditableWorld.EAST);
								break;
							case EditableWorld.WEST:
								edblock.setInfo("direction#"+EditableWorld.SOUTH);
								break;
						}
						editableworld.setBlockOn(edblock);
						editableworld.updateOnFile();
						unsaved();
					});
					item3.getItems().addAll(r, l);
					MenuItem item4 = new MenuItem("Set movable block data");
					item4.setOnAction(clickEv -> {
						Stage st = new Stage();
						st.setTitle("Movable block preferences");
						GridPane pane = new GridPane();
						pane.setPadding(new Insets(10,10,10,10));
						pane.setHgap(20);
						pane.setVgap(20);
						Label l1 = new Label("Set path length: ");
						Spinner sp = new Spinner(2,NewWidget.MAX_WORLD_SIZE,2);
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
							String dir = ((RadioButton)gr.getSelectedToggle()).getText().equals("Horizontal") ? "h" : "v";
							edblock.setInfo(String.format("data#%s %s", pl, dir));
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
						Scene scene = new Scene(pane, 350, 250);
						scene.getStylesheets().add("file://" + changeSlash(PATH) + ".labyrinthgame/Editor/style.css");
						st.setScene(scene);
						st.show();
					});
					switch (edblock.getType()){
						case EditableWorld.PORTAL:
							contextMenu.getItems().add(item1);
							break;
						case EditableWorld.BAT_GEN:
							contextMenu.getItems().add(item2);
							break;
						case EditableWorld.SHOOTER:
							contextMenu.getItems().add(item3);
							break;
						case EditableWorld.ELEVATOR:
							contextMenu.getItems().add(item4);
							break;
					}
					contextMenu.show(canvas, event.getScreenX(), event.getScreenY());
				} else if (event.getButton() == MouseButton.PRIMARY && mode.equals("normal")){
					if (edblock.getType() == EditableWorld.AIR && (edblock.isOnStart(editableworld) || edblock.isOnEnd(editableworld))) {
						Logger.warning("Could not place block on start or end position");
						Alert alert = new Alert(Alert.AlertType.ERROR);
						alert.setHeaderText("Could not place block on start or end position");
						alert.setTitle("SSE Error");
						alert.setContentText(null);
						alert.showAndWait();
						return;
					}
					if (edblock.getType() == EditableWorld.PORTAL && !edblock.getInfo().equals("NoPointSet")){
						String[] data = editableworld.getBlockAtCoord((int)event.getX(), (int)event.getY()).getInfo().split("#")[1].split(" ");
						editableworld.getBlockAt(Integer.parseInt(data[0]), Integer.parseInt(data[1])).setInfo("NoPointSet");
					}
					// Check clone on EditableWorld class
					if (edblock.getType() == EditableWorld.WALL){
					    if (editableworld.getBlockAt(edblock.getX(), edblock.getY()-1) != null){
						if (editableworld.getBlockAt(edblock.getX(), edblock.getY()-1).getType().equals(EditableWorld.WALL)){
						    editableworld.getBlockAt(edblock.getX(), edblock.getY()-1).removeConn("s");
						}
					    }
					    if (editableworld.getBlockAt(edblock.getX()+1, edblock.getY()) != null){
						if (editableworld.getBlockAt(edblock.getX()+1, edblock.getY()).getType().equals(EditableWorld.WALL)){
						    editableworld.getBlockAt(edblock.getX()+1, edblock.getY()).removeConn("w");
						}
					    }
					    if (editableworld.getBlockAt(edblock.getX(), edblock.getY()+1) != null){
						if (editableworld.getBlockAt(edblock.getX(), edblock.getY()+1).getType().equals(EditableWorld.WALL)){
						    editableworld.getBlockAt(edblock.getX(), edblock.getY()+1).removeConn("n");
						}
					    }
					    if (editableworld.getBlockAt(edblock.getX()-1, edblock.getY()) != null){
						if (editableworld.getBlockAt(edblock.getX()-1, edblock.getY()).getType().equals(EditableWorld.WALL)){
						    editableworld.getBlockAt(edblock.getX()-1, edblock.getY()).removeConn("e");
						}
					    }
					    editableworld.updateOnFile();
					}
					switch (SELECTED_BLOCK) {
						case 1:
							if (edblock.getType().equals(EditableWorld.AIR)){
							    StringBuilder b = new StringBuilder();
							    int c = 0;
							    if (editableworld.getBlockAt(edblock.getX(), edblock.getY()-1) != null){
								    if (editableworld.getBlockAt(edblock.getX(), edblock.getY()-1).getType().equals(EditableWorld.WALL)){
									    b.append("n");
									    editableworld.getBlockAt(edblock.getX(), edblock.getY()-1).addConn("s");
								    } else {
									    c++;
								    }
							    } else {
								    c++;
							    }
							    if (editableworld.getBlockAt(edblock.getX()+1, edblock.getY()) != null){
								    if (editableworld.getBlockAt(edblock.getX()+1, edblock.getY()).getType().equals(EditableWorld.WALL)){
									    b.append("e");
									    editableworld.getBlockAt(edblock.getX()+1, edblock.getY()).addConn("w");
								    } else {
									    c++;
								    }
							    } else {
								    c++;
							    }
							    if (editableworld.getBlockAt(edblock.getX(), edblock.getY()+1) != null){
								    if (editableworld.getBlockAt(edblock.getX(), edblock.getY()+1).getType().equals(EditableWorld.WALL)){
									    b.append("s");
									    editableworld.getBlockAt(edblock.getX(), edblock.getY()+1).addConn("n");
								    } else {
									    c++;
								    }
							    } else {
								    c++;
							    }
							    if (editableworld.getBlockAt(edblock.getX()-1, edblock.getY()) != null){
								    if (editableworld.getBlockAt(edblock.getX()-1, edblock.getY()).getType().equals(EditableWorld.WALL)){
									    b.append("w");
									    editableworld.getBlockAt(edblock.getX()-1, edblock.getY()).addConn("e");
								    } else {
									    c++;
								    }
							    } else {
								    c++;
							    }
							    if (c != 4){
								edblock.setInfo("conn#"+b.toString());
							    } else {
								    edblock.setInfo("conn#null");
							    }
							}
							
							edblock.toggleType(EditableWorld.WALL);
							break;
						case 2:
							edblock.setInfo(null);
							edblock.toggleType(EditableWorld.VOID);
							break;
						case 3:
							edblock.setInfo(null);
							edblock.toggleType(EditableWorld.SPIKE);
							break;
						case 4:
							edblock.setInfo("NoPointSet");
							edblock.toggleType(EditableWorld.PORTAL);
							break;
						case 5:
							edblock.setInfo("direction#"+EditableWorld.WEST);
							edblock.toggleType(EditableWorld.SHOOTER);
							break;
						case 6:
							edblock.setInfo("NoDataSet");
							edblock.toggleType(EditableWorld.BAT_GEN);
							break;
						case 7:
							edblock.setInfo("NoDataSet");
							edblock.toggleType(EditableWorld.ELEVATOR);
							break;
						case 8:
							edblock.setInfo(null);
							edblock.toggleType(EditableWorld.C_SPIKE);
							break;
						case 9:
							edblock.setInfo(null);
							edblock.toggleType(EditableWorld.D_WARNING);
							break;
					}
					
					editableworld.setBlockOn(edblock);
					editableworld.updateOnFile();
					
					unsaved();
				} else if (event.getButton() == MouseButton.PRIMARY && mode.equals("engineering")){
					switch (SELECTED_BLOCK){
						case 1:
							engblock.setInfo(null);
							engblock.toggleType(EngBlock.CABLE);
							break;
						case 2:
							engblock.setInfo(null);
							engblock.toggleType(EngBlock.LEVER);
							break;
						case 3:
							engblock.setInfo(null);
							engblock.toggleType(EngBlock.GENERATOR);
							break;
						case 5:
							engblock.setInfo(null);
							engblock.toggleType(EngBlock.DOOR);
							break;
					}
					
					editableworld.getEngineeringWorld().setBlockOn(engblock);
					editableworld.updateOnFile();
				}
			}
		});

		scrollpane.setContent(canvas);

		GraphicsContext pen = canvas.getGraphicsContext2D();
		editableworld.setPen(pen);
		editableworld.setPlayer(new Player(editableworld.start[0], editableworld.start[1], editableworld));
		editableworld.setCanvas(canvas);
		editableworld.draw();
		
		canvas.setOnMouseMoved(event -> {
			Block block = edworld.getBlockAtCoord((int)event.getX(), (int)event.getY());
			EngBlock Eblock;
			if (edworld.getEngineeringWorld() != null){
				Eblock = edworld.getEngineeringWorld().getBlockAtCoord((int)event.getX(), (int)event.getY());
			} else {
				Eblock = null;
			}
			if (this.mode.equals("normal")){
				this.pointingOn.setText("Mouse on block: "+block+" | "+((block.isOnStart(edworld)) ? "On start position" : ((block.isOnEnd(edworld)) ? "On end position" : "Not on start or end position"))+" ["+getFileName()+"]");
			} else if (this.mode.equals("engineering")){
				this.pointingOn.setText("Mouse on block: "+Eblock+" ["+getFileName()+"]");
			}
					
		});

		scrollpane.setPrefSize(this.stage.getWidth(), this.stage.getHeight());

		this.stage.widthProperty().addListener((obs, oldVal, newVal) -> scrollpane.setPrefSize((double) newVal, this.stage.getHeight()));
		this.stage.heightProperty().addListener((obs, oldVal, newVal) -> scrollpane.setPrefSize(this.stage.getWidth(), (double) newVal));

		layout.add(scrollpane, 0, 0);
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

		// Setup the menu
		MenuBar menuBar = new MenuBar();
		
		Menu fileMenu = new Menu("File");
		MenuItem mNew = new MenuItem("New");
		mNew.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN));
		mNew.setOnAction(e -> {
		    // Clone of toolbar button
		    NewWidget wid = new NewWidget(false);
		    wid.setEDW(edworld);
		    wid.setEditor(this);
		});
		MenuItem mSave = new MenuItem("Save");
		mSave.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
		mSave.setOnAction(e -> {
		    try {
				// Clone of toolbar button
				saved();
				copyWorld(WORKING_FILE_PATH, CURRENT_FILE_PATH);
				Alert alert = new Alert(Alert.AlertType.INFORMATION);
				alert.setHeaderText("File saved successfully");
				alert.setTitle("File saved");
				alert.setContentText("File saved successfully.");
				alert.showAndWait();
			} catch (Exception ex) {
				Alert alert = new Alert(Alert.AlertType.ERROR);
				alert.setHeaderText("Error while parsing file");
				alert.setTitle("Error");
				alert.setContentText("Could not save world file!");
				alert.showAndWait();
			}
		});
		MenuItem mOpen = new MenuItem("Open");
		mOpen.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN));
		mOpen.setOnAction(e -> {
		    try {
				// Clone of toolbar button
				FileChooser chooser = new FileChooser();
				chooser.setInitialDirectory(new File(PATH + ".labyrinthgame" + File.separator + "Editor" + File.separator + "Levels" + File.separator));
				chooser.setTitle("Open world");
				chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("World file", "*.wld"));
				File f = chooser.showOpenDialog(this.stage);
				if (f.equals(null)) {
					throw new Exception("Null file opened");
				}
				open(f);
			} catch (Exception ex) {
				Alert alert = new Alert(Alert.AlertType.ERROR);
				alert.setHeaderText("Error while parsing file");
				alert.setTitle("Error");
				alert.setContentText("Could not open world file!");
				alert.showAndWait();
			}
		});
		fileMenu.getItems().addAll(mNew, mSave, mOpen);
		
		Menu editMenu = new Menu("Edit");
		MenuItem mAR = new MenuItem("Add row");
		mAR.setAccelerator(new KeyCodeCombination(KeyCode.R, KeyCombination.ALT_DOWN));
		mAR.setOnAction(e -> {
			// Clone of toolbar button
			if (checkValidityMax("h")) {
				edworld.addRow();
				unsaved();
			}
		});
		engToDisable[0] = mAR;
		MenuItem mAC = new MenuItem("Add column");
		mAC.setAccelerator(new KeyCodeCombination(KeyCode.C, KeyCombination.ALT_DOWN));
		mAC.setOnAction(e -> {
			// Clone of toolbar button
			if (checkValidityMax("w")) {
				edworld.addColumn();
				unsaved();
			}
		});
		engToDisable[1] = mAC;
		MenuItem mRR = new MenuItem("Remove row");
		mRR.setAccelerator(new KeyCodeCombination(KeyCode.R, KeyCombination.SHIFT_DOWN, KeyCombination.ALT_DOWN));
		mRR.setOnAction(e -> {
			// Clone of toolbar button
			checkValidity(edworld.removeRow());
			unsaved();
		});
		engToDisable[2] = mRR;
		MenuItem mRC = new MenuItem("Remove column");
		mRC.setAccelerator(new KeyCodeCombination(KeyCode.C, KeyCombination.SHIFT_DOWN, KeyCombination.ALT_DOWN));
		mRC.setOnAction(e -> {
			// Clone of toolbar button
			checkValidity(edworld.removeColumn());
			unsaved();
		});
		engToDisable[3] = mRC;
		MenuItem mSSE = new MenuItem("Change start/end position");
		mSSE.setAccelerator(new KeyCodeCombination(KeyCode.E, KeyCombination.CONTROL_DOWN));
		mSSE.setOnAction(e -> {
			// Clone of toolbar button
			new SESetup(edworld, edworld.width, edworld.height, edworld.start, edworld.end);
			unsaved();
		});
		engToDisable[4] = mSSE;
		MenuItem mRun = new MenuItem("Run current level");
		mRun.setAccelerator(new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN));
		mRun.setOnAction(e -> {
			// Clone of toolbar button
			new LevelExe(CURRENT_FILE_PATH, getFileName(), saved);
			LevelExe.setOnFinish(null);
		});
		engToDisable[5] = mRun;
		MenuItem mUndo = new MenuItem("Undo");
		mUndo.setDisable(true);
		MenuItem mRedo = new MenuItem("Redo");
		mRedo.setDisable(true);
		editMenu.getItems().addAll(mAR, mAC, mRR, mRC, new SeparatorMenuItem(), mSSE, new SeparatorMenuItem(), mRun, new SeparatorMenuItem(), mUndo, mRedo);
		
		Menu modeMenu = new Menu("Mode");
		ToggleGroup modeGroup = new ToggleGroup();
		RadioMenuItem mNormal = new RadioMenuItem("Normal mode");
		mNormal.setAccelerator(new KeyCodeCombination(KeyCode.M, KeyCombination.CONTROL_DOWN));
		mNormal.setOnAction(e -> setMode("normal"));
		mNormal.setSelected(true);
		mNormal.setToggleGroup(modeGroup);
		RadioMenuItem mEngineer = new RadioMenuItem("Engineer mode");
		mEngineer.setAccelerator(new KeyCodeCombination(KeyCode.G, KeyCombination.CONTROL_DOWN));
		mEngineer.setOnAction(e -> setMode("engineering"));
		mEngineer.setToggleGroup(modeGroup);
		modeMenu.getItems().addAll(mNormal, mEngineer);
		
		menuBar.getMenus().addAll(fileMenu, editMenu, modeMenu);

		// Setup the toolbar
		ToolBar toolbar = new ToolBar();
		toolbar.setOrientation(Orientation.HORIZONTAL);

		createNewWorld("testSystemWorld-DefaultName_NoCopy");
		edworld = new EditableWorld(PATH + ".labyrinthgame" + File.separator + "Editor" + File.separator + "Levels" + File.separator + "testSystemWorld-DefaultName_NoCopy.wld.sys");
		Button newBtn = new Button("New");
		newBtn.setGraphic(new ImageView(new Image("file://" + changeSlash(PATH) + ".labyrinthgame/Images/editor/new.png")));
                newBtn.setTooltip(new Tooltip("Create a new world file"));
		newBtn.setOnAction(event -> {
			// Clone of menu button
			NewWidget wid = new NewWidget(false);
			wid.setEDW(edworld);
			wid.setEditor(this);
		});
		Button saveBtn = new Button("Save");
		saveBtn.setGraphic(new ImageView(new Image("file://" + changeSlash(PATH) + ".labyrinthgame/Images/editor/save.png")));
                saveBtn.setTooltip(new Tooltip("Save file"));
		saveBtn.setOnAction(event -> {
			try {
				// Clone of menu button
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
				// Clone of menu button
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
		engToDisableB[0] = addCBtn;
                addCBtn.setTooltip(new Tooltip("Add column to world"));
		addCBtn.setGraphic(new ImageView(new Image("file://" + changeSlash(PATH) + ".labyrinthgame/Images/editor/ac.png")));
		addCBtn.setOnAction(event -> {
			// Clone of menu button
			if (checkValidityMax("w")) {
				edworld.addColumn();
				unsaved();
			}
		});
		Button addRBtn = new Button();
		engToDisableB[1] = addRBtn;
		addRBtn.setTooltip(new Tooltip("Add row to world"));
		addRBtn.setGraphic(new ImageView(new Image("file://" + changeSlash(PATH) + ".labyrinthgame/Images/editor/ar.png")));
		addRBtn.setOnAction(event -> {
			// Clone of menu button
			if (checkValidityMax("h")) {
				edworld.addRow();
				unsaved();
			}
		});
		Button rmCBtn = new Button();
		engToDisableB[2] = rmCBtn;
		rmCBtn.setTooltip(new Tooltip("Remove column from world"));
		rmCBtn.setGraphic(new ImageView(new Image("file://" + changeSlash(PATH) + ".labyrinthgame/Images/editor/rc.png")));
		rmCBtn.setOnAction(event -> {
			// Clone of menu button
			checkValidity(edworld.removeColumn());
			unsaved();
		});
		Button rmRBtn = new Button();
		engToDisableB[3] = rmRBtn;
		rmRBtn.setTooltip(new Tooltip("Remove row from world"));
		rmRBtn.setGraphic(new ImageView(new Image("file://" + changeSlash(PATH) + ".labyrinthgame/Images/editor/rr.png")));
		rmRBtn.setOnAction(event -> {
			// Clone of menu button
			checkValidity(edworld.removeRow());
			unsaved();
		});

		Button runBtn = new Button("Run");
		engToDisableB[4] = runBtn;
		runBtn.setTooltip(new Tooltip("Run current level"));
		runBtn.setGraphic(new ImageView(new Image("file://" + changeSlash(PATH) + ".labyrinthgame/Images/editor/run.png")));
		runBtn.setOnAction(event -> {
			// Clone of menu button
			new LevelExe(CURRENT_FILE_PATH, getFileName(), saved);
			LevelExe.setOnFinish(null);
		});

		Button sseBtn = new Button();
		engToDisableB[5] = sseBtn;
		sseBtn.setTooltip(new Tooltip("Change start and end position"));
		sseBtn.setGraphic(new ImageView(new Image("file://" + changeSlash(PATH) + ".labyrinthgame/Images/editor/sse.png")));
		sseBtn.setOnAction(event -> {new SESetup(edworld, edworld.width, edworld.height, edworld.start, edworld.end); unsaved();});

		toolbar.getItems().addAll(newBtn, saveBtn, openBtn, new Separator(), addCBtn, addRBtn, rmCBtn, rmRBtn, new Separator(), sseBtn, new Separator(), runBtn);

		// Setup world editor
		ScrollPane scrollpane = new ScrollPane();
		this.stage.widthProperty().addListener((obs, oldVal, newVal) -> scrollpane.setPrefSize((double) newVal, this.stage.getHeight()));
		this.stage.heightProperty().addListener((obs, oldVal, newVal) -> scrollpane.setPrefSize(this.stage.getWidth(), (double) newVal));

		this.pointingOn = new Label("Mouse on Block: null");

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

		Pagination pages = new Pagination();
		pages.setPageCount(4);
		pages.setCurrentPageIndex(0);
		pages.setMaxPageIndicatorCount(4);
		
		ToggleGroup tg = new ToggleGroup();
		
		pages.setPageFactory((pageIndex) -> {
			
			String style = "-fx-font-weight: bold;\n-fx-font-family: \"Courier New\";\n-fx-font-size: 14;";
			
			switch (pageIndex){
				case 0:
					TilePane db = new TilePane();
					normalModePanes[0] = db;
					db.setDisable(this.mode.equals("normal") ? false : true);
					db.setPadding(new Insets(5, 5, 5, 5));
					db.setHgap(5);
					db.setVgap(5);
					ToggleButton wallB = new ToggleButton();
					wallB.setGraphic(new ImageView(new Image("file://" + changeSlash(PATH) + ".labyrinthgame/Images/blocks/block_wall-nesw.png")));
					wallB.setTooltip(new Tooltip("Wall block. ID:N1"));
					wallB.setToggleGroup(tg);
					wallB.setOnAction(event -> SELECTED_BLOCK = 1);
					wallB.setSelected(true);
					ToggleButton portalB = new ToggleButton();
					portalB.setGraphic(new ImageView(new Image("file://" + changeSlash(PATH) + ".labyrinthgame/Images/blocks/block_portal.png")));
					portalB.setTooltip(new Tooltip("Portal block. ID:N4"));
					portalB.setToggleGroup(tg);
					portalB.setOnAction(event -> SELECTED_BLOCK = 4);
					ToggleButton moveB = new ToggleButton();
					moveB.setGraphic(new ImageView(new Image("file://" + changeSlash(PATH) + ".labyrinthgame/Images/entities/move_block.png")));
					moveB.setTooltip(new Tooltip("Elevator block. ID:N7"));
					moveB.setToggleGroup(tg);
					moveB.setOnAction(event -> SELECTED_BLOCK = 7);
					db.getChildren().addAll(wallB, portalB, moveB);
					Label header = new Label(" Default Blocks");
					header.setStyle(style);
					return new VBox(header, db);
				case 1:
					TilePane deb = new TilePane();
					deb.setDisable(this.mode.equals("normal") ? false : true);
					normalModePanes[1] = deb;
					deb.setPadding(new Insets(5, 5, 5, 5));
					deb.setHgap(5);
					deb.setVgap(5);
					ToggleButton voidB = new ToggleButton("VOID");
					voidB.setTooltip(new Tooltip("VOID block. ID:N2"));
					voidB.setToggleGroup(tg);
					voidB.setOnAction(event -> SELECTED_BLOCK = 2);
					ToggleButton warB = new ToggleButton();
					warB.setGraphic(new ImageView(new Image("file://" + changeSlash(PATH) + ".labyrinthgame/Images/blocks/decoration_warning.png")));
					warB.setTooltip(new Tooltip("Warning decoration. ID:N9"));
					warB.setToggleGroup(tg);
					warB.setOnAction(event -> SELECTED_BLOCK = 9);
					deb.getChildren().addAll(voidB, warB);
					Label header1 = new Label(" Decoration Blocks");
					header1.setStyle(style);
					return new VBox(header1, deb);
				case 2:
					TilePane dab = new TilePane();
					dab.setDisable(this.mode.equals("normal") ? false : true);
					normalModePanes[2] = dab;
					dab.setPadding(new Insets(5, 5, 5, 5));
					dab.setHgap(5);
					dab.setVgap(5);
					ToggleButton spikeB = new ToggleButton();
					spikeB.setGraphic(new ImageView(new Image("file://" + changeSlash(PATH) + ".labyrinthgame/Images/blocks/block_spike.png")));
					spikeB.setTooltip(new Tooltip("Spike block. ID:N3"));
					spikeB.setToggleGroup(tg);
					spikeB.setOnAction(event -> SELECTED_BLOCK = 3);
					ToggleButton shootB = new ToggleButton();
					shootB.setGraphic(new ImageView(new Image("file://" + changeSlash(PATH) + ".labyrinthgame/Images/blocks/block_shooter_h.png")));
					shootB.setTooltip(new Tooltip("Shooter block. ID:N5"));
					shootB.setToggleGroup(tg);
					shootB.setOnAction(event -> SELECTED_BLOCK = 5);
					ToggleButton batB = new ToggleButton();
					batB.setGraphic(new ImageView(new Image("file://" + changeSlash(PATH) + ".labyrinthgame/Images/entities/bat_side_1.png")));
					batB.setTooltip(new Tooltip("Bat. ID:N6"));
					batB.setToggleGroup(tg);
					batB.setOnAction(event -> SELECTED_BLOCK = 6);
					ToggleButton cspikeB = new ToggleButton();
					cspikeB.setGraphic(new ImageView(new Image("file://" + changeSlash(PATH) + ".labyrinthgame/Images/blocks/block_spike_closed.png")));
					cspikeB.setTooltip(new Tooltip("Closable spike. ID:N8"));
					cspikeB.setToggleGroup(tg);
					cspikeB.setOnAction(event -> SELECTED_BLOCK = 8);
					dab.getChildren().addAll(spikeB, shootB, batB, cspikeB);
					Label header2 = new Label(" Damage Blocks");
					header2.setStyle(style);
					return new VBox(header2, dab);
				case 3:
					engp = new TilePane();
					engp.setPadding(new Insets(5, 5, 5, 5));
					engp.setHgap(5);
					engp.setVgap(5);
					ToggleButton cableB = new ToggleButton();
					cableB.setGraphic(new ImageView(new Image("file://" + changeSlash(PATH) + ".labyrinthgame/Images/engineering/blocks/cable-es.png")));
					cableB.setTooltip(new Tooltip("Cable block. ID:E1"));
					cableB.setToggleGroup(tg);
					cableB.setOnAction(event -> SELECTED_BLOCK = 1);
					ToggleButton leverB = new ToggleButton();
					leverB.setGraphic(new ImageView(new Image("file://" + changeSlash(PATH) + ".labyrinthgame/Images/engineering/blocks/lever_on.png")));
					leverB.setTooltip(new Tooltip("Lever block. ID:E2"));
					leverB.setToggleGroup(tg);
					leverB.setOnAction(event -> SELECTED_BLOCK = 2);
					ToggleButton generatorB = new ToggleButton();
					generatorB.setGraphic(new ImageView(new Image("file://" + changeSlash(PATH) + ".labyrinthgame/Images/engineering/blocks/generator_5.png")));
					generatorB.setTooltip(new Tooltip("Generator block. ID:E3"));
					generatorB.setToggleGroup(tg);
					generatorB.setOnAction(event -> SELECTED_BLOCK = 3);
					ToggleButton doorB = new ToggleButton();
					doorB.setGraphic(new ImageView(new Image("file://" + changeSlash(PATH) + ".labyrinthgame/Images/engineering/blocks/door_3.png")));
					doorB.setTooltip(new Tooltip("Door block. ID:E5"));
					doorB.setToggleGroup(tg);
					doorB.setOnAction(event -> SELECTED_BLOCK = 5);
					engp.setDisable(this.mode.equals("normal") ? true : false);
					engp.getChildren().addAll(cableB, leverB, generatorB, doorB);
					Label header3 = new Label(" Engineering Blocks");
					header3.setStyle(style);
					return new VBox(header3, engp);
				default:
					return new TilePane(new Label("Page: "+(pageIndex+1)));
			}
		});

		splitpane.getItems().add(pages);

		// Set the divider on 80%
		splitpane.setDividerPositions(0.8f);
		this.stage.widthProperty().addListener((obs, oldVal, newVal) -> splitpane.setDividerPositions(0.8f));
		this.stage.heightProperty().addListener((obs, oldVal, newVal) -> splitpane.setDividerPositions(0.8f));
		splitpane.getDividers().get(0).positionProperty().addListener((ob, ov, nv) -> splitpane.setDividerPositions(0.8f));

		layout.add(menuBar, 0, 0);
		layout.add(toolbar, 0, 1);
		layout.add(splitpane, 0, 2);
		layout.add(pointingOn, 0, 3, 2, 1);

		Scene scene = new Scene(layout, 850, 550);
		scene.getStylesheets().add("file://" + changeSlash(PATH) + ".labyrinthgame/Editor/style.css");
		this.stage.setScene(scene);
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
			writer.write("0,0,0,0\n");
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
		checkAndCreateDir(PATH + ".labyrinthgame" + File.separator + "Images" + File.separator + "engineering");
		checkAndCreateDir(PATH + ".labyrinthgame" + File.separator + "Images" + File.separator + "engineering" + File.separator + "blocks");
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
	
	private void setMode(String m){
		this.mode = m;
		if (this.mode.equals("engineering")){
			if (this.edworld.getEngineeringWorld() == null){
				this.edworld.setEngineeringWorld(EngWorld.createNewEngWorld(this.edworld.width, this.edworld.height));
				this.edworld.updateOnFile();
				unsaved();
				System.out.println("Engineering mode created successfully");
			}
			engp.setDisable(false);
			for (TilePane t: normalModePanes){
				if (t != null){
					t.setDisable(true);
				}
			}
			for (MenuItem n: engToDisable){
				if (n != null){
					n.setDisable(true);
				}
			}
			for (Button b: engToDisableB){
				if (b != null){
					b.setDisable(true);
				}
			}
			SELECTED_BLOCK = 3;
			this.edworld.setDrawingMode("engineering");
			this.edworld.update(0, 0, 0, 0);
		} else if (this.mode.equals("normal")){
			engp.setDisable(true);
			for (TilePane t: normalModePanes){
				if (t != null){
					t.setDisable(false);
				}
			}
			for (MenuItem n: engToDisable){
				if (n != null){
					n.setDisable(false);
				}
			}
			for (Button b: engToDisableB){
				if (b != null){
					b.setDisable(false);
				}
			}
			SELECTED_BLOCK = 1;
			this.edworld.setDrawingMode("normal");
			this.edworld.update(0, 0, 0, 0);
		}
	}
}
