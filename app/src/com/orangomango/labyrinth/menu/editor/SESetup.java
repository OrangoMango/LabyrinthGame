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

public class SESetup{
  private EditableWorld world;
  private Stage stage;

	public SESetup(EditableWorld world, int width, int height, int[] start, int[]end){
    this.world = world;
    this.stage = new Stage();

    GridPane layout = new GridPane();
    layout.setHgap(10);
    layout.setVgap(10);
    layout.setPadding(new Insets(10, 10, 10, 10));

    Label label = new Label("Change start and end position:");
    Label startX = new Label("Start X:");
    Label y = new Label("Y:");
    Label endX = new Label("End X:");
    Label y1 = new Label("Y:");
    Spinner spinner1 = new Spinner(0, width, start[0]);
    Spinner spinner2 = new Spinner(0, height, start[1]);
    Spinner spinner3 = new Spinner(0, width, end[0]);
    Spinner spinner4 = new Spinner(0, height, end[1]);
    Button ok = new Button("Save changes");
    ok.setOnAction(event -> change((int)spinner1.getValue(), (int)spinner2.getValue(), (int)spinner3.getValue(), (int)spinner4.getValue()));
    Button cancel = new Button("Cancel");
    cancel.setOnAction(event -> this.stage.hide());

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

    this.stage.setScene(new Scene(layout, 500, 150));
    this.stage.setTitle("Change start and end position");
    this.stage.show();
  }

  public void change(int sX, int sY, int eX, int eY){
    this.world.start[0] = sX;
    this.world.start[1] = sY;
    this.world.end[0] = eX;
    this.world.end[1] = eY;
    this.stage.hide();
    this.world.draw();
    this.world.updateOnFile();
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setHeaderText("Start and end positon changed");
    alert.setTitle("Positions changed");
    alert.setContentText(null);
    alert.showAndWait();
  }
}
