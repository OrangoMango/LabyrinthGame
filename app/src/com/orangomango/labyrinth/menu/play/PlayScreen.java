package com.orangomango.labyrinth.menu.play;

import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.geometry.Insets;
import javafx.scene.layout.GridPane;
import javafx.scene.control.Button;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tab;
import javafx.scene.control.ScrollPane;
import javafx.scene.canvas.*;
import javafx.scene.paint.Color;

import java.io.*;

import com.orangomango.labyrinth.LabyrinthMain;
import com.orangomango.labyrinth.menu.editor.LevelExe;
import static com.orangomango.labyrinth.menu.editor.Editor.PATH;
import static com.orangomango.labyrinth.menu.editor.Editor.changeSlash;
import static com.orangomango.labyrinth.menu.Menu.PLAY;
import static com.orangomango.labyrinth.menu.LoadingScreen.LEVELS;

public class PlayScreen {

	public static int[] LEVELS_OPEN = new int[LEVELS];
	private GraphicsContext pen;
	
	private static int BUTTON_WIDTH = 65;

	private static int getLevelIndex(String level) {
		return Integer.parseInt(Character.toString(level.charAt(5)));
	}
	
	private void drawCanvas(int num){
		for (int i = 0; i < num-1; i++){
			switch (i % 4){
				case 0:
					this.pen.strokeLine(60+240*i/4, 70, 60+240*i/4, 190);   // Line Width: 120
					break;
				case 1:
					this.pen.strokeLine(60+240*(i-1)/4, 190, 180+240*(i-1)/4, 190);
					break;
				case 2:
					this.pen.strokeLine(180+240*(i-2)/4, 190, 180+240*(i-2)/4, 70);
					break;
				case 3:
					this.pen.strokeLine(180+240*(i-3)/4, 70, 300+240*(i-3)/4, 70);
			}
		}
		for (int i = 0; i < num; i++){
			this.pen.setFill(Color.GREEN);
			switch (i % 4){
				case 0:
					this.pen.fillRect(60+240*i/4-BUTTON_WIDTH/2, 70-BUTTON_WIDTH/2, BUTTON_WIDTH, BUTTON_WIDTH);
					this.pen.setFill(Color.RED);
					this.pen.fillText("Level "+(i+1), 60+240*i/4-23, 70+5);
					break;
				case 1:
					this.pen.fillRect(60+240*(i-1)/4-BUTTON_WIDTH/2, 190-BUTTON_WIDTH/2, BUTTON_WIDTH, BUTTON_WIDTH);
					this.pen.setFill(Color.RED);
					this.pen.fillText("Level "+(i+1), 60+240*(i-1)/4-23, 190+5);
					break;
				case 2:
					this.pen.fillRect(180+240*(i-2)/4-BUTTON_WIDTH/2, 190-BUTTON_WIDTH/2, BUTTON_WIDTH, BUTTON_WIDTH);
					this.pen.setFill(Color.RED);
					this.pen.fillText("Level "+(i+1), 180+240*(i-2)/4-23, 190+5);
					break;
				case 3:
					this.pen.fillRect(180+240*(i-3)/4-BUTTON_WIDTH/2, 70-BUTTON_WIDTH/2, BUTTON_WIDTH, BUTTON_WIDTH);
					this.pen.setFill(Color.RED);
					this.pen.fillText("Level "+(i+1), 180+240*(i-3)/4-23, 70+5);
			}
		}
	}
	
	public void openLevel(int levelNum, Stage stage, TabPane tabpane){
	
		String level = "level"+ levelNum + ".wld.sys"; 
	
		Tab LevelInfoTab = new Tab(level);
		LevelInfoTab.setOnClosed(closeEvent -> LEVELS_OPEN[getLevelIndex(level) - 1] = 0);
		GridPane grid = new GridPane();

		if (LEVELS_OPEN[getLevelIndex(level) - 1] == 1) {
			return;
		}
		LEVELS_OPEN[getLevelIndex(level) - 1] = 1;

		Button playBtn = new Button("Play level");
		playBtn.setOnAction(clickEvent -> {
			new LevelExe(PATH + ".labyrinthgame" + File.separator + "SystemLevels" + File.separator + level, level, true, "normal");
			LevelExe.setOnFinish(stage);
			stage.hide();
		});
		grid.add(playBtn, 0, 0);
		LevelInfoTab.setContent(grid);
		tabpane.getTabs().add(LevelInfoTab);
		tabpane.getSelectionModel().select(LevelInfoTab);
	}

	public PlayScreen(LabyrinthMain main) {
		final Stage stage = new Stage();
		stage.setTitle("Play");
		stage.setOnCloseRequest(event -> {
			PLAY = false;
			LEVELS_OPEN = new int[LEVELS];
		});
		TabPane tabpane = new TabPane();
		Tab tab = new Tab("Levels");
		tab.setClosable(false);
		ScrollPane sp = new ScrollPane();
		sp.requestFocus();
		Canvas canvas = new Canvas(LEVELS*60, 250);
		sp.setContent(canvas);
		
		canvas.setOnMousePressed(event -> {
			double x = event.getX(), y = event.getY();
			for (int i = 0; i < LEVELS; i++){
				switch (i%4){
					case 0:
						if (x >= 60+240*i/4-BUTTON_WIDTH/2 && y >= 70-BUTTON_WIDTH/2 && x <= 60+240*i/4+BUTTON_WIDTH/2 && y <= 70+BUTTON_WIDTH/2){
							openLevel(i+1, stage, tabpane);
						}
						break;
					case 1:
						if (x >= 60+240*(i-1)/4-BUTTON_WIDTH/2 && y >= 190-BUTTON_WIDTH/2 && x <= 60+240*(i-1)/4+BUTTON_WIDTH/2 && y <= 190+BUTTON_WIDTH/2){
							openLevel(i+1, stage, tabpane);
						}
						break;
					case 2:
						if (x >= 180+240*(i-2)/4-BUTTON_WIDTH/2 && y >= 190-BUTTON_WIDTH/2 && x <= 180+240*(i-2)/4+BUTTON_WIDTH/2 && y <= 190+BUTTON_WIDTH/2){
							openLevel(i+1, stage, tabpane);
						}
						break;
					case 3:
						if (x >= 180+240*(i-3)/4-BUTTON_WIDTH/2 && y >= 70-BUTTON_WIDTH/2 && x <= 180+240*(i-3)/4+BUTTON_WIDTH/2 && y <= 70+BUTTON_WIDTH/2){
							openLevel(i+1, stage, tabpane);
						}
						break;
				}
			}
		});
		
		this.pen = canvas.getGraphicsContext2D();
		main.startShowing();

		/*for (String level: LabyrinthMain.FILE_PATHS) {
			Button b = new Button(level);
			b.setOnAction(event -> {
				Tab LevelInfoTab = new Tab(level);
				LevelInfoTab.setOnClosed(closeEvent -> LEVELS_OPEN[getLevelIndex(level) - 1] = 0);
				GridPane grid = new GridPane();

				if (LEVELS_OPEN[getLevelIndex(level) - 1] == 1) {
					return;
				}
				LEVELS_OPEN[getLevelIndex(level) - 1] = 1;

				Button playBtn = new Button("Play level");
				playBtn.setOnAction(clickEvent -> {
					new LevelExe(PATH + ".labyrinthgame" + File.separator + "SystemLevels" + File.separator + level, level, true, "normal");
					LevelExe.setOnFinish(stage);
					stage.hide();
				});
				grid.add(playBtn, 0, 0);
				LevelInfoTab.setContent(grid);
				tabpane.getTabs().add(LevelInfoTab);
				tabpane.getSelectionModel().select(LevelInfoTab);
			});
			pane.getChildren().add(b);
		}*/
		
		drawCanvas(LEVELS);

		tab.setContent(sp);
		tabpane.getTabs().add(tab);
		
		sp.setPrefSize(stage.getWidth(), stage.getHeight());
		stage.widthProperty().addListener((obs, oldVal, newVal) -> sp.setPrefSize((double) newVal, stage.getHeight()));
		stage.heightProperty().addListener((obs, oldVal, newVal) -> sp.setPrefSize(stage.getWidth(), (double) newVal));
		
		Scene scene = new Scene(tabpane, 600, 300);
		scene.getStylesheets().add("file://" + changeSlash(PATH) + ".labyrinthgame/Editor/style.css");
		stage.setScene(scene);
		stage.show();
	}
}
