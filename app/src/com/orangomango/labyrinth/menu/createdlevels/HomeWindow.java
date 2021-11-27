package com.orangomango.labyrinth.menu.createdlevels;

import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Pagination;
import javafx.scene.control.Alert;
import javafx.scene.canvas.*;

import java.io.*;
import java.text.SimpleDateFormat;

import static com.orangomango.labyrinth.menu.Menu.MYLEVELS;
import com.orangomango.labyrinth.menu.editor.Editor;
import static com.orangomango.labyrinth.menu.editor.Editor.PATH;
import static com.orangomango.labyrinth.menu.editor.Editor.changeSlash;
import com.orangomango.labyrinth.menu.editor.LevelExe;
import com.orangomango.labyrinth.World;
import com.orangomango.labyrinth.Player;

public class HomeWindow {
	
	public final static int PREVIEW_BLOCK_WIDTH = 20;

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
				temp.previewMode = true;

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
				ToggleGroup tg = new ToggleGroup();
				RadioButton nm = new RadioButton("Normal Mode");
				nm.setToggleGroup(tg);
				nm.setSelected(true);
				RadioButton em = new RadioButton("Engineering mode");
				em.setDisable(temp.getEngineeringWorld() == null);
				em.setToggleGroup(tg);
				HBox hb = new HBox();
				hb.setSpacing(5);
				hb.getChildren().addAll(nm, em);
				
				int tempW = World.BLOCK_WIDTH;
				World.BLOCK_WIDTH = PREVIEW_BLOCK_WIDTH;
				Canvas canvas = new Canvas(temp.width*World.BLOCK_WIDTH, temp.height*World.BLOCK_WIDTH);
				GraphicsContext pen = canvas.getGraphicsContext2D();
				temp.setPen(pen);
				temp.setPlayer(new Player(temp.start[0], temp.start[1], temp));
				temp.draw();
				World.BLOCK_WIDTH = tempW;
				
				Pagination pages = new Pagination();
				if (World.getArcadeLevels(temp.getFilePath()) > 0){
					pages.setPageCount(World.getArcadeLevels(temp.getFilePath()));
					pages.setCurrentPageIndex(0);
					pages.setMaxPageIndicatorCount(3);
					pages.setPageFactory(index -> {
						temp.worldList.getWorldAt(index).previewMode = true;
						em.setDisable(temp.worldList.getWorldAt(index).getEngineeringWorld() == null);
						temp.worldList.getWorldAt(index).setDrawingMode("normal");
						nm.setSelected(true);
						int tempWid = World.BLOCK_WIDTH;
						World.BLOCK_WIDTH = PREVIEW_BLOCK_WIDTH;
						Canvas PCanvas = new Canvas(temp.worldList.getWorldAt(index).width*World.BLOCK_WIDTH, temp.worldList.getWorldAt(index).height*World.BLOCK_WIDTH);
						GraphicsContext PPen = PCanvas.getGraphicsContext2D();
						temp.worldList.getWorldAt(index).setPen(PPen);
						temp.worldList.getWorldAt(index).setPlayer(new Player(temp.worldList.getWorldAt(index).start[0], temp.worldList.getWorldAt(index).start[1], temp.worldList.getWorldAt(index)));
						temp.worldList.getWorldAt(index).draw();
						World.BLOCK_WIDTH = tempWid;
						return PCanvas;
					});
				}
				
				nm.setOnAction(event -> {
					int tempWid = World.BLOCK_WIDTH;
					World.BLOCK_WIDTH = PREVIEW_BLOCK_WIDTH;
					if (World.getArcadeLevels(temp.getFilePath()) < 0){
						temp.setDrawingMode("normal");
						temp.update(0, 0, 0, 0);
					} else {
						temp.worldList.getWorldAt(pages.getCurrentPageIndex()).setDrawingMode("normal");
						temp.worldList.getWorldAt(pages.getCurrentPageIndex()).update(0, 0, 0, 0);
					}
					World.BLOCK_WIDTH = tempWid;
				});
				
				em.setOnAction(event -> {
					int tempWid = World.BLOCK_WIDTH;
					World.BLOCK_WIDTH = PREVIEW_BLOCK_WIDTH;
					if (World.getArcadeLevels(temp.getFilePath()) < 0){
						temp.setDrawingMode("engineering");
						temp.update(0, 0, 0, 0);
					} else {
						temp.worldList.getWorldAt(pages.getCurrentPageIndex()).setDrawingMode("engineering");
						temp.worldList.getWorldAt(pages.getCurrentPageIndex()).update(0, 0, 0, 0);
					}
					World.BLOCK_WIDTH = tempWid;
				});
				
				innerpane.add(plabel, 0, 0);
				innerpane.add(World.getArcadeLevels(temp.getFilePath()) > 0 ? pages : canvas, 1, 0, 1, 4);
				innerpane.add(edit, 2, 0);
				innerpane.add(mod, 0, 1);
				innerpane.add(size, 0, 2);
				innerpane.add(del, 2, 1);
				innerpane.add(run, 2, 2);
				innerpane.add(author, 0, 3);
				innerpane.add(pub, 2, 3);
				innerpane.add(hb, 0, 4, 2, 1);

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
		Scene scene = new Scene(layout, 800, 500);
		scene.getStylesheets().add("file://" + changeSlash(PATH) + ".labyrinthgame/Editor/style.css");
		stage.setScene(scene);

		stage.show();
	}
}
