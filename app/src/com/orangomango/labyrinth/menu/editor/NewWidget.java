package com.orangomango.labyrinth.menu.editor;

import javafx.application.Platform;
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
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Alert;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TextArea;
import javafx.scene.paint.Color;
import javafx.geometry.Insets;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import com.orangomango.labyrinth.menu.editor.Editor;
import static com.orangomango.labyrinth.World.writeNewFile;
import static com.orangomango.labyrinth.menu.editor.Editor.PATH;
import static com.orangomango.labyrinth.menu.editor.Editor.changeSlash;
import com.orangomango.labyrinth.Logger;

import java.io.*;

public class NewWidget {
	private Stage stage;
	private static Scene SCENE_1;
	private static Scene SCENE_2;
	private static Scene SCENE_3;
	private static Scene SCENE_4;
	public static final int MAX_WORLD_SIZE = 30;
	public static final int MAX_PLAYER_VIEW_SIZE = 15;
	private static boolean FIRST_TIME = false;

	private Spinner spinner1, spinner2, spinner3, spinner4;
	private CheckBox allow, lights;
	private Label pathL;
	private Editor editor;
	private Label sizeL;
    private Stage editorStage;
    private TextArea customInfo;

	private int pWidth, pHeight = 0;
	private int sX, sY, eX, eY;

	private File file;

	public NewWidget(boolean firstTime) {
		FIRST_TIME = firstTime;
		stage = new Stage();

		stage.setOnCloseRequest(event -> {
			if (FIRST_TIME) {
				Platform.exit();
			}
		});

		GridPane layout = new GridPane();

		HBox[] boxes = new HBox[4];
		Button[] pres = new Button[4];
		Button[] nexts = new Button[4];
		Button[] finish = new Button[4];

		// HBoxes
		for (int x = 0; x<4; x++) {
			boxes[x] = new HBox();
			pres[x] = new Button("<--");
			pres[x].setTooltip(new Tooltip("Previous page"));
			pres[x].setOnAction(event -> switchScene(-1));
			if (x == 0) {
				pres[x].setDisable(true);
			}
			nexts[x] = new Button("-->");
			nexts[x].setTooltip(new Tooltip("Next page"));
			nexts[x].setOnAction(event -> switchScene(1));
			if (x == 3) {
				nexts[x].setDisable(true);
			}
			finish[x] = new Button("Finish");
			finish[x].setTooltip(new Tooltip("Create level"));
			finish[x].setOnAction(event -> finishWidget());
			if (x != 3) {
				finish[x].setDisable(true);
			}
			boxes[x].getChildren().addAll(pres[x], nexts[x], finish[x]);
		}

		// Scene 1
		GridPane l1 = new GridPane();
		l1.setPadding(new Insets(10, 10, 10, 10));
		l1.setHgap(10);
		l1.setVgap(10);
		Label sel = new Label("Selected file path: null");
		ScrollPane bp = new ScrollPane(sel);
		Button browse = new Button("Browse");
		browse.setOnAction(event -> {
			FileChooser chooser = new FileChooser();
			chooser.setTitle("Select new level file path");
			chooser.setInitialDirectory(new File(Editor.PATH + ".labyrinthgame" + File.separator + "Editor" + File.separator + "Levels" + File.separator));
			chooser.setInitialFileName("test.wld");
			chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("World file", "*.wld"));
			try {
				this.file = chooser.showSaveDialog(this.stage);
				sel.setText("Selected file path: " + this.file.getAbsolutePath());
				this.pathL.setText("Selected file path: " + this.file.getAbsolutePath());
			} catch (Exception e) {
				this.file = null;
				sel.setText("Selected file path: null");
				this.pathL.setText("Selected file path: null");
			}
		});
		Label info = new Label("Use arrows to switch between pages");
		l1.add(bp, 0, 0);
		l1.add(browse, 0, 1);
		l1.add(info, 0, 2);
		l1.add(boxes[0], 0, 3, 2, 1);

		// Scene 2
		GridPane l2 = new GridPane();
		l2.setPadding(new Insets(10, 10, 10, 10));
		l2.setHgap(10);
		l2.setVgap(10);
		Label lvlSize = new Label("Select level size:");
		Label sX = new Label("Width:");
		Label sY = new Label("Height:");
		Canvas preview = new Canvas(100, 100);
		Slider sl1 = new Slider(2, MAX_WORLD_SIZE, 2);
		Slider sl2 = new Slider(2, MAX_WORLD_SIZE, 2);
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
		pen.fillRect(0, 0, 100, 100);
		Label cPreview = new Label("preview (0x0)");
		Label playerView = new Label("Player view disabled (?)");
		playerView.setTooltip(new Tooltip("When this option is enabled, width or height is greater than "+MAX_PLAYER_VIEW_SIZE+",\nyou will se the player at the center and the level \"moving\".\nOtherwise the player will move normally.\nThe orange square is your view when you play your level"));
		sl1.setOnMouseDragged(event -> {
			this.pWidth = (int) sl1.getValue();
			updateCanvas(pen, cPreview);
			if (this.pWidth > MAX_PLAYER_VIEW_SIZE){
				playerView.setText("Player view enabled (?)");
			} else if (this.pHeight <= MAX_PLAYER_VIEW_SIZE) {
				playerView.setText("Player view disabled (?)");
			}
		});
		sl2.setOnMouseDragged(event -> {
			this.pHeight = (int) sl2.getValue();
			updateCanvas(pen, cPreview);
			if (this.pHeight > MAX_PLAYER_VIEW_SIZE){
				playerView.setText("Player view enabled (?)");
			} else if (this.pWidth <= MAX_PLAYER_VIEW_SIZE){
				playerView.setText("Player view disabled (?)");
			}
		});
		l2.add(lvlSize, 0, 0, 2, 2);
		l2.add(sX, 0, 1);
		l2.add(sY, 0, 2);
		l2.add(sl1, 1, 1);
		l2.add(sl2, 1, 2);
		l2.add(preview, 2, 0);
		l2.add(cPreview, 2, 1);
		l2.add(playerView, 2, 2);
		l2.add(boxes[1], 0, 3, 2, 1);

		// Scene 3
		GridPane l3 = new GridPane();
		l3.setPadding(new Insets(10, 10, 10, 10));
		l3.setHgap(10);
		l3.setVgap(10);
		this.allow = new CheckBox("Set start and end pos");
		Label lab1 = new Label("Start X:");
		Label lab2 = new Label("End X:");
		Label lab3 = new Label("Y:");
		Label lab4 = new Label("Y:");
		Label infoLabel = new Label("Write a short description about your new level:");
		this.customInfo = new TextArea();
		customInfo.setPromptText("Your description goes here...");
		this.spinner1 = new Spinner(0, MAX_WORLD_SIZE, 0);
		this.spinner2 = new Spinner(0, MAX_WORLD_SIZE, 0);
		this.spinner3 = new Spinner(0, MAX_WORLD_SIZE, 0);
		this.spinner4 = new Spinner(0, MAX_WORLD_SIZE, 0);
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
			if (!allow.isSelected()) {
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
		this.lights = new CheckBox("Turn on lights on all level\n(Engineering mode)");
		
		l3.add(allow, 0, 1, 3, 1);
		l3.add(lab1, 0, 2);
		l3.add(lab2, 0, 3);
		l3.add(lab3, 2, 2);
		l3.add(lab4, 2, 3);
		l3.add(this.spinner1, 1, 2);
		l3.add(this.spinner2, 3, 2);
		l3.add(this.spinner3, 1, 3);
		l3.add(this.spinner4, 3, 3);
		l3.add(this.lights, 0, 4, 4, 1);
		l3.add(infoLabel, 0, 5, 4, 1);
		l3.add(this.customInfo, 0, 6, 4, 1);
		l3.add(boxes[2], 0, 7, 2, 1);

		// Scene 4
		GridPane l4 = new GridPane();
		l4.setPadding(new Insets(10, 10, 10, 10));
		l4.setHgap(10);
		l4.setVgap(10);
		Label success = new Label("Level will be created successfully");
		Label toDo = new Label("To change your settings, use the \"<--\"\nbutton");
		this.pathL = new Label("Selected file path: null");
		ScrollPane pathP = new ScrollPane(this.pathL);
		this.sizeL = new Label(String.format("Size: %sx%s", this.pWidth, this.pHeight));
		l4.add(success, 0, 0);
		l4.add(toDo, 0, 1);
		l4.add(pathP, 0, 2);
		l4.add(this.sizeL, 0, 3);
		l4.add(boxes[3], 0, 4, 2, 1);

		SCENE_1 = new Scene(l1, 350, 300);
		SCENE_1.getStylesheets().add("file://" + changeSlash(PATH) + ".labyrinthgame/Editor/style.css");
		SCENE_2 = new Scene(l2, 350, 300);
		SCENE_2.getStylesheets().add("file://" + changeSlash(PATH) + ".labyrinthgame/Editor/style.css");
		SCENE_3 = new Scene(l3, 350, 300);
		SCENE_3.getStylesheets().add("file://" + changeSlash(PATH) + ".labyrinthgame/Editor/style.css");
		SCENE_4 = new Scene(l4, 350, 300);
		SCENE_4.getStylesheets().add("file://" + changeSlash(PATH) + ".labyrinthgame/Editor/style.css");

		stage.setTitle("Create a new world");
		stage.setScene(SCENE_1);
		stage.setResizable(false);
		stage.show();
	}

	public void updateCanvas(GraphicsContext pen, Label preview) {
		preview.setText(String.format("preview (%sx%s)", this.pWidth, this.pHeight));
		pen.setFill(Color.WHITE);
		pen.fillRect(0, 0, 100, 100);
		pen.setStroke(Color.RED);
		pen.strokeRect(10, 10, this.pWidth * 80 / MAX_WORLD_SIZE, this.pHeight * 80 / MAX_WORLD_SIZE); // x : 80 = width : 30
		if (this.pWidth > MAX_PLAYER_VIEW_SIZE || this.pHeight > MAX_PLAYER_VIEW_SIZE){
			pen.setStroke(Color.ORANGE);
			pen.strokeRect(10, 10, (this.pWidth > LevelExe.PWS*2+1 ? LevelExe.PWS*2+1 : this.pWidth) * 80 / MAX_WORLD_SIZE, (this.pHeight > LevelExe.PWS*2+1 ? LevelExe.PWS*2+1 : this.pHeight) * 80 / MAX_WORLD_SIZE);
		}
		this.sizeL.setText(String.format("Size: %sx%s", this.pWidth, this.pHeight));
	}

	public String getPath() {
		return this.file.getAbsolutePath();
	}

	public void finishWidget() {
		try {
			Logger.info("Creating new world");
			String path = this.file.getAbsolutePath();
			this.sX = (int) this.spinner1.getValue();
			this.sY = (int) this.spinner2.getValue();
			this.eX = (int) this.spinner3.getValue();
			this.eY = (int) this.spinner4.getValue();
			if (!this.allow.isSelected()) {
				sX = 1;
				eX = 1;
				eY = 1;
				sY = 0;
			}
			if (this.pWidth == 0 || this.pHeight == 0) {
				Logger.error("No dimension defined for world");
				Alert alert = new Alert(Alert.AlertType.ERROR);
				alert.setHeaderText("No dimension defined for world");
				alert.setTitle("Error");
				alert.setContentText("Choose width and height for your new world!");
				alert.showAndWait();
				return;
			}
			if (sX > this.pWidth || sY > this.pHeight || eX > this.pWidth || eY > this.pHeight) {
				Logger.error("Start or end position is outside world");
				Alert alert = new Alert(Alert.AlertType.ERROR);
				alert.setHeaderText("Start or end position is outside world");
				alert.setTitle("Error");
				alert.setContentText("Check start and end positon!");
				alert.showAndWait();
				return;
			}
			if (sX == eX && sY == eY) {
				Logger.error("Start position is on same position of end");
				Alert alert = new Alert(Alert.AlertType.ERROR);
				alert.setHeaderText("Start position is on same position of end");
				alert.setTitle("SSE Error");
				alert.setContentText(null);
				alert.showAndWait();
				return;
			} if (this.customInfo.getText().equals("")){
				Logger.error("Level description cannot be empty");
				Alert alert = new Alert(Alert.AlertType.ERROR);
				alert.setHeaderText("Level description cannot be empty");
				alert.setTitle("World description error");
				alert.setContentText(null);
				alert.showAndWait();
				return;
			}
		} catch (Exception e) {
			Alert alert = new Alert(Alert.AlertType.ERROR);
			alert.setHeaderText("Insert a path for new file");
			alert.setTitle("Error");
			alert.setContentText("Please choose a path for your new world location!");
			alert.showAndWait();
			return;
		}
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(this.file));
			writeNewFile(writer, this.pWidth, this.pHeight, new int[]{sX, sY}, new int[]{eX, eY}, this.lights.isSelected(), this.customInfo.getText().replace("\n", "\\n"));
			writer.close();
			if (this.editor == null){
				Editor editor = new Editor(this.file.getAbsolutePath(), this.editorStage);
			} else {
				this.editor.open(this.file);
			}
			Logger.info("New world created");
		} catch (IOException ex) {}
        this.stage.close();
	}

	public void setEditor(Editor editor) {
		this.editor = editor;
	}
        
        public void setEditorStage(Stage stage){
            this.editorStage = stage;
        }

	public void switchScene(int move) {
		if (move == 1) {
			if (this.stage.getScene() == SCENE_1) {
				this.stage.setScene(SCENE_2);
			} else if (this.stage.getScene() == SCENE_2) {
				this.stage.setScene(SCENE_3);
			} else if (this.stage.getScene() == SCENE_3) {
				this.stage.setScene(SCENE_4);
			} else if (this.stage.getScene() == SCENE_4) {
				this.stage.setScene(SCENE_1);
			}
		} else if (move == -1) {
			if (this.stage.getScene() == SCENE_1) {
				this.stage.setScene(SCENE_4);
			} else if (this.stage.getScene() == SCENE_2) {
				this.stage.setScene(SCENE_1);
			} else if (this.stage.getScene() == SCENE_3) {
				this.stage.setScene(SCENE_2);
			} else if (this.stage.getScene() == SCENE_4) {
				this.stage.setScene(SCENE_3);
			}
		}
	}
}
