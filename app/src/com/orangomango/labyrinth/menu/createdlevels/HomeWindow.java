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
import static com.orangomango.labyrinth.menu.Menu.EDITOR;
import com.orangomango.labyrinth.menu.editor.Editor;
import com.orangomango.labyrinth.menu.editor.LevelExe;

public class HomeWindow{
  public HomeWindow(){
    Stage stage = new Stage();
    stage.setTitle("My levels");
    
    GridPane layout = new GridPane();
    CreatedWorldFiles cwf = new CreatedWorldFiles();

		ScrollPane pane = new ScrollPane();
		pane.setPrefSize(450, 200);


		if (cwf.getPaths().length == 0){
			pane.setContent(new Label("You did not create any levels yet :(\n Create one in the editor"));
		} else {
			Accordion acc = new Accordion();
			acc.setMaxWidth(450);
			for (String p : cwf.getPaths()){
				File file = new File(p);

				GridPane innerpane = new GridPane();
				innerpane.setHgap(10);
				innerpane.setVgap(6);
				Label plabel = new Label(p);
				Button edit = new Button("Edit");
				edit.setOnAction(event -> {
					Editor editor = new Editor(p);
					editor.start();
					EDITOR = true;
				});
				SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
				Label mod = new Label("Last modified: "+format.format(file.lastModified()));
				Label size = new Label(String.format("Size: %d bytes", file.length()));
				Button del = new Button("Delete");
				Button run = new Button("Run");
				run.setOnAction(event -> new LevelExe(p, file.getName(), true));
				innerpane.add(plabel, 0, 0);
				innerpane.add(edit, 1, 0);
				innerpane.add(mod, 0, 1);
				innerpane.add(size, 0, 2);
				innerpane.add(del, 1, 1);
				innerpane.add(run, 1, 2);

				final TitledPane tp = new TitledPane(file.getName(), innerpane);

				del.setOnAction(event -> {
					File f = new File(p);
					f.delete();
					if (Editor.getCurrentFilePath().equals(p)){
						File f2 = new File(Editor.PATH+".labyrinthgame"+File.separator+"Editor"+File.separator+"Cache"+File.separator+"currentFile.data");
						f2.delete();
					}
					cwf.removeFromList(p);
					Alert alert = new Alert(Alert.AlertType.INFORMATION);
					alert.setHeaderText("File deleted successfully");
					alert.setTitle("File deleted");
					alert.setContentText("File deleted successfully.");
					alert.showAndWait();
					acc.getPanes().remove(tp);
					if (cwf.getPaths().length == 0){
						pane.setContent(new Label("You did not create any levels yet :(\n Create one in the editor"));
					}
				});
				acc.getPanes().add(tp);
			}
			pane.setContent(acc);
		}

    layout.add(pane, 0, 0);

    stage.setOnCloseRequest(event -> MYLEVELS = false);
    stage.setScene(new Scene(layout, 450, 200));
    
    stage.show();
  }  
}