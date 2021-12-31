package com.orangomango.labyrinth.menu;

import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.canvas.*;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.image.*;
import javafx.geometry.Insets;

import java.io.*;
import java.net.URL;

import static com.orangomango.labyrinth.menu.editor.Editor.PATH;
import static com.orangomango.labyrinth.menu.editor.Editor.changeSlash;

public class NewsScreen{

	private static int NEWS = 1;
	private static String[] TITLES = new String[]{"No internet connection..."};
	private static VBox[] INFOS = new VBox[]{new VBox(new Label("Connection not available\nPlease check your internet connection"))};
	private static String[] DATES = new String[]{"Now"};

	private VBox info;
	private GraphicsContext gc;
	private ScrollPane isp;
	
	static {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader((new URL("https://raw.githubusercontent.com/OrangoMango/LabyrinthGame/main/app/lib/news_data.txt")).openStream()));
			NEWS = Integer.parseInt(reader.readLine());
			TITLES = new String[NEWS];
			INFOS = new VBox[NEWS];
			DATES = new String[NEWS];
			for (int i = 0; i < NEWS; i++){
				String data = reader.readLine();
				String title = data.split("]")[0];
				title = title.substring(1, title.length());
				String description = data.split("]")[1].replace("\\n", "\n");
				TITLES[i] = title.split(";")[0];
				DATES[i] = title.split(";")[1];
				VBox out = new VBox();
				while (description.contains("{")){
					String[] part = description.split("\\{", 2);
					Label label = new Label(part[0]);
					label.setStyle("-fx-font-size: 17; -fx-text-fill: #d1e0e0");
					out.getChildren().add(label);
					String imgPath = part[1].split("\\}")[0];
					ImageView iv = new ImageView(imgPath.split(";")[0]);
					iv.setPreserveRatio(true);
					iv.setFitWidth(Integer.parseInt(imgPath.split(";")[1]));
					out.getChildren().add(iv);
					description = part[1].substring(part[1].indexOf("}")+1, part[1].length());
				}
				Label endLabel = new Label(description);
				endLabel.setStyle("-fx-font-size: 17; -fx-text-fill: #d1e0e0");
				out.getChildren().add(endLabel);
				INFOS[i] = out;
			}
		} catch (IOException e){}
	}

	public NewsScreen(Stage stage){
		stage.setTitle("LabyrinthGame News");
		GridPane layout = new GridPane();
		layout.setPadding(new Insets(5, 5, 5, 5));
		layout.setHgap(5);
		layout.setVgap(5);
		
		ScrollPane sp = new ScrollPane();
		sp.setPrefWidth(485);
		sp.setFitToHeight(true);
		
		Canvas canvas = new Canvas(450, NEWS*(50+10)+20);
		canvas.setOnMousePressed(event -> {
			double x = event.getX();
			double y = event.getY();
			if (x > 10 && x < 480){
				int found = -1;
				for (int i = 0; i < NEWS; i++){
					if (y > 10+i*(50+10) && y < 10+i*(50+10)+50){
						found = i;
						break;
					}
				}
				if (found >= 0){
					this.info = INFOS[found];
					this.isp.setContent(this.info);
					this.isp.setVvalue(0);
					updateSelection(this.gc, found);
				}
			}
		});
		this.gc = canvas.getGraphicsContext2D();
		updateSelection(this.gc, 0);
		
		sp.setContent(canvas);
		
		this.info = INFOS[0];
		this.isp = new ScrollPane();
		this.isp.setPrefWidth(450);
		this.isp.setPrefHeight(450);
		this.isp.setContent(this.info);
		
		Button exit = new Button("Back");
		exit.setOnAction(e -> {
			Menu menu = new Menu(stage);
		});
		
		layout.add(sp, 0, 0, 1, 2);
		layout.add(this.isp, 1, 0);
		layout.add(exit, 0, 2);
		Scene scene = new Scene(layout, 950, 500);
		scene.getStylesheets().add("file://" + changeSlash(PATH) + ".labyrinthgame/Editor/style.css");
		stage.setScene(scene);
	}
	
	private void updateSelection(GraphicsContext gc, int s){
		gc.clearRect(0, 0, 450, NEWS*(50+10)+20);
		gc.setFill(Color.YELLOW);
		gc.fillRect(0, 0, 450, NEWS*(50+10)+20);
		gc.setFont(Font.loadFont("file://" + changeSlash(PATH) + ".labyrinthgame/Fonts/news_font.ttf", 22));
		gc.setStroke(Color.BLACK);
		for (int i = 0; i < NEWS; i++){
			gc.setFill(i == s ? Color.RED : Color.LIME);
			gc.fillRect(10, 10+i*(50+10), 430, 50);
			gc.strokeText(TITLES[i], 20, 10+i*(50+10)+35);
		}
		for (int i = 0; i < DATES.length; i++){
			gc.setStroke(i == s ? Color.WHITE : Color.GRAY);
			gc.setFont(Font.loadFont("file://" + changeSlash(PATH) + ".labyrinthgame/Fonts/news_font.ttf", 12));
			gc.strokeText(DATES[i], 360, 10+i*(50+10)+13);
		}
	}
}
