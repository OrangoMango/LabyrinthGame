package com.orangomango.labyrinth.menu;

import javafx.stage.Stage;
import javafx.scene.layout.GridPane;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.Scene;

import javafx.concurrent.Task;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.net.*;

import static com.orangomango.labyrinth.menu.editor.Editor.PATH;

public class LoadingScreen{
  private static final int LEVELS = 3;
	private static final String[] IMGNAMES = new String[]{"ac", "ar", "rr", "rc", "run", "new", "open", "save", "sse"};
	private static final int IMAGES = IMGNAMES.length;
	private Stage stage;
	private Menu menu;

  private void downloadFile(String link, String path){
    try (InputStream in = new URL(link).openStream()) {
      Files.copy(in, Paths.get(path));
    } catch (IOException ex){}
  }

  public LoadingScreen(Menu menu){
		this.menu = menu;
		if (new File(PATH+".labyrinthgame"+File.separator+"SystemLevels").list().length != 0){
			this.menu.start();
      return;
    }
		this.stage = new Stage();
		this.stage.setTitle("Downloading files");

		GridPane pane = new GridPane();
		pane.setVgap(10);
		pane.setHgap(5);
		final Label label = new Label("Start download to continue");
		final ProgressIndicator bar = new ProgressIndicator(0);
		bar.setMaxHeight(50);
		ImageView view = new ImageView(new Image("https://orangomango.github.io/img/icon.png"));
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
			Task dwlworker = new Task(){
				@Override
				protected Object call(){
					int total = LEVELS + IMAGES - 2;
					int progress = 0;
					for (int x = 0; x < LEVELS; x++){
						updateMessage("Downloading "+"level "+(x+1)+".wld.sys");
						updateProgress(progress, total);
						downloadFile(String.format("https://raw.githubusercontent.com/OrangoMango/LabyrinthGame/main/app/lib/levels/level%s.wld", x+1), String.format(PATH+".labyrinthgame"+File.separator+"SystemLevels"+File.separator+"level%s.wld.sys", x+1));
						progress++;
					}
					for (int x = 0; x < IMAGES; x++){
						updateMessage("Downloading image "+IMGNAMES[x]+".png");
						updateProgress(progress, total);
						downloadFile(String.format("https://github.com/OrangoMango/LabyrinthGame/raw/main/app/lib/images/%s.png", IMGNAMES[x]), String.format(PATH+".labyrinthgame"+File.separator+"Images"+File.separator+"%s.png", IMGNAMES[x]));
						progress++;
					}
					updateMessage("Download finished.");
					end.setDisable(false);
					start.setDisable(true);
					return null;
				}
			};

			label.textProperty().bind(dwlworker.messageProperty());
			bar.progressProperty().bind(dwlworker.progressProperty());

			new Thread(dwlworker).start();
		});

		this.stage.setScene(new Scene(pane, 300, 350));
		this.stage.show();
  }

	private void endWindow(){
		this.stage.hide();
		this.menu.start();
	}
}