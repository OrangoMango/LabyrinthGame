package com.orangomango.labyrinth.menu.editor;

import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;

import java.io.*;

import static com.orangomango.labyrinth.menu.editor.Editor.PATH;
import static com.orangomango.labyrinth.menu.editor.Editor.changeSlash;

public class Selection {

	private Editor editor;
	private Stage stage;

	public Selection(Stage stage) {

		this.stage = stage;
		this.stage.setTitle("Create or open a file");
		Button newBtn = new Button("New");
		newBtn.setOnAction(event -> setupNewWidget());
		Button openBtn = new Button("Open");
		openBtn.setOnAction(event -> setupEditor());
		Label or = new Label("or");

		GridPane layout = new GridPane();
		layout.setHgap(10);

		layout.add(newBtn, 0, 0);
		layout.add(or, 1, 0);
		layout.add(openBtn, 2, 0);

		Scene scene = new Scene(layout, 300, 100);
		scene.getStylesheets().add("file://" + changeSlash(PATH) + ".labyrinthgame/Editor/style.css");
		
		this.stage.setScene(scene);
		this.stage.setResizable(false);
		this.stage.setOnCloseRequest(event -> Platform.exit());
		this.stage.show();
	}

	private void setupNewWidget() {
		NewWidget wid = new NewWidget(true);
                wid.setEditorStage(this.stage);
	}

	private void setupEditor() {
		try {
			FileChooser chooser = new FileChooser();
			chooser.setInitialDirectory(new File(PATH + ".labyrinthgame" + File.separator + "Editor" + File.separator + "Levels" + File.separator));
			chooser.setTitle("Open world");
			chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("World file", "*.wld"));
			File f = chooser.showOpenDialog(this.stage);
                        if (this.editor == null){
                            Editor edi = new Editor(f.getAbsolutePath(), this.stage);
                        } else {
                            this.editor.open(f);
                        }
		} catch (Exception e) {
			Alert alert = new Alert(Alert.AlertType.ERROR);
			alert.setHeaderText("Error while parsing file");
			alert.setTitle("Error");
			alert.setContentText("Could not open world file!");
			alert.showAndWait();
                        Platform.exit();
		}
	}
}