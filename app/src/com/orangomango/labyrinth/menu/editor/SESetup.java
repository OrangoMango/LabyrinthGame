package com.orangomango.labyrinth.menu.editor;

import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
import javafx.geometry.HPos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.Alert;

import java.io.*;

import static com.orangomango.labyrinth.menu.editor.Editor.PATH;
import static com.orangomango.labyrinth.menu.editor.Editor.changeSlash;

public class SESetup {
  private EditableWorld world;
  private Stage stage;
  private static boolean OPEN = false;

  public SESetup(EditableWorld world, int width, int height, int[] start, int[] end) {
    if (OPEN){
    	return;
    }
    this.world = world;
    this.stage = new Stage();
    this.stage.setOnCloseRequest(event -> OPEN = false);
    OPEN = true;

    GridPane layout = new GridPane();
    layout.setHgap(10);
    layout.setVgap(10);
    layout.setPadding(new Insets(10, 10, 10, 10));

    Label label = new Label("Change start and end position:");
    Label startX = new Label("Start X:");
    Label y = new Label("Y:");
    Label endX = new Label("End X:");
    Label y1 = new Label("Y:");
    Spinner spinner1 = new Spinner(0, width-1, start[0]);
    Spinner spinner2 = new Spinner(0, height-1, start[1]);
    Spinner spinner3 = new Spinner(0, width-1, end[0]);
    Spinner spinner4 = new Spinner(0, height-1, end[1]);
    Button ok = new Button("Save changes");
    ok.setOnAction(event -> {change((int) spinner1.getValue(), (int) spinner2.getValue(), (int) spinner3.getValue(), (int) spinner4.getValue()); OPEN = false;});
    Button cancel = new Button("Cancel");
    cancel.setOnAction(event -> {OPEN = false; this.stage.close();});

    layout.add(label, 0, 0, 4, 1);
    layout.add(startX, 0, 1);
    layout.add(spinner1, 1, 1);
    layout.add(y, 2, 1);
    layout.add(spinner2, 3, 1);
    layout.add(endX, 0, 2);
    layout.add(spinner3, 1, 2);
    layout.add(y1, 2, 2);
    layout.add(spinner4, 3, 2);
    layout.add(ok, 2, 3, 2, 1);
    layout.add(cancel, 3, 3);
    GridPane.setHalignment(cancel, HPos.RIGHT);
    GridPane.setHalignment(endX, HPos.RIGHT);

    Scene scene = new Scene(layout, 500, 150);
    scene.getStylesheets().add("file://" + changeSlash(PATH) + ".labyrinthgame/Editor/style.css");
    this.stage.setScene(scene);
    this.stage.setTitle("Change start and end position");
    this.stage.show();
  }

  public void change(int sX, int sY, int eX, int eY) {
    if (sX == eX && sY == eY) {
      System.out.println("SSE Error (1)");
      Alert alert = new Alert(Alert.AlertType.ERROR);
      alert.setHeaderText("Start position is on same position of end");
      alert.setTitle("SSE Error");
      alert.setContentText(null);
      alert.showAndWait();
      return;
    }
    if (this.world.getBlockAt(sX, sY).getType() != EditableWorld.AIR || this.world.getBlockAt(eX, eY).getType() != EditableWorld.AIR) {
      System.out.println("SSE Error (2)");
      Alert alert = new Alert(Alert.AlertType.ERROR);
      alert.setHeaderText("Start or end position is on a block different from air");
      alert.setTitle("SSE Error");
      alert.setContentText(null);
      alert.showAndWait();
      return;
    }
    this.world.start[0] = sX;
    this.world.start[1] = sY;
    this.world.end[0] = eX;
    this.world.end[1] = eY;
    this.stage.close();
    this.world.draw();
    this.world.updateOnFile();
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setHeaderText("Start and end positon changed");
    alert.setTitle("Positions changed");
    alert.setContentText(null);
    alert.showAndWait();
  }
}
