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

	public void setBlockOn(EditableBlock block) {
		this.world[block.getY()][block.getX()] = new Block(block.getType(), block.getX(), block.getY(), block.getInfo());
	}
	
	// private (addRow, removeRow, addColumn, removeColumn) for engineering mode
	
	public void addERow(){
		EngBlock[][] newArray = new EngBlock[this.getEngineeringWorld().getHeight() + 1][this.getEngineeringWorld().getWidth()];
		int counter = 0;
		for (EngBlock[] bl: this.getEngineeringWorld().getWorld()) {
			newArray[counter] = bl;
			counter++;
		}
		EngBlock[] newRow = new EngBlock[this.getEngineeringWorld().getWidth()];
		for (int i = 0; i<this.getEngineeringWorld().getWidth(); i++) {
			newRow[i] = EngBlock.fromInt(0, i, counter, null);
		}
		newArray[counter] = newRow;
		this.getEngineeringWorld().setWorld(newArray);
		this.getEngineeringWorld().setHeight(this.getEngineeringWorld().getHeight()+1);
	}
	
	public void addEColumn(){
		EngBlock[][] newArray = new EngBlock[this.getEngineeringWorld().getHeight()][this.getEngineeringWorld().getWidth() + 1];
		int counter = 0;
		for (EngBlock[] bl: this.getEngineeringWorld().getWorld()) {
			EngBlock[] newRow = new EngBlock[this.getEngineeringWorld().getWidth() + 1];
			int c = 0;
			for (EngBlock b: bl) {
				newRow[c] = b;
				c++;
			}
			newRow[c] = EngBlock.fromInt(0, this.getEngineeringWorld().getWidth() + 1, counter, null);
			newArray[counter] = newRow;
			counter++;
		}
		this.getEngineeringWorld().setWorld(newArray);
		this.getEngineeringWorld().setWidth(this.getEngineeringWorld().getWidth()+1);
	}
	
	public void removeERow(){
		EngBlock[][] newArray = new EngBlock[this.getEngineeringWorld().getHeight() - 1][this.getEngineeringWorld().getWidth()];
		int counter = 0;
		for (EngBlock[] bl: this.getEngineeringWorld().getWorld()) {
			if (counter<this.getEngineeringWorld().getHeight() - 1) {
				newArray[counter] = bl;
				counter++;
			}
		}
		
		this.getEngineeringWorld().setWorld(newArray);
		this.getEngineeringWorld().setHeight(this.getEngineeringWorld().getHeight()-1);
	}
	
	public void removeEColumn(){
		EngBlock[][] newArray = new EngBlock[this.getEngineeringWorld().getHeight()][this.getEngineeringWorld().getWidth() - 1];
		int counter = 0;
		for (EngBlock[] bl: this.getEngineeringWorld().getWorld()) {
			EngBlock[] newRow = new EngBlock[this.getEngineeringWorld().getWidth() - 1];
			int c = 0;
			for (EngBlock b: bl) {
				if (c<this.getEngineeringWorld().getWidth() - 1) {
					newRow[c] = b;
				}
				c++;
			}
			newArray[counter] = newRow;
			counter++;
		}
		this.getEngineeringWorld().setWorld(newArray);
		this.getEngineeringWorld().setWidth(this.getEngineeringWorld().getWidth()-1);
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
		if (this.getEngineeringWorld() != null){
			addERow();
		}
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
		if (this.getEngineeringWorld() != null){
			addEColumn();
		}
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
			if (b.getType().equals(PORTAL) && !b.getInfo().split(";")[b.checkInfoKey("point")].split("#")[1].equals("NoPointSet")){
				String[] data = b.getInfo().split("#")[1].split(" ");
				getBlockAt(Integer.parseInt(data[0]), Integer.parseInt(data[1])).addInfoParam("point#NoPointSet");
			}
		}
		
		this.world = newArray;
		this.height--;
		if (this.getEngineeringWorld() != null){
			removeERow();
		}
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
		if (this.getEngineeringWorld() != null){
			removeEColumn();
		}
		updateWalls();
		updateOnFile();
		return true;
	}
}
