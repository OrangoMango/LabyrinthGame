package com.orangomango.labyrinth.menu;

import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.scene.layout.GridPane;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
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
	public static final int LEVELS = 10;
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
		"editor" + File.separator + "button_editor",
		"editor" + File.separator + "button_play",
		"editor" + File.separator + "button_levels",		
		"editor" + File.separator + "warning",
		"editor" + File.separator + "arrow_sign_h",
		"editor" + File.separator + "arrow_sign_v",
		"editor" + File.separator + "pattern_add",
                "editor" + File.separator + "pattern_edit",
                "editor" + File.separator + "pattern_delete",
                "editor" + File.separator + "menu_run",
		"blocks" + File.separator + "block_spike",
		"blocks" + File.separator + "block_spike_closed",
		"blocks" + File.separator + "block_wall-null",
		"blocks" + File.separator + "block_wall-n",
		"blocks" + File.separator + "block_wall-e",
		"blocks" + File.separator + "block_wall-s",
		"blocks" + File.separator + "block_wall-w",
		"blocks" + File.separator + "block_wall-ne",
		"blocks" + File.separator + "block_wall-es",
		"blocks" + File.separator + "block_wall-sw",
		"blocks" + File.separator + "block_wall-nw",
		"blocks" + File.separator + "block_wall-nes",
		"blocks" + File.separator + "block_wall-esw",
		"blocks" + File.separator + "block_wall-nsw",
		"blocks" + File.separator + "block_wall-new",
		"blocks" + File.separator + "block_wall-nesw",
		"blocks" + File.separator + "block_wall-ns",
		"blocks" + File.separator + "block_wall-ew",
		"blocks" + File.separator + "block_portal",
		"blocks" + File.separator + "block_air",
		"blocks" + File.separator + "block_shooter",
		"blocks" + File.separator + "decoration_warning",
		"blocks" + File.separator + "decoration_arrow",
		"blocks" + File.separator + "oxygen_point",
		"blocks" + File.separator + "end",
		"entities" + File.separator + "move_block",
		"entities" + File.separator + "player",
		"entities" + File.separator + "bat_side_1",
		"entities" + File.separator + "bat_side_2",
		"entities" + File.separator + "bat_front_1",
		"entities" + File.separator + "bat_front_2",
		"entities" + File.separator + "bat_front_3",
		"entities" + File.separator + "arrow",
		"entities" + File.separator + "health",
		"entities" + File.separator + "oxygen",
		"engineering" + File.separator + "blocks" + File.separator + "generator_1",
		"engineering" + File.separator + "blocks" + File.separator + "generator_2",
		"engineering" + File.separator + "blocks" + File.separator + "generator_3",
		"engineering" + File.separator + "blocks" + File.separator + "generator_4",
		"engineering" + File.separator + "blocks" + File.separator + "generator_5",
		"engineering" + File.separator + "blocks" + File.separator + "lever_on",
		"engineering" + File.separator + "blocks" + File.separator + "lever_off",
		"engineering" + File.separator + "blocks" + File.separator + "cable",
		"engineering" + File.separator + "blocks" + File.separator + "cable-n",
		"engineering" + File.separator + "blocks" + File.separator + "cable-e",
		"engineering" + File.separator + "blocks" + File.separator + "cable-s",
		"engineering" + File.separator + "blocks" + File.separator + "cable-w",
		"engineering" + File.separator + "blocks" + File.separator + "cable-ne",
		"engineering" + File.separator + "blocks" + File.separator + "cable-es",
		"engineering" + File.separator + "blocks" + File.separator + "cable-sw",
		"engineering" + File.separator + "blocks" + File.separator + "cable-nw",
		"engineering" + File.separator + "blocks" + File.separator + "cable-nes",
		"engineering" + File.separator + "blocks" + File.separator + "cable-esw",
		"engineering" + File.separator + "blocks" + File.separator + "cable-nsw",
		"engineering" + File.separator + "blocks" + File.separator + "cable-new",
		"engineering" + File.separator + "blocks" + File.separator + "cable-nesw",
		"engineering" + File.separator + "blocks" + File.separator + "cable-ns",
		"engineering" + File.separator + "blocks" + File.separator + "cable-ew",
		"engineering" + File.separator + "blocks" + File.separator + "door_1",
		"engineering" + File.separator + "blocks" + File.separator + "door_2",
		"engineering" + File.separator + "blocks" + File.separator + "door_3",
		"engineering" + File.separator + "blocks" + File.separator + "door_4",
		"engineering" + File.separator + "blocks" + File.separator + "led_on",
		"engineering" + File.separator + "blocks" + File.separator + "led_off",
		"engineering" + File.separator + "blocks" + File.separator + "block_air"
	};
	
	private static final int IMAGES = IMGNAMES.length;
	private Stage stage;
	private Menu menu;
	private static int ALERTS = 0;

	private String convertSlash(String input){
		StringBuilder out = new StringBuilder();
		for (char c : input.toCharArray()){
			if (Character.toString(c).equals("\\")){
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
			Logger.error("No internet available/Old update detected");
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
		final ProgressBar bar = new ProgressBar(0);
		final ProgressIndicator bar2 = new ProgressIndicator(0);
		bar.setPrefWidth(320);
		bar2.setMaxHeight(40);
		ImageView view = new ImageView(new Image("https://github.com/OrangoMango/LabyrinthGame/raw/main/app/lib/images/icon.png"));
		Button start = new Button("Start downloading files");
		Button end = new Button("Done");
		end.setDisable(true);
		end.setOnAction(event -> this.endWindow());
		pane.add(label, 0, 0, 3, 1);
		pane.add(view, 0, 1, 2, 1);
		pane.add(bar, 0, 2, 2, 1);
		pane.add(bar2, 2, 2, 1, 2);
		pane.add(start, 0, 3);
		pane.add(end, 1, 3);

		start.setOnAction(event -> {
			start.setDisable(true);
			Task dwlworker = new Task() {
				@Override
				protected Object call() {
					int total = LEVELS + IMAGES - 2 + 1;
					int progress = 0;
					updateMessage("Downloading styles");
					updateProgress(progress, total);
					downloadFile("https://raw.githubusercontent.com/OrangoMango/LabyrinthGame/main/app/lib/style.css", PATH + ".labyrinthgame" + File.separator + "Editor" + File.separator + "style.css");
					progress++;
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
			bar2.progressProperty().bind(dwlworker.progressProperty());

			new Thread(dwlworker).start();
		});

		Scene scene = new Scene(pane, 410, 250);
		scene.getStylesheets().add("https://raw.githubusercontent.com/OrangoMango/LabyrinthGame/main/app/lib/style.css");
		this.stage.setScene(scene);
		this.stage.setResizable(false);
		this.stage.show();
	}

	private void internetError(boolean delete, String errormsg) {
		ALERTS++;
		if (ALERTS > 1) {
			return;
		}
		if (delete) {
			File f = new File(PATH + ".labyrinthgame" + File.separator + "SystemLevels");
			for (String file: f.list()) {
				File f2 = new File(file);
				f2.delete();
			}
		}
		Platform.runLater(() -> {
			Alert alert = new Alert(Alert.AlertType.ERROR);
			alert.setHeaderText("Internet error, please check your connection. If you have internet then be sure to have the latest version of this app");
			alert.setTitle("Internet/Update Error");
			alert.setContentText("ERROR: " + errormsg);
			alert.showAndWait();
			Platform.exit();
		});
	}

	private void endWindow() {
		this.stage.close();
		this.menu.start();
	}
}
