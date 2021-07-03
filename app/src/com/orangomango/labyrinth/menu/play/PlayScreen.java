package com.orangomango.labyrinth.menu.play;

import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.geometry.Insets;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.GridPane;
import javafx.scene.control.Button;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tab;

import java.io.*;

import com.orangomango.labyrinth.LabyrinthMain;
import com.orangomango.labyrinth.menu.editor.LevelExe;
import static com.orangomango.labyrinth.menu.editor.Editor.PATH;
import static com.orangomango.labyrinth.menu.Menu.PLAY;
import static com.orangomango.labyrinth.menu.LoadingScreen.LEVELS;

public class PlayScreen {

  public static int[] LEVELS_OPEN = new int[LEVELS];

  private static int getLevelIndex(String level) {
    return Integer.parseInt(Character.toString(level.charAt(5)));
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
    TilePane pane = new TilePane();
    pane.setPadding(new Insets(7, 7, 7, 7));
    pane.setHgap(10);
    pane.setVgap(10);
    main.startShowing();

    for (String level: LabyrinthMain.FILE_PATHS) {
      Button b = new Button(level);
      b.setOnAction(event -> {
        Tab LevelInfoTab = new Tab(level);
        LevelInfoTab.setOnClosed(closeEvent -> LEVELS_OPEN[getLevelIndex(level)-1] = 0);
        GridPane grid = new GridPane();
        
        if (LEVELS_OPEN[getLevelIndex(level)-1] == 1) {
          return;
        }
        LEVELS_OPEN[getLevelIndex(level)-1] = 1;

        Button playBtn = new Button("Play level");
        playBtn.setOnAction(clickEvent -> {
          new LevelExe(PATH + ".labyrinthgame" + File.separator + "SystemLevels" + File.separator + level, level, true);
          LevelExe.setOnFinish(stage);
          stage.hide();
        });
        grid.add(playBtn, 0, 0);
        LevelInfoTab.setContent(grid);
        tabpane.getTabs().add(LevelInfoTab);
        tabpane.getSelectionModel().select(LevelInfoTab);
      });
      pane.getChildren().add(b);
    }

    tab.setContent(pane);
    tabpane.getTabs().add(tab);
    stage.setScene(new Scene(tabpane, 600, 300));
    stage.show();
  }
}
