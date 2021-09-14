package com.orangomango.labyrinth.menu;

import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Hyperlink;

import java.io.*;

import com.orangomango.labyrinth.menu.editor.Editor;
import com.orangomango.labyrinth.menu.createdlevels.HomeWindow;
import com.orangomango.labyrinth.menu.play.PlayScreen;
import com.orangomango.labyrinth.LabyrinthMain; // import main application
import static com.orangomango.labyrinth.menu.editor.Editor.PATH;
import static com.orangomango.labyrinth.menu.editor.Editor.changeSlash;

public class Menu {
	private Stage stage;
	private LabyrinthMain toShowWorld;

	public static boolean MYLEVELS = false;
	public static boolean PLAY = false;

	public static String OPEN = null;

	public Menu(double version) {
		this.stage = new Stage();
		this.stage.setTitle("Menu v" + version);

		LoadingScreen ls = new LoadingScreen(this);

		GridPane layout = new GridPane();
		layout.setHgap(20);
		layout.setVgap(20);

		Button playBtn = new Button("Play");
		playBtn.setOnAction(event -> {
			if (!PLAY) {
				PlayScreen screen = new PlayScreen(this.toShowWorld);
				PLAY = true;
			}
		});

		Button editorBtn = new Button("Editor");
		editorBtn.setOnAction(event -> {
			startEditor(null);
		});

		Button levelsBtn = new Button("My levels");
		levelsBtn.setOnAction(event -> {
			if (!MYLEVELS) {
				HomeWindow hw = new HomeWindow();
				MYLEVELS = true;
			}
		});

		Label sign = new Label("Game by OrangoMango (C)2021");
		Hyperlink l = new Hyperlink("https://orangomango.github.io");
		l.setOnAction(event -> System.out.println("Click at this link: https://orangomango.github.io"));
		layout.add(playBtn, 0, 0);
		layout.add(editorBtn, 1, 0);
		layout.add(levelsBtn, 2, 0);
		layout.add(sign, 0, 1, 3, 1);
		layout.add(l, 0, 2, 3, 1);

		Scene scene = new Scene(layout, 300, 200);
		scene.getStylesheets().add("file://" + changeSlash(PATH) + ".labyrinthgame/Editor/style.css");
		this.stage.setScene(scene);
	}

	private void startEditor(String param) {
		Editor editor = new Editor(param);
		editor.start();
	}

	public void setTSW(LabyrinthMain tsw) {
		this.toShowWorld = tsw;
	}

	public void start() {
		this.stage.show();
		if (OPEN != null) {
			System.out.println("Opening requested file: " + OPEN);
			Editor editor = new Editor(OPEN);
			editor.start();
		}
	}

	public void stop() {
		this.stage.hide();
	}
}
