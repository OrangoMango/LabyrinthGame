package com.orangomango.labyrinth.menu.play;

import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.geometry.Insets;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tab;
import javafx.scene.control.ScrollPane;
import javafx.scene.canvas.*;
import javafx.scene.image.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import java.io.*;

import com.orangomango.labyrinth.LabyrinthMain;
import com.orangomango.labyrinth.menu.Menu;
import com.orangomango.labyrinth.menu.editor.LevelExe;
import static com.orangomango.labyrinth.menu.editor.Editor.PATH;
import static com.orangomango.labyrinth.menu.editor.Editor.changeSlash;
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
                this.pen.setFont(Font.loadFont("file://" + changeSlash(PATH) + ".labyrinthgame/Fonts/play_font.ttf", 24));
                this.pen.setTextAlign(TextAlignment.CENTER);
		for (int i = 0; i < num; i++){
			this.pen.setFill(Color.GREEN);
			switch (i % 4){
				case 0:
					this.pen.fillRect(60+240*i/4-BUTTON_WIDTH/2, 70-BUTTON_WIDTH/2, BUTTON_WIDTH, BUTTON_WIDTH);
					this.pen.setFill(Color.WHITE);
					this.pen.fillText(""+(i+1), 60+240*i/4, 70+5);
					break;
				case 1:
					this.pen.fillRect(60+240*(i-1)/4-BUTTON_WIDTH/2, 190-BUTTON_WIDTH/2, BUTTON_WIDTH, BUTTON_WIDTH);
					this.pen.setFill(Color.WHITE);
					this.pen.fillText(""+(i+1), 60+240*(i-1)/4, 190+5);
					break;
				case 2:
					this.pen.fillRect(180+240*(i-2)/4-BUTTON_WIDTH/2, 190-BUTTON_WIDTH/2, BUTTON_WIDTH, BUTTON_WIDTH);
					this.pen.setFill(Color.WHITE);
					this.pen.fillText(""+(i+1), 180+240*(i-2)/4, 190+5);
					break;
				case 3:
					this.pen.fillRect(180+240*(i-3)/4-BUTTON_WIDTH/2, 70-BUTTON_WIDTH/2, BUTTON_WIDTH, BUTTON_WIDTH);
					this.pen.setFill(Color.WHITE);
					this.pen.fillText(""+(i+1), 180+240*(i-3)/4, 70+5);
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

	public PlayScreen(Stage stage) {
		stage.setTitle("Play levels");
		TabPane selectionMode = new TabPane();
		/*selectionMode.tabMinWidthProperty().set(32);
        	selectionMode.tabMaxWidthProperty().set(32);
		selectionMode.tabMinHeightProperty().set(32);
        	selectionMode.tabMaxHeightProperty().set(32);*/
		Tab playLevels = new Tab("Levels");
		//playLevels.setGraphic(new ImageView(new Image("file:///home/paul/.labyrinthgame/Images/editor/pattern_add.png")));
		playLevels.setClosable(false);
		Tab playArcade = new Tab("Arcade");
		playArcade.setClosable(false);
		selectionMode.getTabs().addAll(playLevels, playArcade);

		TabPane tabpane = new TabPane();
		playLevels.setContent(tabpane);
		Tab tab = new Tab("Demo");
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
		//main.startShowing();

		drawCanvas(LEVELS);

		tab.setContent(sp);
		tabpane.getTabs().add(tab);
		
		sp.setFitToHeight(true);
		stage.widthProperty().addListener((obs, oldVal, newVal) -> sp.setPrefSize((double) newVal, stage.getHeight()));
		stage.heightProperty().addListener((obs, oldVal, newVal) -> sp.setPrefSize(stage.getWidth(), (double) newVal));
		
		VBox vbox = new VBox();
		vbox.setSpacing(5);
		vbox.setPadding(new Insets(5, 5, 5, 5));
		Button exit = new Button("Back to menu");
                exit.setGraphic(new ImageView(new Image("file://" + changeSlash(PATH) + ".labyrinthgame/Images/editor/back_arrow.png")));
		exit.setOnAction(e -> {
			LEVELS_OPEN = new int[LEVELS];
			Menu m = new Menu(stage);
		});
		
		vbox.getChildren().addAll(selectionMode, exit);
		
		Scene scene = new Scene(vbox, 650, 380);
		scene.getStylesheets().add("file://" + changeSlash(PATH) + ".labyrinthgame/Editor/style.css");
		stage.setScene(scene);
		stage.show();
	}
}
