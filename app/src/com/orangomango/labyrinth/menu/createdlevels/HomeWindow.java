package com.orangomango.labyrinth.menu.createdlevels;

import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.layout.GridPane;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;

import java.io.*;
import java.text.SimpleDateFormat;

import static com.orangomango.labyrinth.menu.Menu.MYLEVELS;
import static com.orangomango.labyrinth.menu.Menu.EDITOR;
import com.orangomango.labyrinth.menu.editor.Editor;

public class HomeWindow{
  public HomeWindow(){
    Stage stage = new Stage();
    stage.setTitle("My levels");
    
    GridPane layout = new GridPane();
    CreatedWorldFiles cwf = new CreatedWorldFiles();

		ScrollPane pane = new ScrollPane();
		pane.setPrefSize(450, 200);

		Accordion acc = new Accordion();
		for (String p : cwf.getPaths()){
			File file = new File(p);

			GridPane innerpane = new GridPane();
			innerpane.setHgap(10);
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
			innerpane.add(plabel, 0, 0);
			innerpane.add(edit, 1, 0);
			innerpane.add(mod, 0, 1);
			innerpane.add(size, 0, 2);

			TitledPane tp = new TitledPane(file.getName(), innerpane);
			acc.getPanes().add(tp);
		}

		pane.setContent(acc);
    layout.add(pane, 0, 0);

    stage.setOnCloseRequest(event -> MYLEVELS = false);
    stage.setScene(new Scene(layout, 450, 200));
    
    stage.show();
  }  
}