package com.orangomango.labyrinth.menu.editor;

import com.orangomango.labyrinth.Block;

public class EditableBlock extends Block {
	public EditableBlock(String type, int x, int y, String i) {
		super(type, x, y, i);
	}

	public void setType(String type) {
		this.type = type;
	}

	public static EditableBlock fromBlock(Block bl) {
		return new EditableBlock(bl.getType(), bl.getX(), bl.getY(), bl.getInfo());
	}

	public void toggleType(String blockType) {
		if (getType() == EditableWorld.AIR) {
			setType(blockType);
		} else {
			setType(EditableWorld.AIR);
			setInfo(null);
		}
	}
}
