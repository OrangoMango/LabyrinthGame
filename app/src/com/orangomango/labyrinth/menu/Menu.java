package com.orangomango.labyrinth.menu;

import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.Pane;
import javafx.scene.canvas.*;
import javafx.scene.paint.Color;
import javafx.scene.image.*;
import javafx.scene.text.Font;
import javafx.scene.effect.ColorAdjust;

import java.io.*;
import java.util.*;
import java.net.URL;

import com.orangomango.labyrinth.menu.editor.Editor;
import com.orangomango.labyrinth.menu.createdlevels.HomeWindow;
import com.orangomango.labyrinth.menu.play.PlayScreen;
import com.orangomango.labyrinth.menu.editor.Selection;
import com.orangomango.labyrinth.Logger;
import static com.orangomango.labyrinth.menu.editor.Editor.PATH;
import static com.orangomango.labyrinth.menu.editor.Editor.changeSlash;

public class Menu {
	private Stage stage;
        private GraphicsContext gc;
	private double width, height;
        private final static String IMAGE_PATH = changeSlash(PATH) + ".labyrinthgame/Images/editor/";
        private final static int WIDTH = 650;
        private final static int HEIGHT = 400;
        private final static int BUTTON_WIDTH = 260;
        private final static int BUTTON_HEIGHT = 40;
        private final static int MENU_WIDTH = 350;
        private final static int MENU_HEIGHT = 300;
        private boolean isIn = false;
        private boolean openedWindow = false;

	public static String OPEN = null;

	public Menu(Stage stage) {
		this.stage = stage;
		this.stage.setTitle("Menu v" + com.orangomango.labyrinth.LabyrinthMain.VERSION);
		
		Pane layout = new Pane();
		Canvas canvas = new Canvas(WIDTH, HEIGHT);
		canvas.setOnMousePressed(e -> {
			int x = (int)Math.round(e.getX());
			int y = (int)Math.round(e.getY());
                        if (!this.openedWindow && isContainedIn(x, y, 195, 80, 455, 120)){
                           	PlayScreen screen = new PlayScreen(this.stage);
                        } else if (!this.openedWindow && isContainedIn(x, y, 195, 130, 455, 170)){
                           	startEditor(null);
                        } else if (!this.openedWindow && isContainedIn(x, y, 195, 180, 455, 220)){
				HomeWindow hw = new HomeWindow(this.stage);
                        } else if (!this.openedWindow && isContainedIn(x, y, 195, 230, 455, 270)){
				showInMenu("profile", this.gc);
                        } else if (!this.openedWindow && isContainedIn(x, y, 195, 280, 455, 320)){
                        	showInMenu("credits", this.gc);
                        } else if (this.openedWindow && isContainedIn(x, y, 155, 300, 197, 342)){
			    	this.openedWindow = false;
				updateCanvas(this.gc, -1);
			}
		});
                
                canvas.setOnMouseMoved(e -> {
			if (openedWindow) return;
			int x = (int)Math.round(e.getX());
			int y = (int)Math.round(e.getY());
                        if (isContainedIn(x, y, 195, 80, 455, 120)){
                            if (!isIn){
                                updateCanvas(this.gc, 0);
                                isIn = true;
                            }
                        } else if (isContainedIn(x, y, 195, 130, 455, 170)){
                            if (!isIn){
                                updateCanvas(this.gc, 1);
                                isIn = true;
                            }
                        } else if (isContainedIn(x, y, 195, 180, 455, 220)){
                            if (!isIn){
                                updateCanvas(this.gc, 2);
                                isIn = true;
                            }
                        } else if (isContainedIn(x, y, 195, 230, 455, 270)){
                            if (!isIn){
                                updateCanvas(this.gc, 3);
                                isIn = true;
                            }
                        } else if (isContainedIn(x, y, 195, 280, 455, 320)){
                            if (!isIn){
                                updateCanvas(this.gc, 4);
                                isIn = true;
                            }
                        } else if (isIn){
                            isIn = false;
                            updateCanvas(this.gc, -1);
                        }
                });
                
		gc = canvas.getGraphicsContext2D();
                updateCanvas(gc, -1);
		
		layout.getChildren().add(canvas);
                
		Scene scene = new Scene(layout, WIDTH, HEIGHT);
		
		this.stage.setScene(scene);
                this.stage.setResizable(false);
                
		LoadingScreen ls = new LoadingScreen(this);
        }
	
	private List<Double> getVersions(){
		List<Double> versions = new ArrayList<>();
		try {
			URL url = new URL("https://github.com/OrangoMango/LabyrinthGame/releases");
			BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
			reader.lines().forEach(line -> {
				if (line.contains("tags/v") && line.contains(".zip")){
					versions.add(Double.parseDouble(line.substring(65, line.indexOf(".zip"))));
				}
			});
			reader.close();
			return versions;
		} catch (IOException e){
			Logger.warning("Could not get latest version, connection error");
		}
		return null;
	}
        
        private void showMenu(GraphicsContext gc){
        	gc.setFill(Color.GRAY);
       		gc.fillRect(0, 0, WIDTH, HEIGHT);
        	gc.setFill(Color.YELLOW);
           	gc.fillRect((WIDTH-MENU_WIDTH)/2, (HEIGHT-MENU_HEIGHT)/2, MENU_WIDTH, MENU_HEIGHT);
        }
        
        private void updateCanvas(GraphicsContext gc, int ds){
            showMenu(gc);
            gc.setFont(Font.loadFont("file://" + changeSlash(PATH) + ".labyrinthgame/Fonts/menu_font.ttf", 20));
            for (int i = 0; i < 5; i++){
                String text = null;
                String imageName = null;
                switch (i){
                    case 0:
                        text = "PLAY";
                        imageName = "button_play.png";
                        break;
                    case 1:
                        text = "EDITOR";
                        imageName = "button_editor.png";
                        break;
                    case 2:
                        text = "MY LEVELS";
                        imageName = "button_levels.png";
                        break;
                    case 3:
                        text = "PROFILE";
                        imageName = "button_profile.png";
                        break;
                    case 4:
                        text = "CREDITS";
                        imageName = "button_credits.png";
                        break;
                }
                gc.setFill(Color.RED);
                if (i == ds){
                	ColorAdjust effect = new ColorAdjust();
                	effect.setBrightness(-0.2);
                    	gc.setEffect(effect);
                }
                gc.drawImage(new Image((new File(IMAGE_PATH+imageName)).exists() ? "file://" + IMAGE_PATH+imageName : "https://github.com/OrangoMango/LabyrinthGame/raw/main/app/lib/images/editor/"+imageName), (WIDTH-MENU_WIDTH)/2+(MENU_WIDTH-BUTTON_WIDTH)/2, 80+(BUTTON_HEIGHT+10)*i, BUTTON_WIDTH, BUTTON_HEIGHT);
                gc.setEffect(null);
                gc.setFill(Color.BLACK);
                gc.fillText(text, 275, 105+(BUTTON_HEIGHT+10)*i);
            }
        }
        
        private void showInMenu(String what, GraphicsContext gc){
		this.openedWindow = true;
        	if (what.equals("credits")){
        		showMenu(gc);
               		gc.setFill(Color.BLACK);
			gc.setFont(Font.loadFont("file://" + changeSlash(PATH) + ".labyrinthgame/Fonts/credits_font.ttf", 25));
        		gc.fillText("CREDITS\n\nIMAGES: OrangoMango\nCODE: OrangoMango\nWEB PAGE: \norangomango.github.io\n\nMIT Licence, OrangoMango\n-- February 2021 --", 160, 90);
        	} else if (what.equals("profile")){
        		showMenu(gc);
               		gc.setFill(Color.BLACK);
        		gc.fillText("Coming soon...", 225, 150);
        	}
		gc.setFill(Color.WHITE);
		gc.fillRect(155, 300, 42, 42);
		gc.drawImage(new Image("file://" + changeSlash(PATH) + ".labyrinthgame/Images/editor/back_arrow.png"), 160, 305);
        }

	private void startEditor(String param) {
		if (Editor.getCurrentFilePath() == null){
			Selection sel = new Selection(this.stage);
		} else {
			Editor editor = new Editor(param, this.stage);
		}
	}
        
        private boolean isContainedIn(int v1, int v2, int x, int y, int x1, int y1){
            if (v1 >= x && v1 <= x1 && v2 >= y && v2 <= y1){
                return true;
            } else {
                return false;
            }
        }

	public void start() {
		this.stage.show();
		Logger.info("Checking updates...");
		List<Double> versions = getVersions();
		if (versions != null && com.orangomango.labyrinth.LabyrinthMain.VERSION < versions.get(0)){
			Logger.error("Please update to the latest version! ("+com.orangomango.labyrinth.LabyrinthMain.VERSION+" < "+versions.get(0)+")");
			Alert alert = new Alert(Alert.AlertType.ERROR);
			alert.setHeaderText("Update available");
			alert.setTitle("Update available ("+com.orangomango.labyrinth.LabyrinthMain.VERSION+" < "+versions.get(0)+")");
			alert.setContentText("Please update to the latest version!\nCheck out the new release at https://github.com/OrangoMango/LabyrinthGame/releases/latest");
			alert.showAndWait();
			Platform.exit();
		}
		Logger.info("You are running the latest version");
		if (OPEN != null) {
			System.out.println("Opening requested file: " + OPEN);
			Editor editor = new Editor(OPEN, this.stage);
		}
	}

}
