package com.orangomango.labyrinth.menu.editor;

import javafx.scene.canvas.*;

import java.io.*;

import com.orangomango.labyrinth.World;
import com.orangomango.labyrinth.Block;
import com.orangomango.labyrinth.Logger;

public class EditableWorld extends World {
	public EditableWorld(String path) {
		super(path);
	}

	public Block getBlockAtCoord(int x, int y) {
		int x1 = x / World.BLOCK_WIDTH;
		int y1 = y / World.BLOCK_WIDTH;
		return super.getBlockAt(x1, y1);
	}

	public void updateOnFile() {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(this.filePath));
			writer.write(this.width + "x" + this.height + "\n");
			int counter = 0;
			for (Block[] bArr: this.world) {
				for (Block block: bArr) {
					writer.write(Integer.toString(block.toInt())+"$"+block.getInfo());
					if (counter + 1 != this.height * this.width) {
						writer.write(",");
					}
					counter++;
				}
			}
			writer.newLine();
			writer.write(start[0] + "," + start[1] + "\n");
			writer.write(end[0] + "," + end[1]);
			writer.close();
		} catch (IOException e) {
			Logger.error(e.getMessage());
		}
		changeToWorld(this.filePath);
	}

	public void setBlockOn(EditableBlock block) {
		this.world[block.getY()][block.getX()] = new Block(block.getType(), block.getX(), block.getY(), block.getInfo());
	}

	public void addRow() {
		Block[][] newArray = new Block[this.height + 1][this.width];
		int counter = 0;
		for (Block[] bl: this.world) {
			newArray[counter] = bl;
			counter++;
		}
		Block[] newRow = new Block[this.width];
		for (int i = 0; i<this.width; i++) {
			newRow[i] = Block.fromInt(0, i, counter, null);
		}
		newArray[counter] = newRow;
		this.world = newArray;
		this.height++;
		updateOnFile();
	}

	public void addColumn() {
		Block[][] newArray = new Block[this.height][this.width + 1];
		int counter = 0;
		for (Block[] bl: this.world) {
			Block[] newRow = new Block[this.width + 1];
			int c = 0;
			for (Block b: bl) {
				newRow[c] = b;
				c++;
			}
			newRow[c] = Block.fromInt(0, this.width + 1, counter, null);
			newArray[counter] = newRow;
			counter++;
		}
		this.world = newArray;
		this.width++;
		updateOnFile();
	}

	public boolean removeRow() {
		Block[][] newArray = new Block[this.height - 1][this.width];
		int counter = 0;
		for (Block[] bl: this.world) {
			if (counter<this.height - 1) {
				newArray[counter] = bl;
				counter++;
			}
		}
		if (this.height - 1 == start[1] || this.height - 1 == end[1]) {
			return false;
		}
		this.world = newArray;
		this.height--;
		updateOnFile();
		return true;
	}

	public boolean removeColumn() {
		Block[][] newArray = new Block[this.height][this.width - 1];
		int counter = 0;
		for (Block[] bl: this.world) {
			Block[] newRow = new Block[this.width - 1];
			int c = 0;
			for (Block b: bl) {
				if (this.width - 1 == start[0] || this.width - 1 == end[0]) {
					return false;
				}
				if (c<this.width - 1) {
					newRow[c] = b;
				}
				c++;
			}
			newArray[counter] = newRow;
			counter++;
		}
		this.world = newArray;
		this.width--;
		updateOnFile();
		return true;
	}
}
