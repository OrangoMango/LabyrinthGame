package com.orangomango.labyrinth.menu.createdlevels;

import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.layout.GridPane;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;

import java.io.*;
import java.text.SimpleDateFormat;

import static com.orangomango.labyrinth.menu.Menu.MYLEVELS;
import com.orangomango.labyrinth.menu.editor.Editor;
import static com.orangomango.labyrinth.menu.editor.Editor.PATH;
import static com.orangomango.labyrinth.menu.editor.Editor.changeSlash;
import com.orangomango.labyrinth.menu.editor.LevelExe;
import com.orangomango.labyrinth.World;

public class HomeWindow {

	private String getDim(int w, int h){
	    int area = w*h;
	    
	    /*
	     * 0 - 99     XS
	     * 100 - 195  S
	     * 196 - 399  M
	     * 400 - 870  L
	     * 900        XL
	     */
	    
	    if (area <= 99){
		return "XS";
	    } else if (area <= 195){
		return "S";
	    } else if (area <= 399){
		return "M";
	    } else if (area <= 870){
		return "L";
	    } else if (area <= 900){
		return "XL";
	    } else {
		return "N/A";
	    }
	}

	public HomeWindow() {
		Stage stage = new Stage();
		stage.setTitle("My levels");

		GridPane layout = new GridPane();
		CreatedWorldFiles cwf = new CreatedWorldFiles();

		ScrollPane pane = new ScrollPane();
		pane.setPrefSize(450, 200);
		stage.widthProperty().addListener((obs, oldVal, newVal) -> pane.setPrefSize((double) newVal, stage.getHeight()));
		stage.heightProperty().addListener((obs, oldVal, newVal) -> pane.setPrefSize(stage.getWidth(), (double) newVal));

		if (cwf.getPaths().length == 0) {
			pane.setContent(new Label("You did not create any levels yet :(\n Create one in the editor"));
		} else {
			Accordion acc = new Accordion();
			for (String p: cwf.getPaths()) {
				File file = new File(p);
				World temp = new World(p);

				GridPane innerpane = new GridPane();
				innerpane.setHgap(10);
				innerpane.setVgap(6);
				Label plabel = new Label(p);
				Button edit = new Button("Edit");
				edit.setOnAction(event -> {
					if (Editor.EDITOR_INSTANCE == null) {
						Editor editor = new Editor(p);
						editor.start();
					} else {
						Editor.EDITOR_INSTANCE.open(new File(p));
					}
				});
				SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
				Label mod = new Label("Last modified: " + format.format(file.lastModified()));
				Label size = new Label(String.format("Size: %d bytes (Dim.: %dx%d) %s", file.length(), temp.width, temp.height, getDim(temp.width, temp.height)));
				Label author = new Label("Author: -");
				Button del = new Button("Delete");
				Button run = new Button("Run");
				Button pub = new Button("Publish");
				pub.setDisable(true);
				run.setOnAction(event -> {
					new LevelExe(p, file.getName(), true, "normal");LevelExe.setOnFinish(null);
				});
				innerpane.add(plabel, 0, 0);
				innerpane.add(edit, 1, 0);
				innerpane.add(mod, 0, 1);
				innerpane.add(size, 0, 2);
				innerpane.add(del, 1, 1);
				innerpane.add(run, 1, 2);
				innerpane.add(author, 0, 3);
				innerpane.add(pub, 1, 3);

				final TitledPane tp = new TitledPane(file.getName(), innerpane);

				del.setOnAction(event -> {
					File f = new File(p);
					f.delete();
					if (Editor.getCurrentFilePath().equals(p)) {
						File f2 = new File(Editor.PATH + ".labyrinthgame" + File.separator + "Editor" + File.separator + "Cache" + File.separator + "currentFile.data");
						f2.delete();
					}
					cwf.removeFromList(p);
					Alert alert = new Alert(Alert.AlertType.INFORMATION);
					alert.setHeaderText("File deleted successfully");
					alert.setTitle("File deleted");
					alert.setContentText("File deleted successfully.");
					alert.showAndWait();
					acc.getPanes().remove(tp);
					if (cwf.getPaths().length == 0) {
						pane.setContent(new Label("You did not create any levels yet :(\n Create one in the editor"));
					}
				});
				acc.getPanes().add(tp);
			}
			pane.setContent(acc);
		}

		layout.add(pane, 0, 0);

		stage.setOnCloseRequest(event -> MYLEVELS = false);
		Scene scene = new Scene(layout, 550, 300);
		scene.getStylesheets().add("file://" + changeSlash(PATH) + ".labyrinthgame/Editor/style.css");
		stage.setScene(scene);

		stage.show();
	}
}
