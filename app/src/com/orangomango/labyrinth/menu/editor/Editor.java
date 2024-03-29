package com.orangomango.labyrinth.menu.editor;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.geometry.*;
import javafx.event.EventHandler;
import javafx.event.ActionEvent;
import javafx.scene.input.*;
import javafx.scene.canvas.*;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.animation.*;
import javafx.util.Duration;
import javafx.stage.FileChooser;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.Random;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

import com.orangomango.labyrinth.Player;
import com.orangomango.labyrinth.Block;
import com.orangomango.labyrinth.World;
import static com.orangomango.labyrinth.World.WorldList;
import static com.orangomango.labyrinth.World.getArcadeLevels;
import com.orangomango.labyrinth.menu.createdlevels.CreatedWorldFiles;
import com.orangomango.labyrinth.command.Command;
import com.orangomango.labyrinth.Logger;
import com.orangomango.labyrinth.engineering.*;

public class Editor {
	public Stage stage;
	private EditableWorld edworld;
	public final static String PATH = System.getProperty("user.home") + File.separator;
	private static String WORKING_FILE_PATH = "";
	private static String CURRENT_FILE_PATH = "";
	private boolean saved = true;
	public static CreatedWorldFiles worldList;
	private static int SELECTED_BLOCK = 1;
	private static int SELECTED_PATTERN = 1;
	private TabPane tabs;
	private Label pointingOn;
	private boolean arcade = false;
	private MenuItem mArcade;
	private Tab worldsTab, personalViewTab;
	private RadioMenuItem mNormal, mEngineer;
	private Button runArcBtn;
	private MenuItem mRunPattern, mUndo, mRedo;
	private CheckMenuItem mLights;
	private TabPane blocksTabPane;
	private Timeline workLoop;
	private static List<String> workingList = new ArrayList<>();
	private int HISTORY_POINT = 0;

	// Temp variables used to store info
	private String dirSelection;
	private EngBlock currentSelectedEngBlock;
	private EditableWorld referenceWorld;

	private String mode = "normal";
	private VBox vbf = new VBox();
	private TilePane[] normalModePanes = new TilePane[3];
	private MenuItem engToDisable = new MenuItem();
	private Button engToDisableB = new Button();

	private static List<String> WORKING_FILE_PATHS = new ArrayList<String>();
	private static List<String> CURRENT_FILE_PATHS = new ArrayList<String>();
	private static List<List<String>> WORKING_LISTS = new ArrayList<List<String>>();
	
	private static int OPENED_TABS = 0;
	private static boolean[] SAVES = new boolean[0];
	private static List<EditableWorld> WORLDS = new ArrayList<EditableWorld>();

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
		System.out.println("Edoworld: "+(editableworld == null));
		editableworld.warningOnEnd = CURRENT_FILE_PATH.endsWith(".arc") || CURRENT_FILE_PATH.endsWith(".arc.sys");

		Canvas canvas = new Canvas(editableworld.width * EditableWorld.BLOCK_WIDTH, editableworld.height * EditableWorld.BLOCK_WIDTH);
		canvas.setFocusTraversable(true);

		canvas.setOnMousePressed(new EventHandler<MouseEvent> () {
			@Override
			public void handle(MouseEvent event) {
				EditableBlock edblock = EditableBlock.fromBlock(editableworld.getBlockAtCoord((int) event.getX(), (int) event.getY()));
				EngBlock engblock = null;
				if (mode.equals("engineering")) {
					engblock = editableworld.getEngineeringWorld().getBlockAtCoord((int) event.getX(), (int) event.getY());
				}
				if (event.getButton() == MouseButton.SECONDARY) {
					ContextMenu contextMenu = new ContextMenu();
					if (mode.equals("normal")) {
						Menu item1 = new Menu("Set other portal end");
						MenuItem rmPoint = new MenuItem("Remove pointing");
						rmPoint.setOnAction(rmEvent -> {
							String[] data = editableworld.getBlockAtCoord((int) event.getX(), (int) event.getY()).getInfo().split("#")[1].split(" ");
							editableworld.getBlockAtCoord((int) event.getX(), (int) event.getY()).addInfoParam("point#NoPointSet");
							editableworld.getBlockAt(Integer.parseInt(data[0]), Integer.parseInt(data[1])).addInfoParam("point#NoPointSet");
							editableworld.updateOnFile();
							unsaved();
						});
						if (edblock.getInfo() != null) {
							if (edblock.checkInfoKey("point") > 0)
								rmPoint.setDisable(edblock.getInfo().split(";")[edblock.checkInfoKey("point")].split("#")[1].equals("NoPointSet"));
						}
						item1.getItems().add(rmPoint);
						for (int y = 0; y<editableworld.height; y++) {
							for (int x = 0; x<editableworld.width; x++) {
								Block b = editableworld.getBlockAt(x, y);
								if (b.getType() == EditableWorld.PORTAL) {
									MenuItem menuitem = new MenuItem(b.toString());
									menuitem.setOnAction(itemEvent -> {
										if (!edblock.getInfo().split(";")[edblock.checkInfoKey("point")].split("#")[1].equals("NoPointSet")) {
											String[] data = edblock.getInfo().split(";")[edblock.checkInfoKey("point")].split("#")[1].split(" ");
											editableworld.getBlockAt(Integer.parseInt(data[0]), Integer.parseInt(data[1])).setInfo("NoPointSet");
										}
										if (!b.getInfo().split(";")[b.checkInfoKey("point")].split("#")[1].equals("NoPointSet")) {
											String[] data = b.getInfo().split(";")[b.checkInfoKey("point")].split("#")[1].split(" ");
											editableworld.getBlockAt(Integer.parseInt(data[0]), Integer.parseInt(data[1])).addInfoParam("point#NoPointSet");
										}
										editableworld.getBlockAtCoord((int) event.getX(), (int) event.getY()).addInfoParam(String.format("point#%s %s", b.getX(), b.getY()));
										b.addInfoParam(String.format("point#%s %s", edblock.getX(), edblock.getY()));
										editableworld.updateOnFile();
										unsaved();
									});
									if (b.getX() == edblock.getX() && b.getY() == edblock.getY() && b.getX() == edblock.getX()) {
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
							pane.setPadding(new Insets(10, 10, 10, 10));
							pane.setHgap(20);
							pane.setVgap(20);
							Label l1 = new Label("Set path length: ");
							Spinner sp = new Spinner(2, NewWidget.MAX_WORLD_SIZE, 2);
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
							slider.setOnMouseDragged(evt -> l3.setText("Set speed (ms) [" + Math.round((int) slider.getValue()) + "]:"));
							Label l4 = new Label("Set damage [30]:");
							Slider slider2 = new Slider(1, 100, 30);
							slider2.setPrefWidth(180);
							slider2.setShowTickLabels(true);
							slider2.setShowTickMarks(true);
							slider2.setOnMouseDragged(evt -> l4.setText("Set damage [" + Math.round((int) slider2.getValue()) + "]:"));
							CheckBox invert = new CheckBox("Invert movement");
							Button ok = new Button("Save changes");
							ok.setOnAction(ev -> {
								int pl = (int) sp.getValue();
								String dir = ((RadioButton) gr.getSelectedToggle()).getText().equals("Horizontal") ? "h" : "v";
								int speed = (int) slider.getValue();
								int damage = (int) slider2.getValue();
								edblock.addInfoParam(String.format("data#%s %s %s %s %s", pl, dir, speed, invert.isSelected() ? "t" : "f", damage));
								editableworld.setBlockOn(edblock);
								editableworld.updateOnFile();
								unsaved();
								st.close();
							});
							Button canc = new Button("Cancel");
							canc.setOnAction(e -> st.close());
							pane.add(l1, 0, 0);
							pane.add(sp, 1, 0);
							pane.add(l2, 0, 1);
							pane.add(b1, 0, 2);
							pane.add(b2, 0, 3);
							pane.add(l3, 0, 4);
							pane.add(slider, 1, 4);
							pane.add(l4, 0, 5);
							pane.add(slider2, 1, 5);
							pane.add(invert, 0, 6);
							pane.add(ok, 0, 7);
							pane.add(canc, 1, 7);
							Scene scene = new Scene(pane, 420, 370);
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
									edblock.addInfoParam("direction#" + EditableWorld.EAST);
									break;
								case EditableWorld.EAST:
									edblock.addInfoParam("direction#" + EditableWorld.SOUTH);
									break;
								case EditableWorld.SOUTH:
									edblock.addInfoParam("direction#" + EditableWorld.WEST);
									break;
								case EditableWorld.WEST:
									edblock.addInfoParam("direction#" + EditableWorld.NORTH);
									break;
							}
							editableworld.setBlockOn(edblock);
							editableworld.updateOnFile();
							unsaved();
						});
						MenuItem l = new MenuItem("Rotate 90deg left");
						l.setOnAction(rl -> {
							String d = Character.toString(edblock.getInfo().split("#")[1].charAt(0));
							switch (d) {
								case EditableWorld.NORTH:
									edblock.addInfoParam("direction#" + EditableWorld.WEST);
									break;
								case EditableWorld.EAST:
									edblock.addInfoParam("direction#" + EditableWorld.NORTH);
									break;
								case EditableWorld.SOUTH:
									edblock.addInfoParam("direction#" + EditableWorld.EAST);
									break;
								case EditableWorld.WEST:
									edblock.addInfoParam("direction#" + EditableWorld.SOUTH);
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
							pane.setPadding(new Insets(10, 10, 10, 10));
							pane.setHgap(20);
							pane.setVgap(20);
							Label l1 = new Label("Set path length: ");
							Spinner sp = new Spinner(2, NewWidget.MAX_WORLD_SIZE, 2);
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
								String dir = ((RadioButton) gr.getSelectedToggle()).getText().equals("Horizontal") ? "h" : "v";
								edblock.addInfoParam(String.format("data#%s %s", pl, dir));
								editableworld.setBlockOn(edblock);
								editableworld.updateOnFile();
								unsaved();
								st.close();
							});
							Button canc = new Button("Cancel");
							canc.setOnAction(e -> st.close());
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
						MenuItem item5 = new MenuItem("Toggle water");

						item5.setOnAction(clickEv -> {
							boolean water = edblock.isWater();
							StringBuilder sb = new StringBuilder();
							if (edblock.getInfo() != null) {
								int counter = 0;
								for (String s: edblock.getInfo().split(";")) {
									if (s.split("#")[0].equals("water")) {
										sb.append(water ? "water#false" : "water#true");
									} else {
										sb.append(s);
										if (counter + 1 == edblock.getInfo().split(";").length) {
											sb.append(water ? ";water#false" : ";water#true");
											break;
										}
									}
									if (counter + 1 != edblock.getInfo().split(";").length) {
										sb.append(";");
									}
									counter++;
								}
							} else {
								sb.append(water ? "water#false" : "water#true");
							}
							edblock.setInfo(sb.toString());
							editableworld.setBlockOn(edblock);
							editableworld.updateOnFile();
							unsaved();
						});

						MenuItem item7 = new MenuItem("Set shooter block data");
						item7.setOnAction(clickEv -> {
							Stage st = new Stage();
							st.setTitle("Shooter block data");
							GridPane pane = new GridPane();
							pane.setPadding(new Insets(10, 10, 10, 10));
							pane.setHgap(20);
							pane.setVgap(20);
							Label l1 = new Label("Set damage [30]:");
							Slider slider = new Slider(1, 100, 30);
							slider.setPrefWidth(180);
							slider.setShowTickLabels(true);
							slider.setShowTickMarks(true);
							slider.setOnMouseDragged(evt -> l1.setText("Set damage [" + Math.round((int) slider.getValue()) + "]:"));
							Button ok = new Button("Save changes");
							ok.setOnAction(ev -> {
								int dam = (int) slider.getValue();
								edblock.addInfoParam(String.format("damage#%s", dam));
								editableworld.setBlockOn(edblock);
								editableworld.updateOnFile();
								unsaved();
								st.close();
							});
							Button canc = new Button("Cancel");
							canc.setOnAction(e -> st.close());
							pane.add(l1, 0, 0);
							pane.add(slider, 1, 0);
							pane.add(ok, 0, 1);
							pane.add(canc, 1, 1);
							Scene scene = new Scene(pane, 350, 130);
							scene.getStylesheets().add("file://" + changeSlash(PATH) + ".labyrinthgame/Editor/style.css");
							st.setScene(scene);
							st.show();
						});

						switch (edblock.getType()) {
							case EditableWorld.PORTAL:
								contextMenu.getItems().add(item1);
								break;
							case EditableWorld.BAT_GEN:
								contextMenu.getItems().add(item2);
								break;
							case EditableWorld.SHOOTER:
								contextMenu.getItems().add(item7);
							case EditableWorld.D_ARROW:
								contextMenu.getItems().add(item3);
								break;
							case EditableWorld.ELEVATOR:
								contextMenu.getItems().add(item4);
								break;
						}
						contextMenu.getItems().add(item5);

					} else if (mode.equals("engineering")) {
						dirSelection = null; // This parameter can also not be added to the function "updateEngBlockConn()"
						currentSelectedEngBlock = engblock;
						referenceWorld = editableworld;
						Menu item6 = new Menu("Change cable connections");
						// 0 Connections
						MenuItem con0 = new MenuItem("0 Connections");
						con0.setGraphic(new ImageView(new Image("file://" + changeSlash(PATH) + ".labyrinthgame/Images/engineering/blocks/cable.png")));
						con0.setOnAction(et -> {
							dirSelection = " ";updateEngBlockConn(dirSelection);
						});
						// 1 Connection
						Menu con1 = new Menu("1 Connection");
						con1.setGraphic(new ImageView(new Image("file://" + changeSlash(PATH) + ".labyrinthgame/Images/engineering/blocks/cable-e.png")));
						MenuItem con_n = new MenuItem("conn-n");
						con_n.setGraphic(new ImageView(new Image("file://" + changeSlash(PATH) + ".labyrinthgame/Images/engineering/blocks/cable-n.png")));
						con_n.setOnAction(et -> {
							dirSelection = "n";updateEngBlockConn(dirSelection);
						});
						MenuItem con_e = new MenuItem("conn-e");
						con_e.setGraphic(new ImageView(new Image("file://" + changeSlash(PATH) + ".labyrinthgame/Images/engineering/blocks/cable-e.png")));
						con_e.setOnAction(et -> {
							dirSelection = "e";updateEngBlockConn(dirSelection);
						});
						MenuItem con_s = new MenuItem("conn-s");
						con_s.setGraphic(new ImageView(new Image("file://" + changeSlash(PATH) + ".labyrinthgame/Images/engineering/blocks/cable-s.png")));
						con_s.setOnAction(et -> {
							dirSelection = "s";updateEngBlockConn(dirSelection);
						});
						MenuItem con_w = new MenuItem("conn-w");
						con_w.setGraphic(new ImageView(new Image("file://" + changeSlash(PATH) + ".labyrinthgame/Images/engineering/blocks/cable-w.png")));
						con_w.setOnAction(et -> {
							dirSelection = "w";updateEngBlockConn(dirSelection);
						});
						con1.getItems().addAll(con_n, con_e, con_s, con_w);
						// 2 Connections
						Menu con2 = new Menu("2 Connections");
						con2.setGraphic(new ImageView(new Image("file://" + changeSlash(PATH) + ".labyrinthgame/Images/engineering/blocks/cable-sw.png")));
						MenuItem con_ne = new MenuItem("conn-ne");
						con_ne.setGraphic(new ImageView(new Image("file://" + changeSlash(PATH) + ".labyrinthgame/Images/engineering/blocks/cable-ne.png")));
						con_ne.setOnAction(et -> {
							dirSelection = "ne";updateEngBlockConn(dirSelection);
						});
						MenuItem con_es = new MenuItem("conn-es");
						con_es.setGraphic(new ImageView(new Image("file://" + changeSlash(PATH) + ".labyrinthgame/Images/engineering/blocks/cable-es.png")));
						con_es.setOnAction(et -> {
							dirSelection = "es";updateEngBlockConn(dirSelection);
						});
						MenuItem con_sw = new MenuItem("conn-sw");
						con_sw.setGraphic(new ImageView(new Image("file://" + changeSlash(PATH) + ".labyrinthgame/Images/engineering/blocks/cable-sw.png")));
						con_sw.setOnAction(et -> {
							dirSelection = "sw";updateEngBlockConn(dirSelection);
						});
						MenuItem con_nw = new MenuItem("conn-nw");
						con_nw.setGraphic(new ImageView(new Image("file://" + changeSlash(PATH) + ".labyrinthgame/Images/engineering/blocks/cable-nw.png")));
						con_nw.setOnAction(et -> {
							dirSelection = "nw";updateEngBlockConn(dirSelection);
						});
						MenuItem con_ns = new MenuItem("conn-ns");
						con_ns.setGraphic(new ImageView(new Image("file://" + changeSlash(PATH) + ".labyrinthgame/Images/engineering/blocks/cable-ns.png")));
						con_ns.setOnAction(et -> {
							dirSelection = "ns";updateEngBlockConn(dirSelection);
						});
						MenuItem con_ew = new MenuItem("conn-ew");
						con_ew.setGraphic(new ImageView(new Image("file://" + changeSlash(PATH) + ".labyrinthgame/Images/engineering/blocks/cable-ew.png")));
						con_ew.setOnAction(et -> {
							dirSelection = "ew";updateEngBlockConn(dirSelection);
						});
						con2.getItems().addAll(con_ne, con_es, con_sw, con_nw, con_ns, con_ew);
						// 3 Connections
						Menu con3 = new Menu("3 Connections");
						con3.setGraphic(new ImageView(new Image("file://" + changeSlash(PATH) + ".labyrinthgame/Images/engineering/blocks/cable-nsw.png")));
						MenuItem con_nes = new MenuItem("conn-nes");
						con_nes.setGraphic(new ImageView(new Image("file://" + changeSlash(PATH) + ".labyrinthgame/Images/engineering/blocks/cable-nes.png")));
						con_nes.setOnAction(et -> {
							dirSelection = "nes";updateEngBlockConn(dirSelection);
						});
						MenuItem con_esw = new MenuItem("conn-esw");
						con_esw.setGraphic(new ImageView(new Image("file://" + changeSlash(PATH) + ".labyrinthgame/Images/engineering/blocks/cable-esw.png")));
						con_esw.setOnAction(et -> {
							dirSelection = "esw";updateEngBlockConn(dirSelection);
						});
						MenuItem con_nsw = new MenuItem("conn-nsw");
						con_nsw.setGraphic(new ImageView(new Image("file://" + changeSlash(PATH) + ".labyrinthgame/Images/engineering/blocks/cable-nsw.png")));
						con_nsw.setOnAction(et -> {
							dirSelection = "nsw";updateEngBlockConn(dirSelection);
						});
						MenuItem con_nwe = new MenuItem("conn-nwe");
						con_nwe.setGraphic(new ImageView(new Image("file://" + changeSlash(PATH) + ".labyrinthgame/Images/engineering/blocks/cable-nwe.png")));
						con_nwe.setOnAction(et -> {
							dirSelection = "nwe";updateEngBlockConn(dirSelection);
						});
						con3.getItems().addAll(con_nes, con_esw, con_nsw, con_nwe);
						// 4 Connections
						MenuItem con4 = new MenuItem("4 Connections");
						con4.setGraphic(new ImageView(new Image("file://" + changeSlash(PATH) + ".labyrinthgame/Images/engineering/blocks/cable-nesw.png")));
						con4.setOnAction(et -> {
							dirSelection = "nesw";updateEngBlockConn(dirSelection);
						});

						item6.getItems().addAll(con0, con1, con2, con3, con4);

						if (engblock.getType().equals(EngBlock.CABLE)) {
							//contextMenu.getItems().add(item6); // Feature will be added in 3.5/3.6
						}
					}

					contextMenu.show(canvas, event.getScreenX(), event.getScreenY());
				} else if (event.getButton() == MouseButton.PRIMARY && mode.equals("normal")) {
					if (edblock.getType() == EditableWorld.AIR && (edblock.isOnStart(editableworld) || edblock.isOnEnd(editableworld))) {
						Logger.warning("Could not place block on start or end position");
						Alert alert = new Alert(Alert.AlertType.ERROR);
						alert.setHeaderText("Could not place block on start or end position");
						alert.setTitle("SSE Error");
						alert.setContentText(null);
						alert.showAndWait();
						return;
					}
					if (edblock.getType() == EditableWorld.PORTAL && !edblock.getInfo().split(";")[edblock.checkInfoKey("point")].split("#")[1].equals("NoPointSet")) {
						String[] data = editableworld.getBlockAtCoord((int) event.getX(), (int) event.getY()).getInfo().split("#")[1].split(" ");
						editableworld.getBlockAt(Integer.parseInt(data[0]), Integer.parseInt(data[1])).addInfoParam("point#NoPointSet");
					}
					// Check clone on EditableWorld class
					if (edblock.getType() == EditableWorld.WALL) {
						if (editableworld.getBlockAt(edblock.getX(), edblock.getY() - 1) != null) {
							if (editableworld.getBlockAt(edblock.getX(), edblock.getY() - 1).getType().equals(EditableWorld.WALL)) {
								editableworld.getBlockAt(edblock.getX(), edblock.getY() - 1).removeConn("s");
							}
						}
						if (editableworld.getBlockAt(edblock.getX() + 1, edblock.getY()) != null) {
							if (editableworld.getBlockAt(edblock.getX() + 1, edblock.getY()).getType().equals(EditableWorld.WALL)) {
								editableworld.getBlockAt(edblock.getX() + 1, edblock.getY()).removeConn("w");
							}
						}
						if (editableworld.getBlockAt(edblock.getX(), edblock.getY() + 1) != null) {
							if (editableworld.getBlockAt(edblock.getX(), edblock.getY() + 1).getType().equals(EditableWorld.WALL)) {
								editableworld.getBlockAt(edblock.getX(), edblock.getY() + 1).removeConn("n");
							}
						}
						if (editableworld.getBlockAt(edblock.getX() - 1, edblock.getY()) != null) {
							if (editableworld.getBlockAt(edblock.getX() - 1, edblock.getY()).getType().equals(EditableWorld.WALL)) {
								editableworld.getBlockAt(edblock.getX() - 1, edblock.getY()).removeConn("e");
							}
						}
						editableworld.updateOnFile();
					}
					if (edblock.getType().equals(EditableWorld.PARALLEL_BLOCK)) {
						Logger.warning("Could not place block on a parallel block");
						Alert alert = new Alert(Alert.AlertType.ERROR);
						alert.setHeaderText("Could not place block on a parallel block.\nPlease remove this block from the engineering mode");
						alert.setTitle("Parallel block Error");
						alert.setContentText(null);
						alert.showAndWait();
						return;
					}
					switch (SELECTED_BLOCK) {
						case 1:
							if (edblock.getType().equals(EditableWorld.AIR)) {
								StringBuilder b = new StringBuilder();
								int c = 0;
								if (editableworld.getBlockAt(edblock.getX(), edblock.getY() - 1) != null) {
									if (editableworld.getBlockAt(edblock.getX(), edblock.getY() - 1).getWallAttach()) {
										b.append("n");
										editableworld.getBlockAt(edblock.getX(), edblock.getY() - 1).addConn("s");
									} else {
										c++;
									}
								} else {
									c++;
								}
								if (editableworld.getBlockAt(edblock.getX() + 1, edblock.getY()) != null) {
									if (editableworld.getBlockAt(edblock.getX() + 1, edblock.getY()).getWallAttach()) {
										b.append("e");
										editableworld.getBlockAt(edblock.getX() + 1, edblock.getY()).addConn("w");
									} else {
										c++;
									}
								} else {
									c++;
								}
								if (editableworld.getBlockAt(edblock.getX(), edblock.getY() + 1) != null) {
									if (editableworld.getBlockAt(edblock.getX(), edblock.getY() + 1).getWallAttach()) {
										b.append("s");
										editableworld.getBlockAt(edblock.getX(), edblock.getY() + 1).addConn("n");
									} else {
										c++;
									}
								} else {
									c++;
								}
								if (editableworld.getBlockAt(edblock.getX() - 1, edblock.getY()) != null) {
									if (editableworld.getBlockAt(edblock.getX() - 1, edblock.getY()).getWallAttach()) {
										b.append("w");
										editableworld.getBlockAt(edblock.getX() - 1, edblock.getY()).addConn("e");
									} else {
										c++;
									}
								} else {
									c++;
								}
								if (c != 4) {
									edblock.addInfoParam("conn#" + b.toString());
								} else {
									edblock.addInfoParam("conn#null");
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
							edblock.addInfoParam("point#NoPointSet");
							edblock.toggleType(EditableWorld.PORTAL);
							break;
						case 5:
							edblock.setInfo("direction#" + EditableWorld.WEST);
							edblock.toggleType(EditableWorld.SHOOTER);
							break;
						case 6:
							edblock.addInfoParam("data#NoDataSet");
							edblock.toggleType(EditableWorld.BAT_GEN);
							break;
						case 7:
							edblock.addInfoParam("data#NoDataSet");
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
							// 10: Parallel block
						case 11:
							edblock.setInfo("direction#" + EditableWorld.EAST);
							edblock.toggleType(EditableWorld.D_ARROW);
							break;
						case 12:
							edblock.setInfo(null);
							edblock.toggleType(EditableWorld.OXYGEN_POINT);
							break;
						case 13:
							if (edblock.getType().equals(EditableWorld.WALL)) {
								String plant = "n";
								if (edblock.checkInfoKey("plant") >= 0) {
									plant = edblock.getInfo().split(";")[edblock.checkInfoKey("plant")].split("#")[1];
								}
								edblock.addInfoParam("plant#" + (plant.equals("y") ? "n" : "y"));
							} else {
								edblock.setInfo(null);
								edblock.toggleType(EditableWorld.D_PLANT);
							}
							break;
						case 14:
							edblock.setInfo(null);
							edblock.toggleType(EditableWorld.D_CONE);
							break;
						case 15:
							edblock.setInfo(null);
							edblock.toggleType(EditableWorld.D_STONES);
							break;
						case 16:
							edblock.setInfo(null);
							edblock.toggleType(EditableWorld.D_BUSH);
							break;
					}

					editableworld.setBlockOn(edblock);
					editableworld.updateWalls();
					unsaved();
				} else if (event.getButton() == MouseButton.PRIMARY && mode.equals("engineering")) {
					if ((editableworld.getBlockAt(engblock.getX(), engblock.getY()).getType() != EditableWorld.AIR && !editableworld.getBlockAt(engblock.getX(), engblock.getY()).getType().equals(EditableWorld.PARALLEL_BLOCK)) && (SELECTED_BLOCK == 2 || SELECTED_BLOCK == 4) && engblock.getType().equals(EngBlock.AIR)) {
						Logger.warning("Could not place block on a block in normal mode");
						Alert alert = new Alert(Alert.AlertType.ERROR);
						alert.setHeaderText("Could not place block on a existing block in normal mode.\nPlease remove this block in the same coordinates from the normal mode");
						alert.setTitle("Normal mode block Error");
						alert.setContentText(null);
						alert.showAndWait();
						return;
					}
					switch (SELECTED_BLOCK) {
						case 1:
							engblock.setInfo(null);
							engblock.toggleType(EngBlock.CABLE);
							if (!edblock.getType().equals(EditableWorld.AIR)) {
								createParallelBlock(editableworld, engblock.getX(), engblock.getY(), ""); // Maybe delete parallel block
							}
							break;
						case 2:
							engblock.setInfo(null);
							engblock.toggleType(EngBlock.LEVER);
							createParallelBlock(editableworld, engblock.getX(), engblock.getY(), "imagePath#engineering/blocks/lever.png;category#wall;type#lever");
							break;
						case 3:
							engblock.setInfo(null);
							engblock.toggleType(EngBlock.GENERATOR);
							if (!edblock.getType().equals(EditableWorld.AIR)) {
								createParallelBlock(editableworld, engblock.getX(), engblock.getY(), ""); // Maybe delete parallel block
							}
							break;
						case 4:
							engblock.setInfo(null);
							engblock.toggleType(EngBlock.LED);
							createParallelBlock(editableworld, engblock.getX(), engblock.getY(), "imagePath#engineering/blocks/led.png;category#air;type#led");
							break;
						case 5:
							engblock.setInfo(null);
							engblock.toggleType(EngBlock.DOOR);
							createParallelBlock(editableworld, engblock.getX(), engblock.getY(), "imagePath#engineering/blocks/door.png;category#air;type#door");
							break;
					}

					editableworld.getEngineeringWorld().setBlockOn(engblock);
					editableworld.updateOnFile();
					unsaved();
				}
			}
		});

		scrollpane.setContent(canvas);

		GraphicsContext pen = canvas.getGraphicsContext2D();
		editableworld.setPen(pen);
		editableworld.setCanvas(canvas);
		editableworld.setPlayer(new Player(editableworld.start[0], editableworld.start[1], editableworld));
		editableworld.draw();

		canvas.setOnMouseMoved(event -> {
			Block block = editableworld.getBlockAtCoord((int) event.getX(), (int) event.getY());
			EngBlock Eblock;
			if (editableworld.getEngineeringWorld() != null) {
				Eblock = editableworld.getEngineeringWorld().getBlockAtCoord((int) event.getX(), (int) event.getY());
			} else {
				Eblock = null;
			}
			if (this.mode.equals("normal")) {
				String inf = "Mouse on block: " + block + " | " + ((block.isOnStart(edworld)) ? "On start position" : ((block.isOnEnd(edworld)) ? "On end position" : "Not on start or end position")) + " [" + getFileName() + (this.arcade ? "/"+SELECTED_PATTERN : "") + "]";
				this.pointingOn.setText(inf);
				this.pointingOn.setTooltip(new Tooltip(inf));
			} else if (this.mode.equals("engineering")) {
				String inf = "Mouse on block: " + Eblock + " [" + getFileName() + (this.arcade ? "/"+SELECTED_PATTERN : "") +"]";
				this.pointingOn.setText(inf);
				this.pointingOn.setTooltip(new Tooltip(inf));
			}

		});

		scrollpane.setPrefSize(this.stage.getWidth(), this.stage.getHeight());

		this.stage.widthProperty().addListener((obs, oldVal, newVal) -> scrollpane.setPrefSize((double) newVal, this.stage.getHeight()));
		this.stage.heightProperty().addListener((obs, oldVal, newVal) -> scrollpane.setPrefSize(this.stage.getWidth(), (double) newVal));

		layout.add(scrollpane, 0, 0);
		this.edworld = editableworld;
		if (OPENED_TABS > 0) {
			WORLDS.add(editableworld);
		}

		return layout;
	}

	public void createParallelBlock(EditableWorld w, int x, int y, String i) {
		EditableBlock edblock = EditableBlock.fromBlock(w.getBlockAt(x, y));
		if (edblock.getType().equals(EditableWorld.AIR)) {
			edblock.setType(EditableWorld.PARALLEL_BLOCK);
			edblock.addInfoParam(i);
		} else if (edblock.getType().equals(EditableWorld.PARALLEL_BLOCK)) { // Remove parallel block if already exists
			edblock.setType(EditableWorld.AIR);
			edblock.setInfo(null);
		}
		w.setBlockOn(edblock);
	}
	
	public static List<String> getWorkingFilePaths(){
		return WORKING_FILE_PATHS;
	}

	public static List<String> getCurrentFilePaths(){
		return CURRENT_FILE_PATHS;
	}
	
	public static List<EditableWorld> getEdWorldList(){
		return WORLDS;
	}

	private void updateEngBlockConn(String d) {
		currentSelectedEngBlock.addInfoParam("attachments#" + d);
		currentSelectedEngBlock.addInfoParam("modified#1");
		referenceWorld.getEngineeringWorld().setBlockOn(currentSelectedEngBlock);
		referenceWorld.updateOnFile();
		unsaved();
	}

	public void exit() {
		this.workLoop.stop();
		WORKING_FILE_PATHS = new ArrayList<String>();
		CURRENT_FILE_PATHS = new ArrayList<String>();
		WORKING_LISTS = new ArrayList<List<String>>();
		SAVES = new boolean[1];
		OPENED_TABS = 0;
	}

	/**
	 * Editor class constructor. Setups all editor window (toolbar, canvas, tabs, ...)
	 * @param editorFilePath the file path to open
	 */
	public Editor(String editorFilePath, Stage stage) {
		worldList = new CreatedWorldFiles();
		this.stage = stage;
		this.stage.setOnCloseRequest(event -> exit());

		GridPane layout = new GridPane();

		// Setup the menu
		MenuBar menuBar = new MenuBar();

		// IMPORTANT: If events are changed, please be sure to change also commands in the Command class

		Menu fileMenu = new Menu("_File");
		fileMenu.setMnemonicParsing(true);
		MenuItem mNew = new MenuItem("New");
		mNew.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN));
		mNew.setOnAction(e -> {
			// Clone of toolbar button
			NewWidget wid = new NewWidget(false);
			wid.setEditor(this);
		});
		MenuItem mSave = new MenuItem("Save");
		mSave.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
		mSave.setOnAction(e -> {
			try {
				saved(true);
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
				chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("World file", "*.wld"), new FileChooser.ExtensionFilter("Arcade file", "*.arc"));
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

		Menu editMenu = new Menu("_Edit");
		editMenu.setMnemonicParsing(true);
		MenuItem mAR = new MenuItem("Add row");
		mAR.setAccelerator(new KeyCodeCombination(KeyCode.G, KeyCombination.CONTROL_DOWN));
		mAR.setOnAction(e -> {
			// Clone of toolbar button
			if (checkValidityMax("h")) {
				edworld.addRow();
				unsaved();
			}
		});
		MenuItem mAC = new MenuItem("Add column");
		mAC.setAccelerator(new KeyCodeCombination(KeyCode.H, KeyCombination.CONTROL_DOWN));
		mAC.setOnAction(e -> {
			// Clone of toolbar button
			if (checkValidityMax("w")) {
				edworld.addColumn();
				unsaved();
			}
		});
		MenuItem mRR = new MenuItem("Remove row");
		mRR.setAccelerator(new KeyCodeCombination(KeyCode.G, KeyCombination.SHIFT_DOWN, KeyCombination.CONTROL_DOWN));
		mRR.setOnAction(e -> {
			// Clone of toolbar button
			checkValidity(edworld.removeRow());
			unsaved();
		});
		MenuItem mRC = new MenuItem("Remove column");
		mRC.setAccelerator(new KeyCodeCombination(KeyCode.H, KeyCombination.SHIFT_DOWN, KeyCombination.CONTROL_DOWN));
		mRC.setOnAction(e -> {
			// Clone of toolbar button
			checkValidity(edworld.removeColumn());
			unsaved();
		});
		MenuItem mSSE = new MenuItem("Change start/end position");
		mSSE.setAccelerator(new KeyCodeCombination(KeyCode.E, KeyCombination.CONTROL_DOWN));
		mSSE.setOnAction(e -> {
			// Clone of toolbar button
			new SESetup(edworld, edworld.width, edworld.height, edworld.start, edworld.end);
			unsaved();
		});
		engToDisable = mSSE;
		MenuItem mRun = new MenuItem("Run current level");
		mRun.setAccelerator(new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN));
		mRun.setOnAction(e -> {
			// Clone of toolbar button
			new LevelExe(CURRENT_FILE_PATH, getFileName(), saved, this.mode);
			LevelExe.setOnFinish(null);
		});
		mRunPattern = new MenuItem("Run current arcade pattern");
		mRunPattern.setAccelerator(new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));
		mRunPattern.setOnAction(e -> {
			// Clone of toolbar button
			new LevelExe(this.edworld.getFilePath(), "Arcade pattern (" + getFileName() + ")", saved, this.mode);
			LevelExe.setOnFinish(null);
		});
		this.mUndo = new MenuItem("Undo");
		mUndo.setAccelerator(new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN));
		mUndo.setOnAction(e -> {
			try {
				File tempFile = File.createTempFile("temp-world-" + (new Random()).nextInt(), ".wld");
				tempFile.deleteOnExit();
				BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
				writer.write(workingList.get(HISTORY_POINT-2));      // (-1) - 1
				writer.close();
				HISTORY_POINT--;
				if (HISTORY_POINT < 2){
					this.mUndo.setDisable(true);
				}
				this.mRedo.setDisable(false);
				//System.out.println("TEMP PATH: "+tempFile.getAbsolutePath());
				if (this.arcade){
					this.edworld.updateWorldList(tempFile.getAbsolutePath());
					this.edworld.worldList.updateOnFile(WORKING_FILE_PATH);
					this.edworld.changeToWorld(this.edworld.getFilePath());
					//System.out.println("EDWORLD_WORLDLIST: "+this.edworld.worldList.getLength());
				} else {
					WORKING_FILE_PATH = tempFile.getAbsolutePath();
					WORKING_FILE_PATHS.set(this.tabs.getSelectionModel().getSelectedIndex(), WORKING_FILE_PATH);
					this.edworld.changeToWorld(tempFile.getAbsolutePath());
				}
			} catch (IOException ioe){
				ioe.printStackTrace();
			}
			prepareArcadeMode(this.arcade);
			unsaved();
		});
		mUndo.setDisable(false);
		this.mRedo = new MenuItem("Redo");
		mRedo.setAccelerator(new KeyCodeCombination(KeyCode.Y, KeyCombination.CONTROL_DOWN));
		mRedo.setOnAction(e -> {
			try {
				File tempFile = File.createTempFile("temp-world-" + (new Random()).nextInt(), ".wld");
				tempFile.deleteOnExit();
				BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
				writer.write(workingList.get(HISTORY_POINT));     // (+1) - 1
				writer.close();
				HISTORY_POINT++;
				if (HISTORY_POINT == workingList.size()){
					this.mRedo.setDisable(true);
				}
				this.mUndo.setDisable(false);
				//System.out.println("TEMP PATH: "+tempFile.getAbsolutePath());
				if (this.arcade){
					this.edworld.updateWorldList(tempFile.getAbsolutePath());
					//System.out.println("------------_ "+tempFile.getAbsolutePath());
					//try {
					//	Thread.sleep(60000);
					//} catch (InterruptedException ex){}
					this.edworld.worldList.updateOnFile(WORKING_FILE_PATH);
					this.edworld.changeToWorld(this.edworld.getFilePath());
					//System.out.println("EDWORLD_WORLDLIST: "+this.edworld.worldList.getLength());
				} else {
					WORKING_FILE_PATH = tempFile.getAbsolutePath();
					WORKING_FILE_PATHS.set(this.tabs.getSelectionModel().getSelectedIndex(), WORKING_FILE_PATH);
					this.edworld.changeToWorld(tempFile.getAbsolutePath());
				}
			} catch (IOException ioe){
				ioe.printStackTrace();
			}
			prepareArcadeMode(this.arcade);
			unsaved();
		});
		editMenu.getItems().addAll(mAR, mAC, mRR, mRC, new SeparatorMenuItem(), mSSE, new SeparatorMenuItem(), mRun, mRunPattern, new SeparatorMenuItem(), mUndo, mRedo);

		Menu modeMenu = new Menu("_Mode");
		modeMenu.setMnemonicParsing(true);
		ToggleGroup modeGroup = new ToggleGroup();
		mNormal = new RadioMenuItem("Normal mode");
		mNormal.setAccelerator(new KeyCodeCombination(KeyCode.M, KeyCombination.CONTROL_DOWN));
		mNormal.setOnAction(e -> setMode("normal"));
		mNormal.setSelected(true);
		mNormal.setToggleGroup(modeGroup);
		mEngineer = new RadioMenuItem("Engineering mode");
		mEngineer.setAccelerator(new KeyCodeCombination(KeyCode.J, KeyCombination.CONTROL_DOWN));
		mEngineer.setOnAction(e -> setMode("engineering"));
		mEngineer.setToggleGroup(modeGroup);
		this.mArcade = new MenuItem("Convert to arcade mode");
		this.mArcade.setDisable(this.arcade);
		this.mArcade.setAccelerator(new KeyCodeCombination(KeyCode.K, KeyCombination.CONTROL_DOWN));
		this.mArcade.setOnAction(e -> {
			Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
			alert.setHeaderText("Do you want to delete current world file after converting?");
			alert.setTitle("Delete current world file");
			alert.setContentText(null);
			alert.showAndWait()
			.filter(response -> response == ButtonType.OK)
			.ifPresent(response -> {
				int index = this.tabs.getSelectionModel().getSelectedIndex();
				this.tabs.getTabs().get(index).setDisable(true);
				this.worldList.removeFromList(CURRENT_FILE_PATH);
				File f = new File(CURRENT_FILE_PATH);
				f.delete();
			});
			setArcadeMode();
			mArcade.setDisable(true);
		});
		modeMenu.getItems().addAll(mNormal, mEngineer, new SeparatorMenuItem(), mArcade);

		Menu prefMenu = new Menu("_Preferences");
		prefMenu.setMnemonicParsing(true);
		mLights = new CheckMenuItem("Turn on lights on all level");
		mLights.setOnAction(evt -> {
			if (this.arcade) {
				for (int i = 0; i<World.getArcadeLevels(CURRENT_FILE_PATH); i++) {
					this.edworld.worldList.getWorldAt(i).setAllLights(mLights.isSelected());
					this.edworld.worldList.getWorldAt(i).updateOnFile(false);
				}
				this.edworld.worldList.updateOnFile(CURRENT_FILE_PATH);
			} else {
				this.edworld.setAllLights(mLights.isSelected());
				this.edworld.updateOnFile();
			}
			unsaved();
		});
		MenuItem changeDescription = new MenuItem("Change world description");
		changeDescription.setOnAction(event -> {
			new ChangeDescription(this.edworld);unsaved();
		});
		prefMenu.getItems().addAll(mLights, changeDescription);

		Menu exitMenu = new Menu("Exit");
		MenuItem mExit = new MenuItem("Exit");
		mExit.setGraphic(new ImageView(new Image("file://" + changeSlash(PATH) + ".labyrinthgame/Images/editor/back_arrow.png")));
		mExit.setOnAction(e -> {
			exit();
			com.orangomango.labyrinth.menu.Menu m = new com.orangomango.labyrinth.menu.Menu(this.stage);
		});
		exitMenu.getItems().add(mExit);

		menuBar.getMenus().addAll(fileMenu, editMenu, modeMenu, prefMenu, exitMenu);

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
			wid.setEditor(this);
		});
		Button saveBtn = new Button("Save");
		saveBtn.setGraphic(new ImageView(new Image("file://" + changeSlash(PATH) + ".labyrinthgame/Images/editor/save.png")));
		saveBtn.setTooltip(new Tooltip("Save file"));
		saveBtn.setOnAction(event -> {
			try {
				saved(true);
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
				chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("World file", "*.wld"), new FileChooser.ExtensionFilter("Arcade file", "*.arc"));
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
			// Clone of menu button
			if (checkValidityMax("w")) {
				edworld.addColumn();
				unsaved();
			}
		});
		Button addRBtn = new Button();
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
		rmCBtn.setTooltip(new Tooltip("Remove column from world"));
		rmCBtn.setGraphic(new ImageView(new Image("file://" + changeSlash(PATH) + ".labyrinthgame/Images/editor/rc.png")));
		rmCBtn.setOnAction(event -> {
			// Clone of menu button
			checkValidity(edworld.removeColumn());
			unsaved();
		});
		Button rmRBtn = new Button();
		rmRBtn.setTooltip(new Tooltip("Remove row from world"));
		rmRBtn.setGraphic(new ImageView(new Image("file://" + changeSlash(PATH) + ".labyrinthgame/Images/editor/rr.png")));
		rmRBtn.setOnAction(event -> {
			// Clone of menu button
			checkValidity(edworld.removeRow());
			unsaved();
		});

		Button runBtn = new Button("Run");
		runBtn.setTooltip(new Tooltip("Run current level"));
		runBtn.setGraphic(new ImageView(new Image("file://" + changeSlash(PATH) + ".labyrinthgame/Images/editor/run.png")));
		runBtn.setOnAction(event -> {
			// Clone of menu button
			new LevelExe(CURRENT_FILE_PATH, getFileName(), saved, this.mode);
			LevelExe.setOnFinish(null);
		});

		runArcBtn = new Button("Run arcade pattern");
		runArcBtn.setTooltip(new Tooltip("Run current arcade pattern"));
		runArcBtn.setGraphic(new ImageView(new Image("file://" + changeSlash(PATH) + ".labyrinthgame/Images/editor/run.png")));
		runArcBtn.setOnAction(event -> {
			// Clone of menu button
			new LevelExe(this.edworld.getFilePath(), "Arcade pattern (" + getFileName() + ")", saved, this.mode);
			LevelExe.setOnFinish(null);
		});
		runArcBtn.setDisable(!this.arcade);

		Button sseBtn = new Button();
		engToDisableB = sseBtn;
		sseBtn.setTooltip(new Tooltip("Change start and end position"));
		sseBtn.setGraphic(new ImageView(new Image("file://" + changeSlash(PATH) + ".labyrinthgame/Images/editor/sse.png")));
		sseBtn.setOnAction(event -> {
			new SESetup(edworld, edworld.width, edworld.height, edworld.start, edworld.end);
			unsaved();
		});

		toolbar.getItems().addAll(newBtn, saveBtn, openBtn, new Separator(), addCBtn, addRBtn, rmCBtn, rmRBtn, new Separator(), sseBtn, new Separator(), runBtn, runArcBtn);

		// Setup world editor
		ScrollPane scrollpane = new ScrollPane();
		this.stage.widthProperty().addListener((obs, oldVal, newVal) -> scrollpane.setPrefSize((double) newVal, this.stage.getHeight()));
		this.stage.heightProperty().addListener((obs, oldVal, newVal) -> scrollpane.setPrefSize(this.stage.getWidth(), (double) newVal));

		this.pointingOn = new Label("Mouse on Block: null");

		if (editorFilePath != null) {
			Logger.info("Opening: " + editorFilePath);
			open(new File(editorFilePath));
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

		tabs.getTabs().add(editTab);
		splitpane.getItems().add(tabs);

		blocksTabPane = new TabPane();
		Tab blocksTab = new Tab("Blocks");
		blocksTab.setClosable(false);

		Pagination pages = new Pagination();
		pages.setPageCount(4);
		pages.setCurrentPageIndex(0);
		pages.setMaxPageIndicatorCount(4);

		blocksTab.setContent(pages);

		worldsTab = new Tab("Arcade patterns");
		worldsTab.setClosable(false);
		worldsTab.setDisable(!this.arcade);

		personalViewTab = new Tab("Pseudo arcade view");
		personalViewTab.setClosable(false);
		personalViewTab.setDisable(!this.arcade);
		
		Tab consoleTab = new Tab("Console");
		consoleTab.setClosable(false);
		BorderPane consoleContent = new BorderPane();
		
		TextField commandEntry = new TextField();
		commandEntry.setPromptText("Enter command...");
		Button exe = new Button("Enter");
		HBox input = new HBox(5, commandEntry, exe);
		VBox outputL = new VBox();
		ScrollPane output = new ScrollPane();
		output.setMinHeight(365);
		output.setMaxHeight(365);
		output.setContent(outputL);
		EventHandler<ActionEvent> customEvent = e -> {
			String in = commandEntry.getText();
			if (in.equals("")) return;
			Label inserted = new Label("> "+in.toLowerCase());
			inserted.setStyle("-fx-font-weight: bold");
			outputL.getChildren().add(inserted);
			commandEntry.setText("");
			Command cmd = new Command(in, outputL);
			cmd.setEditor(this);
			cmd.setEditableWorld(this.edworld);
			cmd.execute(true);
			// generate layout pass for the scrollpane
			output.applyCss();
			output.layout();
			output.setVvalue(1.0);
		};
		commandEntry.setOnAction(customEvent);
		exe.setOnAction(customEvent);
		consoleContent.setTop(output);
		consoleContent.setBottom(input);
		consoleContent.setPadding(new Insets(5, 5, 5, 5));
		consoleTab.setContent(consoleContent);

		prepareArcadeMode(CURRENT_FILE_PATH.endsWith(".arc") || CURRENT_FILE_PATH.endsWith(".arc.sys"));
		
		this.stage.setTitle("LabyrinthGame - Editor (" + getFileName() + ((saved) ? "" : "*") + (this.arcade ? "/"+SELECTED_PATTERN : "") + ")");

		blocksTabPane.getTabs().addAll(blocksTab, worldsTab, personalViewTab, consoleTab);

		this.tabs.getSelectionModel().selectedItemProperty().addListener((ov, ot, nt) -> {
			if (CURRENT_FILE_PATHS.size() > 0 || WORKING_FILE_PATHS.size() > 0) {
				int index = this.tabs.getSelectionModel().getSelectedIndex();
				CURRENT_FILE_PATH = CURRENT_FILE_PATHS.get(index);
				WORKING_FILE_PATH = WORKING_FILE_PATHS.get(index);
				workingList = WORKING_LISTS.get(index);
				HISTORY_POINT = 0;
				updateCurrentWorldFile(CURRENT_FILE_PATH);
				saved = SAVES[index];
				edworld = WORLDS.get(index);
				this.mLights.setSelected(edworld.getAllLights());
				this.setMode("normal");
				this.prepareArcadeMode(CURRENT_FILE_PATH.endsWith(".arc") || CURRENT_FILE_PATH.endsWith(".arc.sys"));
				if (this.blocksTabPane != null) {
					this.blocksTabPane.getSelectionModel().selectFirst();
				}
				this.stage.setTitle("LabyrinthGame - Editor (" + getFileName() + ((saved) ? "" : "*") + (this.arcade ? "/"+SELECTED_PATTERN : "") + ")");
			}
		});

		ToggleGroup tg = new ToggleGroup();

		pages.setPageFactory((pageIndex) -> {

			String style = "-fx-font-weight: bold;\n-fx-font-family: \"Courier New\";\n-fx-font-size: 14;";
			String style2 = "-fx-font-weight: bold;\n-fx-font-family: \"Courier New\";\n-fx-font-size: 11;";

			switch (pageIndex) {
				case 0:
					TilePane db = new TilePane();
					normalModePanes[0] = db;
					db.setDisable(this.mode.equals("engineering") ? true : false);
					db.setPadding(new Insets(5, 5, 5, 5));
					db.setHgap(5);
					db.setVgap(5);
					ToggleButton wallB = new ToggleButton();
					wallB.setGraphic(new ImageView(new Image("file://" + changeSlash(PATH) + ".labyrinthgame/Images/editor/button_block_wall.png")));
					wallB.setTooltip(new Tooltip("Wall block. ID:N1"));
					wallB.setToggleGroup(tg);
					wallB.setSelected(SELECTED_BLOCK == 1 && mode.equals("normal"));
					wallB.setOnAction(event -> SELECTED_BLOCK = 1);
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
					ToggleButton oxyB = new ToggleButton();
					oxyB.setGraphic(new ImageView(new Image("file://" + changeSlash(PATH) + ".labyrinthgame/Images/blocks/oxygen_point.png")));
					oxyB.setTooltip(new Tooltip("Oxygen point block. ID:N12"));
					oxyB.setToggleGroup(tg);
					oxyB.setOnAction(event -> SELECTED_BLOCK = 12);
					ToggleButton starB = new ToggleButton();
					starB.setGraphic(new ImageView(new Image("file://" + changeSlash(PATH) + ".labyrinthgame/Images/blocks/star.png")));
					starB.setTooltip(new Tooltip("Star bonus. ID:N17"));
					starB.setToggleGroup(tg);
					starB.setDisable(true);
					starB.setOnAction(event -> SELECTED_BLOCK = 17);
					db.getChildren().addAll(wallB, portalB, moveB, oxyB);
					Label header = new Label(" Default Blocks");
					header.setStyle(style);
					return new VBox(header, db);
				case 1:
					TilePane deb = new TilePane();
					deb.setDisable(this.mode.equals("engineering") ? true : false);
					normalModePanes[1] = deb;
					deb.setPadding(new Insets(5, 5, 5, 5));
					deb.setHgap(5);
					deb.setVgap(5);
					ToggleButton voidB = new ToggleButton();
					voidB.setGraphic(new ImageView(new Image("file://" + changeSlash(PATH) + ".labyrinthgame/Images/blocks/block_void.png")));
					voidB.setTooltip(new Tooltip("Void block. ID:N2"));
					voidB.setToggleGroup(tg);
					voidB.setOnAction(event -> SELECTED_BLOCK = 2);
					ToggleButton warB = new ToggleButton();
					warB.setGraphic(new ImageView(new Image("file://" + changeSlash(PATH) + ".labyrinthgame/Images/blocks/decoration_warning.png")));
					warB.setTooltip(new Tooltip("Warning decoration. ID:N9"));
					warB.setToggleGroup(tg);
					warB.setOnAction(event -> SELECTED_BLOCK = 9);
					ToggleButton arrB = new ToggleButton();
					arrB.setGraphic(new ImageView(new Image("file://" + changeSlash(PATH) + ".labyrinthgame/Images/editor/button_decoration_arrow.png")));
					arrB.setTooltip(new Tooltip("Arrow decoration. ID:N11"));
					arrB.setToggleGroup(tg);
					arrB.setOnAction(event -> SELECTED_BLOCK = 11);
					ToggleButton plantB = new ToggleButton();
					plantB.setGraphic(new ImageView(new Image("file://" + changeSlash(PATH) + ".labyrinthgame/Images/blocks/decoration_plant.png")));
					plantB.setTooltip(new Tooltip("Plant decoration. ID:N13"));
					plantB.setToggleGroup(tg);
					plantB.setOnAction(event -> SELECTED_BLOCK = 13);
					ToggleButton coneB = new ToggleButton();
					coneB.setGraphic(new ImageView(new Image("file://" + changeSlash(PATH) + ".labyrinthgame/Images/blocks/decoration_cone.png")));
					coneB.setTooltip(new Tooltip("Cone decoration. ID:N14"));
					coneB.setToggleGroup(tg);
					coneB.setOnAction(event -> SELECTED_BLOCK = 14);
					ToggleButton stonesB = new ToggleButton();
					stonesB.setGraphic(new ImageView(new Image("file://" + changeSlash(PATH) + ".labyrinthgame/Images/blocks/decoration_stones.png")));
					stonesB.setTooltip(new Tooltip("Stones decoration. ID:N15"));
					stonesB.setToggleGroup(tg);
					stonesB.setOnAction(event -> SELECTED_BLOCK = 15);
					ToggleButton bushB = new ToggleButton();
					bushB.setGraphic(new ImageView(new Image("file://" + changeSlash(PATH) + ".labyrinthgame/Images/blocks/decoration_bush.png")));
					bushB.setTooltip(new Tooltip("Bush decoration. ID:N16"));
					bushB.setToggleGroup(tg);
					bushB.setOnAction(event -> SELECTED_BLOCK = 16);
					deb.getChildren().addAll(voidB, warB, arrB, plantB, coneB, stonesB, bushB);
					Label header1 = new Label(" Decoration Blocks");
					header1.setStyle(style);
					return new VBox(header1, deb);
				case 2:
					TilePane dab = new TilePane();
					dab.setDisable(this.mode.equals("engineering") ? true : false);
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
					shootB.setGraphic(new ImageView(new Image("file://" + changeSlash(PATH) + ".labyrinthgame/Images/editor/button_block_shooter.png")));
					shootB.setTooltip(new Tooltip("Shooter block. ID:N5"));
					shootB.setToggleGroup(tg);
					shootB.setOnAction(event -> SELECTED_BLOCK = 5);
					ToggleButton batB = new ToggleButton();
					batB.setGraphic(new ImageView(new Image("file://" + changeSlash(PATH) + ".labyrinthgame/Images/editor/button_entity_bat.png")));
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
					VBox vb1 = new VBox();
					vb1.setSpacing(3);
					TilePane engp1 = new TilePane();
					engp1.setHgap(5);
					engp1.setVgap(5);
					ToggleButton cableB = new ToggleButton();
					cableB.setGraphic(new ImageView(new Image("file://" + changeSlash(PATH) + ".labyrinthgame/Images/editor/button_block_cable.png")));
					cableB.setTooltip(new Tooltip("Cable block. ID:E1"));
					cableB.setToggleGroup(tg);
					cableB.setSelected(SELECTED_BLOCK == 1 && mode.equals("engineering"));
					cableB.setOnAction(event -> SELECTED_BLOCK = 1);
					engp1.getChildren().add(cableB);
					Label h1 = new Label("Signal extender");
					h1.setStyle(style2);
					vb1.getChildren().addAll(h1, engp1);

					VBox vb2 = new VBox();
					vb2.setSpacing(3);
					TilePane engp2 = new TilePane();
					engp2.setHgap(5);
					engp2.setVgap(5);
					ToggleButton leverB = new ToggleButton();
					leverB.setGraphic(new ImageView(new Image("file://" + changeSlash(PATH) + ".labyrinthgame/Images/editor/button_block_lever.png")));
					leverB.setTooltip(new Tooltip("Lever block. ID:E2"));
					leverB.setToggleGroup(tg);
					leverB.setOnAction(event -> SELECTED_BLOCK = 2);
					engp2.getChildren().add(leverB);
					Label h2 = new Label("Signal input");
					h2.setStyle(style2);
					vb2.getChildren().addAll(h2, engp2);

					VBox vb3 = new VBox();
					vb3.setSpacing(3);
					TilePane engp3 = new TilePane();
					engp3.setHgap(5);
					engp3.setVgap(5);
					ToggleButton generatorB = new ToggleButton();
					generatorB.setGraphic(new ImageView(new Image("file://" + changeSlash(PATH) + ".labyrinthgame/Images/editor/button_block_generator.png")));
					generatorB.setTooltip(new Tooltip("Generator block. ID:E3"));
					generatorB.setToggleGroup(tg);
					generatorB.setOnAction(event -> SELECTED_BLOCK = 3);
					engp3.getChildren().add(generatorB);
					Label h3 = new Label("Signal generator");
					h3.setStyle(style2);
					vb3.getChildren().addAll(h3, engp3);

					VBox vb4 = new VBox();
					vb4.setSpacing(3);
					TilePane engp4 = new TilePane();
					engp4.setHgap(5);
					engp4.setVgap(5);
					ToggleButton ledB = new ToggleButton();
					ledB.setGraphic(new ImageView(new Image("file://" + changeSlash(PATH) + ".labyrinthgame/Images/editor/button_block_led.png")));
					ledB.setTooltip(new Tooltip("Led block. ID:E4"));
					ledB.setToggleGroup(tg);
					ledB.setOnAction(event -> SELECTED_BLOCK = 4);
					ToggleButton doorB = new ToggleButton();
					doorB.setGraphic(new ImageView(new Image("file://" + changeSlash(PATH) + ".labyrinthgame/Images/editor/button_block_door.png")));
					doorB.setTooltip(new Tooltip("Door block. ID:E5"));
					doorB.setToggleGroup(tg);
					doorB.setOnAction(event -> SELECTED_BLOCK = 5);
					engp4.getChildren().addAll(ledB, doorB);
					Label h4 = new Label("Signal output");
					h4.setStyle(style2);
					vb4.getChildren().addAll(h4, engp4);

					Label header3 = new Label(" Engineering Blocks");
					header3.setStyle(style);

					vbf = new VBox();
					vbf.getChildren().addAll(header3, vb1, vb2, vb3, vb4);
					vbf.setPadding(new Insets(5, 5, 5, 5));
					vbf.setSpacing(5);
					vbf.setDisable(this.mode.equals("engineering") ? false : true);

					return vbf;
				default:
					return new TilePane(new Label("Page: " + (pageIndex + 1)));
			}
		});

		splitpane.getItems().add(blocksTabPane);

		// Set the divider on 55%
		splitpane.setDividerPositions(0.55f);
		layout.add(menuBar, 0, 0);
		layout.add(toolbar, 0, 1);
		layout.add(splitpane, 0, 2);
		layout.add(pointingOn, 0, 3, 2, 1);

		Scene scene = new Scene(layout, 1000, 550);
		scene.getStylesheets().add("file://" + changeSlash(PATH) + ".labyrinthgame/Editor/style.css");
		this.stage.setScene(scene);
		this.workLoop = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
			try {
				StringWriter sw = new StringWriter();
				BufferedWriter writer = new BufferedWriter(sw);
				if (this.arcade){
					this.edworld.worldList.sync();
					this.edworld.worldList.updateOnFile(writer);
				} else {
					this.edworld.writeToFile(writer);
				}
				writer.close();
				String toAdd = sw.toString();
				//System.out.println("toAdd: "+toAdd.substring(0, 12));
				//System.out.println("1) HISTORY_POINT: "+HISTORY_POINT);
				//if (HISTORY_POINT > 0) System.out.println("List[H_P-1]: "+workingList.get(HISTORY_POINT-1).equals(toAdd));
				//System.out.println(workingList.contains(toAdd));
				if (HISTORY_POINT == 0 || !workingList.get(HISTORY_POINT-1).equals(toAdd)){
					workingList.add(toAdd);
					HISTORY_POINT = workingList.size();
					this.mRedo.setDisable(true);
					if (HISTORY_POINT > 1){
						this.mUndo.setDisable(false);
					}
				}
			} catch (IOException ioe){
				ioe.printStackTrace();
			}
			//System.out.println("Size: "+workingList.size()+" 2) HISTORY_POINT: "+HISTORY_POINT);
			/*for (String i : workingList){
				System.out.println(i.substring(0, 12));
			}
			System.out.println("\n");*/
		}));
		this.workLoop.setCycleCount(Animation.INDEFINITE);
		this.workLoop.play();
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
	
	// Working file path changes everytime that a world is opened or a tab is selected. When switching arcade pattern this will not change.
	public static String getWorkingFilePath(){
		return WORKING_FILE_PATH;
	}

	private void checkAndDeleteCache() {
		File f = new File(PATH + ".labyrinthgame" + File.separator + "Editor" + File.separator + "Cache");
		String[] fPaths = f.list();
		if (fPaths.length > 22) { // 20 cached files + 2 default files
			Logger.warning("More than 20 cached files! Deleting some of them");
			for (String fp: fPaths) {
				if (fp.startsWith("cache")) {
					File toDel = new File(PATH + ".labyrinthgame" + File.separator + "Editor" + File.separator + "Cache" + File.separator + fp);
					toDel.delete();
				}
			}
		}
	}

	/**
	 * Open a world file by opening a new tab containing the opened world. Will be created a copy of the world file in the cache folder.
	 * When the user saves the cached file (where the user edits the world) will be replaced with the original file.
	 * @param f the world File
	 */
	public void open(File f) {
		try {
			if (f.getAbsolutePath().contains("temp")) {
				throw new Exception("Could not open temp file");
			}
			Random r = new Random();
			int number = r.nextInt();
			boolean oldPathIn = CURRENT_FILE_PATHS.contains(f.getAbsolutePath());

			CURRENT_FILE_PATH = f.getAbsolutePath();
			updateCurrentWorldFile(CURRENT_FILE_PATH);
			WORKING_FILE_PATH = PATH + ".labyrinthgame" + File.separator + "Editor" + File.separator + "Cache" + File.separator + "cache[" + getFileName() + "]" + number + ".wld.ns"; // ns = not saved
			workingList = new ArrayList<String>();
			HISTORY_POINT = 0;
			SELECTED_PATTERN = 1;

			SAVES = Arrays.copyOf(SAVES, OPENED_TABS + 1);

			CURRENT_FILE_PATHS.add(CURRENT_FILE_PATH);
			WORKING_FILE_PATHS.add(WORKING_FILE_PATH);
			WORKING_LISTS.add(workingList);
			SAVES[OPENED_TABS] = true;
			OPENED_TABS++;

			Logger.info(CURRENT_FILE_PATHS.toString() + " " + WORKING_FILE_PATHS.toString());

			checkAndDeleteCache();
			copyWorld(CURRENT_FILE_PATH, WORKING_FILE_PATH);
			if (this.tabs != null && getCurrentFilePath() != null) {
				Tab newTab;
				if (oldPathIn) {
					newTab = this.tabs.getTabs().get(CURRENT_FILE_PATHS.indexOf(f.getAbsolutePath()));
				} else {
					newTab = new Tab(f.getName());
					newTab.setClosable(false);
					newTab.setContent(getEditorTabContent());
					this.tabs.getTabs().add(newTab);
				}
				newTab.setDisable(false);
				this.tabs.getSelectionModel().select(newTab);
			} else {
				if (this.tabs != null) {
					this.tabs.getSelectionModel().getSelectedItem().setText(getFileName());
				}
				edworld.changeToWorld(WORKING_FILE_PATH);
			}
			this.mLights.setSelected(edworld.getAllLights());
			worldList.addToList(CURRENT_FILE_PATH);
			if (this.blocksTabPane != null) {
				this.blocksTabPane.getSelectionModel().selectFirst();
			}
			//prepareArcadeMode(CURRENT_FILE_PATH.endsWith(".arc") || CURRENT_FILE_PATH.endsWith(".arc.sys")); // Currently not needed
			System.out.println(">>>> "+this.edworld.worldList.getLength());
			saved(false);
		} catch (Exception e) {
			Logger.error("Could not load world file");
			Alert alert = new Alert(Alert.AlertType.ERROR);
			alert.setHeaderText("Error while parsing file");
			alert.setTitle("Error");
			alert.setContentText("Could not load world file!\n" + e.getMessage());
			alert.showAndWait();
			//e.printStackTrace();
		}
	}
	
	public Tab getTab(String p){
		int index = CURRENT_FILE_PATHS.indexOf(p);
		if (index < 0) return null;
		return this.tabs.getTabs().get(index);
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
	public boolean checkValidityMax(String s) {
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
	public void unsaved() {
		this.saved = false;
		try {
			SAVES[this.tabs.getSelectionModel().getSelectedIndex()] = saved;
			this.tabs.getSelectionModel().getSelectedItem().setText(getFileName() + ((saved) ? "" : "*"));
		} catch (NullPointerException e) {
			SAVES[0] = saved;
		}
		this.stage.setTitle("LabyrinthGame - Editor (" + getFileName() + ((saved) ? "" : "*") + (this.arcade ? "/"+SELECTED_PATTERN : "") + ")");
	}

	/**
	 * When the user clicks the<pre>Save</pre> button, this method updates the window and the tab title
	 */
	public void saved(boolean save) {
		this.saved = true;
		if (save){
			if (this.arcade) {
				this.edworld.worldList.sync();
				this.edworld.worldList.updateOnFile(WORKING_FILE_PATH);
			}
			copyWorld(WORKING_FILE_PATH, CURRENT_FILE_PATH);
		}
		try {
			SAVES[this.tabs.getSelectionModel().getSelectedIndex()] = saved;
			this.tabs.getSelectionModel().getSelectedItem().setText(getFileName() + ((saved) ? "" : "*"));
		} catch (NullPointerException e) {
			SAVES[0] = saved;
		}
		prepareArcadeMode(this.arcade);
		this.stage.setTitle("LabyrinthGame - Editor (" + getFileName() + ((saved) ? "" : "*") + (this.arcade ? "/"+SELECTED_PATTERN : "") + ")");
	}
	
	public boolean isSaved(){
		return this.saved;
	}
	
	public String getMode(){
		return this.mode;
	}

	/**
	 * This method creates a default world 2x2
	 */
	private void createNewWorld(String name) {
		File f = new File(PATH + ".labyrinthgame" + File.separator + "Editor" + File.separator + "Levels" + File.separator + "" + name + ".wld" + ((name == "testSystemWorld-DefaultName_NoCopy") ? ".sys" : ""));
		try {
			f.createNewFile();
			BufferedWriter writer = new BufferedWriter(new FileWriter(f));
			writer.write("Deafult world\n");
			writer.write("2x2\n");
			writer.write("0,0,0,0\n");
			writer.write("1,0\n");
			writer.write("1,1\n0");
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
		checkAndCreateDir(PATH + ".labyrinthgame" + File.separator + "Fonts");
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

	private void setArcadeMode() {
		WorldList wl = new WorldList(new World(this.edworld.getFilePath()));
		String pt = CURRENT_FILE_PATH.substring(0, CURRENT_FILE_PATH.lastIndexOf(".")) + ".arc";
		File f = new File(pt);
		wl.updateOnFile(pt);
		open(f);
	}

	private void prepareArcadeMode(boolean arc) {
		this.arcade = arc;
		//System.out.println("Arcade mode: "+arc);
		//System.out.println("File: "+CURRENT_FILE_PATH+" Arcade: "+this.arcade+" Levels: "+getArcadeLevels(CURRENT_FILE_PATH));
		this.mArcade.setDisable(this.arcade);
		this.runArcBtn.setDisable(!this.arcade);
		this.mRunPattern.setDisable(!this.arcade);
		TilePane tilePane = new TilePane();
		tilePane.setPadding(new Insets(5, 5, 5, 5));
		tilePane.setHgap(10);
		tilePane.setVgap(10);
		final int PREVIEW_BLOCK_WIDTH = 10;
		World.BLOCK_WIDTH = PREVIEW_BLOCK_WIDTH;
		//System.out.println("W_F_P: "+WORKING_FILE_PATH);
		//System.out.println("C_F_P: "+CURRENT_FILE_PATH);
		//System.out.println("gAL(W): "+getArcadeLevels(WORKING_FILE_PATH));
		//System.out.println("gAL(C): "+getArcadeLevels(CURRENT_FILE_PATH));
		for (int i = 1; i<= getArcadeLevels(WORKING_FILE_PATH); i++) {
			final int now = i;
			GridPane miniP = new GridPane();
			miniP.setVgap(3);
			Label title = new Label("Pattern " + i);
			Button btn = new Button();
			btn.setGraphic(new ImageView(new Image("file://" + changeSlash(PATH) + ".labyrinthgame/Images/editor/pattern_edit.png")));
			btn.setTooltip(new Tooltip(this.edworld.worldList.getLength() > 1 ? this.edworld.worldList.getWorldAt(i - 1).getFilePath() : WORKING_FILE_PATH));
			btn.setOnAction(e -> {
				SELECTED_PATTERN = now;
				System.out.println(World.getArcadeLevels(WORKING_FILE_PATH)+" "+WORKING_FILE_PATH+" "+this.edworld.worldList.getLength());
				this.stage.setTitle("LabyrinthGame - Editor (" + getFileName() + ((saved) ? "" : "*") + (this.arcade ? "/"+SELECTED_PATTERN : "") + ")");
				this.edworld.changeToWorld(this.edworld.worldList.getWorldAt(now - 1).getFilePath());
				this.setMode("normal");
			});
			Button dBtn = new Button();
			dBtn.setGraphic(new ImageView(new Image("file://" + changeSlash(PATH) + ".labyrinthgame/Images/editor/pattern_delete.png")));
			dBtn.setTooltip(new Tooltip("Delete pattern"));
			dBtn.setDisable(i == 1);
			dBtn.setOnAction(delEvent -> {
				this.edworld.worldList.deleteWorld(now - 1);
				this.edworld.worldList.updateOnFile(WORKING_FILE_PATH);
				this.edworld.changeToWorld(WORKING_FILE_PATH);
				unsaved();
				prepareArcadeMode(this.arcade);
			});
			World tW;
			if (this.edworld.worldList.getLength() > 1) {
				tW = this.edworld.worldList.getWorldAt(now - 1);
			} else {
				tW = new World(WORKING_FILE_PATH);
				tW.setEngineeringWorld(this.edworld.getEngineeringWorld());
			}
			tW.previewMode = true;
			if (tW.getEngineeringWorld() != null) {
				tW.setDrawingMode(this.mode);
			}
			Canvas prevCanvas = new Canvas(tW.width * PREVIEW_BLOCK_WIDTH, tW.height * PREVIEW_BLOCK_WIDTH);
			GraphicsContext pen = prevCanvas.getGraphicsContext2D();
			tW.setPen(pen);
			tW.setPlayer(new Player(tW.start[0], tW.start[1], tW));
			tW.draw();
			if (i == SELECTED_PATTERN){
				miniP.setStyle("-fx-border-color: black");
			}
			miniP.add(title, 0, 0);
			miniP.add(prevCanvas, 0, 1, 2, 1);
			miniP.add(btn, 0, 2);
			miniP.add(dBtn, 1, 2);
			tilePane.getChildren().add(miniP);
		}
		Button addBtn = new Button();
		addBtn.setGraphic(new ImageView(new Image("file://" + changeSlash(PATH) + ".labyrinthgame/Images/editor/pattern_add.png")));
		addBtn.setTooltip(new Tooltip("Add new pattern"));
		addBtn.setOnAction(addEvent -> {
			try {
				File file = File.createTempFile("temp-world-" + (new Random()).nextInt(), ".wld");
				file.deleteOnExit();
				BufferedWriter writer = new BufferedWriter(new FileWriter(file));
				World.writeNewFile(writer, this.edworld.width, this.edworld.height, this.edworld.start, this.edworld.end, this.edworld.getAllLights(), "Arcade pattern description");
				writer.close();
				World newWorld = new World(file.getAbsolutePath());
				this.edworld.worldList.addWorld(newWorld);
				this.edworld.worldList.updateOnFile(WORKING_FILE_PATH);
				unsaved();
				prepareArcadeMode(this.arcade);
			} catch (IOException ioe) {}
		});
		tilePane.getChildren().add(addBtn);
		if (this.worldsTab != null) {
			this.worldsTab.setDisable(!this.arcade);
			ScrollPane sp = new ScrollPane(tilePane);
			sp.setFitToWidth(true);
			this.worldsTab.setContent(sp);
		}
		if (this.personalViewTab != null) {
			this.personalViewTab.setDisable(!this.arcade);
			GridPane personalLayout = new GridPane();
			personalLayout.setHgap(10);

			ListView<String> selPatterns = new ListView<String> ();
			ScrollPane pane = new ScrollPane(); // scrollpane for generated world

			Button generate = new Button("Generate combined world");
			generate.setOnAction(c -> {
				World temp = new World(this.edworld.worldList.getWorldAt(Integer.parseInt(Character.toString(selPatterns.getItems().get(0).charAt(8))) - 1).getFilePath());
				for (int i = 1; i<selPatterns.getItems().size(); i++) {
					temp.changeToWorld(World.combineWorlds(temp, this.edworld.worldList.getWorldAt(Integer.parseInt(Character.toString(selPatterns.getItems().get(i).charAt(8))) - 1)));
				}
				World.BLOCK_WIDTH = 20;
				Canvas cv = new Canvas(temp.width * World.BLOCK_WIDTH, temp.height * World.BLOCK_WIDTH);
				temp.setPen(cv.getGraphicsContext2D());
				temp.previewMode = true;
				temp.setPlayer(new Player(temp.start[0], temp.start[1], temp));
				temp.draw();
				pane.setContent(cv);
				World.BLOCK_WIDTH = World.DEFAULT_BLOCK_WIDTH;
			});
			generate.setDisable(true);

			ContextMenu cm = new ContextMenu();
			MenuItem deleteList = new MenuItem("Remove selected item");
			deleteList.setOnAction(c -> {
				int index = selPatterns.getSelectionModel().getSelectedIndex();
				if (index >= 0) {
					selPatterns.getItems().remove(index);
					if (selPatterns.getItems().size() == 0) {
						generate.setDisable(true);
					}
				}
			});
			MenuItem moveUp = new MenuItem("Move up selected item");
			moveUp.setOnAction(c -> {
				int index = selPatterns.getSelectionModel().getSelectedIndex();
				if (index == 0) return;
				selPatterns.getItems().add(index - 1, selPatterns.getItems().get(index));
				selPatterns.getItems().remove(index + 1);
				selPatterns.getSelectionModel().select(index - 1);
			});
			MenuItem moveDown = new MenuItem("Move down selected item");
			moveDown.setOnAction(c -> {
				int index = selPatterns.getSelectionModel().getSelectedIndex();
				if (index == selPatterns.getItems().size() - 1) return;
				selPatterns.getItems().add(index + 2, selPatterns.getItems().get(index));
				selPatterns.getItems().remove(index);
				selPatterns.getSelectionModel().select(index + 1);
			});
			cm.getItems().addAll(deleteList, new SeparatorMenuItem(), moveUp, moveDown);
			selPatterns.setContextMenu(cm);
			selPatterns.setMaxHeight(340);
			selPatterns.setMaxWidth(120);
			selPatterns.setPlaceholder(new Label("Empty list..."));
			ChoiceBox<String> box = new ChoiceBox<String> ();
			box.setMaxWidth(130);
			List<String> patternList = new ArrayList<String> ();
			for (int i = 1; i<= World.getArcadeLevels(WORKING_FILE_PATH); i++) {
				patternList.add("Pattern " + i);
			}
			box.getItems().addAll(patternList);
			Button select = new Button("Add to list");
			select.setOnAction(event -> {
				String value = box.getValue();
				if (value != null) {
					selPatterns.getItems().add(value);
					generate.setDisable(false);
				}
			});

			pane.setPrefWidth(300);
			pane.setMaxHeight(250);

			VBox selection = new VBox();
			selection.setSpacing(5);
			selection.getChildren().addAll(box, select, new Separator(), generate, pane);
			personalLayout.add(selPatterns, 0, 0);
			personalLayout.add(selection, 1, 0);
			ScrollPane sp = new ScrollPane(personalLayout);
			sp.setFitToWidth(true);
			sp.setFitToHeight(true);
			this.personalViewTab.setContent(sp);
		}
		World.BLOCK_WIDTH = World.DEFAULT_BLOCK_WIDTH;
	}

	private void setMode(String m) {
		this.mode = m;
		if (this.mode.equals("engineering")) {
			if (this.edworld.getEngineeringWorld() == null) {
				this.edworld.setEngineeringWorld(EngWorld.createNewEngWorld(this.edworld, this.edworld.width, this.edworld.height));
				this.edworld.updateOnFile();
				if (this.arcade) {
					for (int i = 0; i<World.getArcadeLevels(WORKING_FILE_PATH); i++) {
						this.edworld.worldList.getWorldAt(i).setEngineeringWorld(EngWorld.createNewEngWorld(this.edworld.worldList.getWorldAt(i), this.edworld.worldList.getWorldAt(i).width, this.edworld.worldList.getWorldAt(i).height));
						this.edworld.worldList.getWorldAt(i).updateOnFile(false);
					}
				}
				unsaved();
				Logger.info("Engineering mode created successfully");
			}
			vbf.setDisable(false);
			for (TilePane t: normalModePanes) {
				if (t != null) {
					t.setDisable(true);
				}
			}
			engToDisable.setDisable(true);
			engToDisableB.setDisable(true);
			mEngineer.setSelected(true);
			SELECTED_BLOCK = 1;
			this.edworld.setDrawingMode("engineering");
			this.edworld.update(0, 0, 0, 0);
		} else if (this.mode.equals("normal")) {
			vbf.setDisable(true);
			for (TilePane t: normalModePanes) {
				if (t != null) {
					t.setDisable(false);
				}
			}
			engToDisable.setDisable(false);
			engToDisableB.setDisable(false);
			mNormal.setSelected(true);
			SELECTED_BLOCK = 1;
			this.edworld.setDrawingMode("normal");
			this.edworld.update(0, 0, 0, 0);
			this.mode = "normal";
		}
		prepareArcadeMode(this.arcade);
		if (!this.arcade){
			this.edworld.updateWalls();
		}
	}
}
