package com.orangomango.labyrinth.menu.editor;

import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.Button;
import javafx.geometry.Insets;

import static com.orangomango.labyrinth.menu.editor.Editor.PATH;
import static com.orangomango.labyrinth.menu.editor.Editor.changeSlash;

public class ChangeDescription {
	private EditableWorld world;
	private static boolean OPEN = false;
	
	public ChangeDescription(EditableWorld ew){
		if (OPEN){
			return;
		}
		this.world = ew;
		OPEN = true;
		Stage stage = new Stage();
		stage.setOnCloseRequest(e -> OPEN = false);
		stage.setTitle("Change world description");
		GridPane layout = new GridPane();
		layout.setPadding(new Insets(5, 5, 5, 5));
		layout.setVgap(10);
		layout.setHgap(10);
		Label text = new Label("Description: ");
		TextArea info = new TextArea();
		info.setPromptText("Your description goes here...");
		info.setText(ew.getWorldInformation().replace("\\n", "\n"));
		info.end();
		Button ok = new Button("Save changes");
		ok.setOnAction(e -> {
			this.world.setWorldInformation(info.getText().replace("\n", "\\n"));
			OPEN = false;
			stage.close();
		});
		Button exit = new Button("Close");
		exit.setOnAction(e -> {OPEN = false; stage.close();});
		layout.add(text, 0, 0, 2, 1);
		layout.add(info, 0, 1, 2, 1);
		layout.add(ok, 0, 2);
		layout.add(exit, 1, 2);
		Scene scene = new Scene(layout, 300, 200);
		scene.getStylesheets().add("file://" + changeSlash(PATH) + ".labyrinthgame/Editor/style.css");
		stage.setScene(scene);
		stage.show();
	}
}
