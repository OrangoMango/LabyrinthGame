/**
   Labirinth game - world class
   @author OrangoMango
*/

package com.orangomango.labyrinth;

import java.io.*;
import java.util.Arrays;

import javafx.scene.canvas.*;
import javafx.stage.Stage;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class World {
	protected Block[][] world;
	protected String filePath;
	public int height, width;
	public int[] start, end;
	private GraphicsContext pen;
	private Player player;
	protected Canvas canvas;

	public final static String WALL = "wall";
	public final static String AIR = "air";
	public final static String VOID = "void";

	public final static int BLOCK_WIDTH = 50;

	public World(String path) {
		filePath = path;
		world = readWorld(filePath);
	}

	public void setPen(GraphicsContext pen) {
		this.pen = pen;
	}

	public void setPlayer(Player pl) {
		this.player = pl;
	}

	public void setCanvas(Canvas canvas) {
		this.canvas = canvas;
	}

	public void changeToWorld(String path) {
		filePath = path;
		world = readWorld(filePath);
		try {
			this.canvas.setHeight(this.height * BLOCK_WIDTH);
			this.canvas.setWidth(this.width * BLOCK_WIDTH);
		} catch (NullPointerException e) {
			Logger.warning("World canvas is null");
		}
		try {
			this.player.setX(start[0]);
			this.player.setY(start[1]);
		} catch (NullPointerException e) {
			Logger.warning("World player is null");
		}
		update(0, 0, 0, 0);
	}

	public void update(int x, int y, int x1, int y1) {
		try {
			this.pen.setFill(Color.WHITE);
			if (x == 0 && y == 0 && x1 == 0 && y1 == 0){
				this.pen.fillRect(0, 0, this.width * BLOCK_WIDTH, this.height * BLOCK_WIDTH);
				draw();
			} else {
				this.pen.fillRect(0, 0, (x+y) * BLOCK_WIDTH, (x+y) * BLOCK_WIDTH);
				draw(x, y, x1, y1);
			}
		} catch (NullPointerException e) {
			Logger.warning("World pen is null");
		}
	}

	private Block[][] readWorld(String path) {
		File file = new File(path);
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));

			// Get world width and height from file
			String data = reader.readLine();
			this.width = Integer.parseInt(data.split("x")[0]);
			this.height = Integer.parseInt(data.split("x")[1]);

			// Get world layout
			String wData = reader.readLine();
			Block[][] Fworld = parseWorldData(wData, height, width);

			String startData = reader.readLine();
			String endData = reader.readLine();

			start = configureFromString(startData);
			end = configureFromString(endData);

			reader.close();
			return Fworld;

		} catch (IOException ex) {
			Logger.error("Could not read world");
			return null;
		}
	}

	private int[] configureFromString(String data) {
		String[] split = data.split(",");
		int[] output = new int[2];
		int x = 0;

		for (String i: split) {
			output[x] = Integer.parseInt(i);
			x++;
		}

		return output;
	}

	/**
	  Parse give string from file and return an array
	  @param data - string with all file data
	  @param h - world height
	  @param w - world width
	*/
	private Block[][] parseWorldData(String data, int h, int w) {
		String[] current = data.split(",");
		Block[][] output = new Block[h][w];

		int iterator = 0;
		int counter = 0;
		for (String i: current) {
			Block[] x = new Block[w];
			int it2 = 0;

			if (iterator + w > current.length) { // If itertor is bigger than the list length then stop
				break;
			}

			for (String v: Arrays.copyOfRange(current, iterator, iterator + w)) { // Parse every array range in "width" length parts
				x[it2] = Block.fromInt(Integer.parseInt(v), it2, counter);
				it2++;
			}
			output[counter] = x;
			iterator += w;
			counter++;
		}
		return output;
	}

	public void draw() {
		for (Block[] blocks: world) {
			for (Block block: blocks) {
				block.draw(this.pen);
			}
		}
		this.player.draw(this.pen);
		drawPoints(0, 0);
	}
	
	public void draw(int x, int y, int x1, int y1){
                int coux = 0;
                int couy = 0;
                for (int cy = y; cy <= y1; cy++){
                    for (int cx = x; cx <= x1; cx++){
                        Block b = getBlockAt(cx, cy);
                        if (b != null){
                        	b.draw(this.pen, coux, couy);
                        } else {
                        	new Block(VOID, coux, couy).draw(this.pen);
                        }
                        coux++;
                    }
                    coux = 0;
                    couy++;
                }
		if ((start[0] >= x && start[0] <= x1) && (start[1] >= y && start[1] <= y1) && (end[0] >= x && end[0] <= x1) && (end[1] >= y && end[1] <= y1)){
			drawPoints(x, y);
		}
		if ((this.player.getX() >= x && this.player.getX() <= x1) && (this.player.getY() >= y && this.player.getY() <= y1)){
			this.player.draw(this.pen, this.player.getX()-x, this.player.getY()-y);
		}
	}

	private void drawPoints(int x, int y) {
		this.pen.setStroke(Color.GREEN);
		this.pen.setFont(new Font("Arial", 35));
		this.pen.strokeText("S", (start[0] - x) * BLOCK_WIDTH + 5, (start[1] - y) * BLOCK_WIDTH + 35);
		this.pen.strokeText("E", (end[0] - x) * BLOCK_WIDTH + 5, (end[1] - y) * BLOCK_WIDTH + 35);
	}

	public Block getBlockAt(int x, int y) {
		try {
			Block item = world[y][x];
			return item;
		} catch (ArrayIndexOutOfBoundsException e) {
			return null;
		}
	}

	public Block[] getXRow(int y) {
		return world[y];
	}

	public Block[] getYRow(int x) {
		Block[] output = new Block[height];
		int counter = 0;
		for (Block[] i: world) {
			output[counter] = i[x];
			counter++;
		}

		return output;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("World:\n");
		for (Block[] x: world) {
			for (Block b: x) {
				builder.append("|" + b + "|").append(" ");
			}
			builder.append("\n");
		}
		builder.append(String.format("Width: %s, Height: %s.\nStart at: %s, End at: %s", width, height, Arrays.toString(start), Arrays.toString(end)));

		return builder.toString();
	}
}
