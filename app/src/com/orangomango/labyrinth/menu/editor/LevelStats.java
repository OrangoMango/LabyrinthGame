package com.orangomango.labyrinth.menu.editor;

import javafx.scene.canvas.*;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;

import com.orangomango.labyrinth.*;
import static com.orangomango.labyrinth.menu.editor.Editor.PATH;
import com.orangomango.labyrinth.menu.editor.Editor;

public class LevelStats {
	private GraphicsContext pen;
	private World world;
	
	private static final int BAR_WIDTH = 50;
	private static final int BAR_HEIGHT = 10;
	public static final int ICON_SIZE = 10;
	public static final double GAP_FACTOR = 1.3;
	
	public LevelStats(World w, GraphicsContext pen){
		this.pen = pen;
		this.world = w;
	}
	
	public void draw(double x, double y){
		pen.drawImage(new Image("file://" + Editor.changeSlash(PATH) + ".labyrinthgame/Images/entities/health.png"), x, y, ICON_SIZE, ICON_SIZE);
		pen.drawImage(new Image("file://" + Editor.changeSlash(PATH) + ".labyrinthgame/Images/entities/oxygen.png"), x, y+ICON_SIZE*GAP_FACTOR, ICON_SIZE, ICON_SIZE);
		int health = this.world.getPlayer().getHealth();
		
		/* RED - ORANGE - YELLOW - GREEN - LIME
		 *
		 * 0 - 20 RED
		 * 21 - 40 ORANGE
		 * 41 - 60 YELLOW
		 * 61 - 80 GREEN
		 * 81 - 100 LIME
		 */
		 
		if (health <= 20){
			this.pen.setFill(Color.RED);
		} else if  (health <= 40){
			this.pen.setFill(Color.ORANGE);
		} else if  (health <= 60){
			this.pen.setFill(Color.YELLOW);
		} else if  (health <= 80){
			this.pen.setFill(Color.GREEN);
		} else if  (health <= 100){
			this.pen.setFill(Color.LIME);
		} else {
			this.pen.setFill(Color.BLACK);
		}
		
		this.pen.fillRect(x+ICON_SIZE*GAP_FACTOR, y, (int)Math.round(health/100.0*BAR_WIDTH), BAR_HEIGHT);
		this.pen.strokeRect(x+ICON_SIZE*GAP_FACTOR, y, BAR_WIDTH, BAR_HEIGHT);
		
		int ox = this.world.getPlayer().getOx();
		this.pen.setFill(Color.CYAN);
		this.pen.fillRect(x+ICON_SIZE*GAP_FACTOR, y+ICON_SIZE*GAP_FACTOR, (int)Math.round(ox/100.0*BAR_WIDTH), BAR_HEIGHT);
		this.pen.strokeRect(x+ICON_SIZE*GAP_FACTOR, y+ICON_SIZE*GAP_FACTOR, BAR_WIDTH, BAR_HEIGHT);
	}
}

