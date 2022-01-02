package com.orangomango.labyrinth.menu.play;

import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.geometry.Insets;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.control.Button;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tab;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.canvas.*;
import javafx.scene.image.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import java.io.*;

import com.orangomango.labyrinth.LabyrinthMain;
import com.orangomango.labyrinth.World;
import com.orangomango.labyrinth.Player;
import com.orangomango.labyrinth.menu.Menu;
import com.orangomango.labyrinth.menu.editor.LevelExe;
import static com.orangomango.labyrinth.menu.createdlevels.HomeWindow.PREVIEW_BLOCK_WIDTH;
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
			switch (i % 4){
				case 0:
					this.pen.drawImage(new Image("file://" + changeSlash(PATH) + ".labyrinthgame/Images/editor/level_select.png"), 60+240*i/4-BUTTON_WIDTH/2, 70-BUTTON_WIDTH/2, BUTTON_WIDTH, BUTTON_WIDTH);
					this.pen.setFill(Color.WHITE);
					this.pen.fillText(""+(i+1), 60+240*i/4, 70+5);
					break;
				case 1:
					this.pen.drawImage(new Image("file://" + changeSlash(PATH) + ".labyrinthgame/Images/editor/level_select.png"), 60+240*(i-1)/4-BUTTON_WIDTH/2, 190-BUTTON_WIDTH/2, BUTTON_WIDTH, BUTTON_WIDTH);
					this.pen.setFill(Color.WHITE);
					this.pen.fillText(""+(i+1), 60+240*(i-1)/4, 190+5);
					break;
				case 2:
					this.pen.drawImage(new Image("file://" + changeSlash(PATH) + ".labyrinthgame/Images/editor/level_select.png"), 180+240*(i-2)/4-BUTTON_WIDTH/2, 190-BUTTON_WIDTH/2, BUTTON_WIDTH, BUTTON_WIDTH);
					this.pen.setFill(Color.WHITE);
					this.pen.fillText(""+(i+1), 180+240*(i-2)/4, 190+5);
					break;
				case 3:
					this.pen.drawImage(new Image("file://" + changeSlash(PATH) + ".labyrinthgame/Images/editor/level_select.png"), 180+240*(i-3)/4-BUTTON_WIDTH/2, 70-BUTTON_WIDTH/2, BUTTON_WIDTH, BUTTON_WIDTH);
					this.pen.setFill(Color.WHITE);
					this.pen.fillText(""+(i+1), 180+240*(i-3)/4, 70+5);
			}
		}
	}
	
	public void openLevel(int levelNum, Stage stage, TabPane tabpane){
	
		String level = "level"+ levelNum + ".wld.sys"; 
		String lPath = PATH + ".labyrinthgame" + File.separator + "SystemLevels" + File.separator + level;
	
		Tab LevelInfoTab = new Tab(level);
		LevelInfoTab.setOnClosed(closeEvent -> LEVELS_OPEN[getLevelIndex(level) - 1] = 0);
		GridPane grid = new GridPane();
		grid.setPadding(new Insets(10, 10, 10, 10));
		grid.setVgap(5);
		grid.setHgap(5);

		if (LEVELS_OPEN[getLevelIndex(level) - 1] == 1) {
			return;
		}
		LEVELS_OPEN[getLevelIndex(level) - 1] = 1;
		
		World temp = new World(lPath);
		temp.previewMode = true;
		World.BLOCK_WIDTH = PREVIEW_BLOCK_WIDTH;
		Canvas Tcanvas = new Canvas(temp.width*World.BLOCK_WIDTH, temp.height*World.BLOCK_WIDTH);
		GraphicsContext pen = Tcanvas.getGraphicsContext2D();
		temp.setPen(pen);
		temp.setPlayer(new Player(temp.start[0], temp.start[1], temp));
		temp.draw();
		World.BLOCK_WIDTH = World.DEFAULT_BLOCK_WIDTH;
		
		Label information = new Label(temp.getWorldInformation().replace("\\n", "\n"));

		Button playBtn = new Button();
		playBtn.setGraphic(new ImageView(new Image("file://" + changeSlash(PATH) + ".labyrinthgame/Images/editor/menu_run.png")));
		playBtn.setTooltip(new Tooltip("Play this level"));
		playBtn.setOnAction(clickEvent -> {
			new LevelExe(lPath, level, true, "normal");
			LevelExe.setOnFinish(stage);
			stage.hide();
		});
		grid.add(information, 0, 0);
		grid.add(playBtn, 1, 0);
		grid.add(Tcanvas, 0, 1);
		ScrollPane sp = new ScrollPane(grid);
		sp.setFitToHeight(true);
		sp.setFitToWidth(true);
		LevelInfoTab.setContent(sp);
		tabpane.getTabs().add(LevelInfoTab);
		tabpane.getSelectionModel().select(LevelInfoTab);
	}

	public PlayScreen(Stage stage) {
		stage.setTitle("Play");
		TabPane selectionMode = new TabPane();
		selectionMode.getStyleClass().add("floating");
		selectionMode.setTabMaxWidth(32);
		selectionMode.setTabMaxHeight(32);
		Tab playLevels = new Tab("Levels");
		playLevels.setStyle("-fx-background-color: transparent");
		playLevels.setGraphic(new ImageView(new Image("file://" + changeSlash(PATH) + ".labyrinthgame/Images/editor/tab_levels_on.png")));
		playLevels.setClosable(false);
		Tab playArcade = new Tab("Arcade");
		playArcade.setStyle("-fx-background-color: transparent");
		playArcade.setGraphic(new ImageView(new Image("file://" + changeSlash(PATH) + ".labyrinthgame/Images/editor/tab_arcade_off.png")));
		playArcade.setClosable(false);
                VBox arcadeContent = new VBox();
		arcadeContent.setBackground(new Background(new BackgroundImage(new Image("file://" + changeSlash(PATH) + ".labyrinthgame/Images/editor/background_arcade.png"), BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, null, null)));
		playArcade.setContent(arcadeContent);
                selectionMode.getTabs().addAll(playLevels, playArcade);

		playLevels.setOnSelectionChanged(e -> {
			playLevels.setGraphic(new ImageView(new Image("file://" + changeSlash(PATH) + ".labyrinthgame/Images/editor/tab_levels_on.png")));
			playArcade.setGraphic(new ImageView(new Image("file://" + changeSlash(PATH) + ".labyrinthgame/Images/editor/tab_arcade_off.png")));
		});
		
		playArcade.setOnSelectionChanged(e -> {
			playLevels.setGraphic(new ImageView(new Image("file://" + changeSlash(PATH) + ".labyrinthgame/Images/editor/tab_levels_off.png")));
			playArcade.setGraphic(new ImageView(new Image("file://" + changeSlash(PATH) + ".labyrinthgame/Images/editor/tab_arcade_on.png")));
		});

		TabPane tabpane = new TabPane();
        tabpane.getStyleClass().add("floating");
		VBox lvls = new VBox();
		lvls.setPadding(new Insets(20, 20, 20, 20));
		lvls.getChildren().add(tabpane);
		playLevels.setContent(lvls);
		lvls.setBackground(new Background(new BackgroundImage(new Image("file://" + changeSlash(PATH) + ".labyrinthgame/Images/editor/background_levels.png"), BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, null, null)));
		Tab tab = new Tab("Demo");
		tab.setClosable(false);
		ScrollPane sp = new ScrollPane();
		sp.requestFocus();
		Canvas canvas = new Canvas(LEVELS*60, 250);
		sp.setContent(canvas);
		tab.setContent(sp);
		
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
		
		Scene scene = new Scene(vbox, 700, 430);
		scene.getStylesheets().addAll("file://" + changeSlash(PATH) + ".labyrinthgame/Editor/style.css", "file://" + changeSlash(PATH) + ".labyrinthgame/Editor/play_style.css");
		stage.setScene(scene);
		
		stage.show();
	}
}
