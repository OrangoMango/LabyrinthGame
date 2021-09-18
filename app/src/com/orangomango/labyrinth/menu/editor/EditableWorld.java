package com.orangomango.labyrinth.menu.editor;

import javafx.scene.canvas.*;

import java.io.*;

import com.orangomango.labyrinth.World;
import com.orangomango.labyrinth.Block;
import com.orangomango.labyrinth.Logger;
import com.orangomango.labyrinth.engineering.EngBlock;

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
					writer.write(Integer.toString(block.toInt())+((block.getInfo() == null) ? "" : ":"+block.getInfo()));
					if (counter + 1 != this.height * this.width) {
						writer.write(",");
					}
					counter++;
				}
			}
			writer.newLine();
			writer.write(start[0] + "," + start[1] + "\n");
			writer.write(end[0] + "," + end[1]);
			if (getEngineeringWorld() != null){
				writer.write("\nengineering_mode\n");
				for (EngBlock[] bArr: getEngineeringWorld().getWorld()) {
					for (EngBlock block: bArr) {
						writer.write(Integer.toString(block.toInt())+((block.getInfo() == null) ? "" : ":"+block.getInfo()));
						if (counter + 1 != getEngineeringWorld().getWorld().length*getEngineeringWorld().getWorld()[0].length) {
							writer.write(",");
						}
						counter++;
					}
				}
			}
			writer.close();
		} catch (IOException e) {
			Logger.error(e.getMessage());
		}
		changeToWorld(this.filePath);
	}

	public void setBlockOn(EditableBlock block) {
		this.world[block.getY()][block.getX()] = new Block(block.getType(), block.getX(), block.getY(), block.getInfo());
	}
	
	public EngBlock[][] getEngineeringClone(){
		EngBlock[][] output = new EngBlock[this.height][this.width];
		for (int y = 0; y < this.height; y++){
			for (int x = 0; x < this.width; x++){
				if (getEngineeringWorld().getBlockAt(x, y) != null){
					output[y][x] = getEngineeringWorld().getBlockAt(x, y);
				} else {
					output[y][x] = EngBlock.fromInt(0, x, y, null);
				}
			}
		}
		return output;
		//return getEngineeringWorld().getWorld();
	}
	
	public void updateWalls(){
		for (Block[] blockRow : this.world){
			for (Block b : blockRow){
				if (b.getType() == EditableWorld.WALL){
					if (this.getBlockAt(b.getX(), b.getY()-1) != null){
						if (this.getBlockAt(b.getX(), b.getY()-1).getType().equals(WALL)){
						    this.getBlockAt(b.getX(), b.getY()-1).addConn("s");
						} else {
							b.removeConn("n");
						}
					} else {
						b.removeConn("n");
					}
					if (this.getBlockAt(b.getX()+1, b.getY()) != null){
						if (this.getBlockAt(b.getX()+1, b.getY()).getType().equals(WALL)){
						    this.getBlockAt(b.getX()+1, b.getY()).addConn("w");
						} else {
							b.removeConn("e");
						}
					} else {
						b.removeConn("e");
					}
					if (this.getBlockAt(b.getX(), b.getY()+1) != null){
						if (this.getBlockAt(b.getX(), b.getY()+1).getType().equals(WALL)){
						    this.getBlockAt(b.getX(), b.getY()+1).addConn("n");
						} else {
							b.removeConn("s");
						}
					} else {
						b.removeConn("s");
					}
					if (this.getBlockAt(b.getX()-1, b.getY()) != null){
						if (this.getBlockAt(b.getX()-1, b.getY()).getType().equals(WALL)){
						    this.getBlockAt(b.getX()-1, b.getY()).addConn("e");
						} else {
							b.removeConn("w");
						}
					} else {
						b.removeConn("w");
					}
				}
			}
		}
		updateOnFile();
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
		getEngineeringWorld().setWorld(getEngineeringClone());
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
		getEngineeringWorld().setWorld(getEngineeringClone());
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
		
		for (Block b : this.world[newArray.length]){
			if (b.getType().equals(PORTAL) && !b.getInfo().equals("NoPointSet")){
				String[] data = b.getInfo().split("#")[1].split(" ");
				getBlockAt(Integer.parseInt(data[0]), Integer.parseInt(data[1])).setInfo("NoPointSet");
			}
		}
		
		this.world = newArray;
		this.height--;
		getEngineeringWorld().setWorld(getEngineeringClone());
		updateWalls();
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
		getEngineeringWorld().setWorld(getEngineeringClone());
		updateWalls();
		updateOnFile();
		return true;
	}
}
