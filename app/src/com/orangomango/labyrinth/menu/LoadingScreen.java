package com.orangomango.labyrinth.menu;

import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.scene.layout.GridPane;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.Scene;
import javafx.geometry.Insets;

import javafx.concurrent.Task;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.net.*;

import static com.orangomango.labyrinth.menu.editor.Editor.PATH;
import com.orangomango.labyrinth.Logger;

public class LoadingScreen {
	public static final int LEVELS = 5;
	private static final String[] IMGNAMES = new String[] {
		"editor" + File.separator + "ac",
		"editor" + File.separator + "ar",
		"editor" + File.separator + "rr",
		"editor" + File.separator + "rc",
		"editor" + File.separator + "run",
		"editor" + File.separator + "new",
		"editor" + File.separator + "open",
		"editor" + File.separator + "save",
		"editor" + File.separator + "sse",
		"editor" + File.separator + "warning",
		"blocks" + File.separator + "block_spike",
		"blocks" + File.separator + "block_wall",
		"blocks" + File.separator + "block_portal",
		"blocks" + File.separator + "block_air",
		"blocks" + File.separator + "block_shooter",
		"blocks" + File.separator + "end",
		"entities" + File.separator + "player",
		"entities" + File.separator + "bat_side_1",
		"entities" + File.separator + "bat_side_2",
		"entities" + File.separator + "arrow"
	};
	private static final int IMAGES = IMGNAMES.length;
	private Stage stage;
	private Menu menu;
	private static int ALERTS = 0;

	private String convertSlash(String input){
		StringBuilder out = new StringBuilder();
		for (char c : input.toCharArray()){
			if (Character.toString(c) == "\\"){
				out.append("/");
			} else {
				out.append(c);
			}
		}
		return out.toString();
	}

	private void downloadFile(String link, String path) {
		try (InputStream in = new URL(link).openStream()) {
			Files.copy( in , Paths.get(path));
		} catch (IOException ex) {
			Logger.error("No internet available");
			internetError(true, "Could not connect to " + ex.getMessage());
		}
	}

	public LoadingScreen(Menu menu) {
		this.menu = menu;
		if (new File(PATH + ".labyrinthgame" + File.separator + "SystemLevels").list().length != 0) {
			this.menu.start();
			return;
		}
		this.stage = new Stage();
		this.stage.setTitle("Downloading files");

		GridPane pane = new GridPane();
		pane.setVgap(10);
		pane.setHgap(5);
		pane.setPadding(new Insets(10, 10, 10, 10));
		final Label label = new Label("Start download to continue");
		final ProgressIndicator bar = new ProgressIndicator(0);
		bar.setMaxHeight(50);
		ImageView view = new ImageView(new Image("https://github.com/OrangoMango/LabyrinthGame/raw/main/app/lib/images/icon.png"));
		Button start = new Button("Start downloading files");
		Button end = new Button("Done");
		end.setDisable(true);
		end.setOnAction(event -> this.endWindow());
		pane.add(label, 0, 0);
		pane.add(bar, 1, 0);
		pane.add(view, 0, 1, 2, 1);
		pane.add(start, 0, 2);
		pane.add(end, 1, 2);

		start.setOnAction(event -> {
			start.setDisable(true);
			Task dwlworker = new Task() {
				@Override
				protected Object call() {
					int total = LEVELS + IMAGES - 2;
					int progress = 0;
					for (int x = 0; x<LEVELS; x++) {
						updateMessage("Downloading " + "level " + (x + 1) + ".wld.sys");
						updateProgress(progress, total);
						downloadFile(String.format("https://raw.githubusercontent.com/OrangoMango/LabyrinthGame/main/app/lib/levels/level%s.wld", x + 1), String.format(PATH + ".labyrinthgame" + File.separator + "SystemLevels" + File.separator + "level%s.wld.sys", x + 1));
						progress++;
					}
					for (int x = 0; x<IMAGES; x++) {
						updateProgress(progress, total);
						updateMessage("Downloading image " + IMGNAMES[x] + ".png");
						downloadFile(String.format("https://github.com/OrangoMango/LabyrinthGame/raw/main/app/lib/images/%s.png", convertSlash(IMGNAMES[x])), String.format(PATH + ".labyrinthgame" + File.separator + "Images" + File.separator + "%s.png", IMGNAMES[x]));
						progress++;
					}
					updateMessage("Download finished.");
					end.setDisable(false);
					return null;
				}
			};

			label.textProperty().bind(dwlworker.messageProperty());
			bar.progressProperty().bind(dwlworker.progressProperty());

			new Thread(dwlworker).start();
		});

		this.stage.setScene(new Scene(pane, 380, 250));
		this.stage.setResizable(false);
		this.stage.show();
	}

	private void internetError(boolean delete, String errormsg) {
		ALERTS++;
		if (ALERTS > 1) {
			return;
		}
		Platform.runLater(() -> {
			Alert alert = new Alert(Alert.AlertType.ERROR);
			alert.setHeaderText("Internet error, please check your connection");
			alert.setTitle("Internet Error");
			alert.setContentText("ERROR: " + errormsg);
			alert.showAndWait();
			if (delete) {
				File f = new File(PATH + ".labyrinthgame" + File.separator + "SystemLevels");
				for (String file: f.list()) {
					new File(file).delete();
				}
			}
			Platform.exit();
		});
	}

	private void endWindow() {
		this.stage.hide();
		this.menu.start();
	}
}
