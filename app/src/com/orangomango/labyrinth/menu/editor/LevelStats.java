package com.orangomango.labyrinth.menu.editor;

import javafx.scene.canvas.*;
import javafx.scene.paint.Color;

import com.orangomango.labyrinth.*;

public class LevelStats {
	private GraphicsContext pen;
	private World world;
	
	public static final int HEIGHT = 70, WIDTH = 250;
	
	public LevelStats(World w, GraphicsContext pen){
		this.pen = pen;
		this.world = w;
	}
	
	public void update(){
		this.pen.clearRect(0, 0, WIDTH, HEIGHT);
		draw();
	}
	
	public void draw(){
		this.pen.setStroke(Color.BLACK);
		this.pen.setLineWidth(2);
		this.pen.strokeRect(0, 0, WIDTH, HEIGHT);
		this.pen.setLineWidth(1);
		this.pen.strokeRect(10, 10, 20, 20);
		this.pen.strokeRect(10, 40, 20, 20);
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
		
		this.pen.fillRect(40, 10, (int)Math.round(health/100.0*175), 20);
		this.pen.strokeRect(40, 10, 175, 20);
		
		int ox = this.world.getPlayer().getOx();
		this.pen.setFill(Color.CYAN);
		this.pen.fillRect(40, 40, (int)Math.round(ox/100.0*175), 20);
		this.pen.strokeRect(40, 40, 175, 20);
	}
}
