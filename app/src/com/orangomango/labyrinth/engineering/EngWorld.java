package com.orangomango.labyrinth.engineering;

import java.util.Arrays;

import com.orangomango.labyrinth.World;

public class EngWorld {
	private EngBlock[][] world;
	private int width;
	private int height;
	public EngBlock[] foundBlocks = new EngBlock[0];

	public EngWorld(EngBlock[][] wo, int w, int h) {
		this.world = wo;
		this.height = h;
		this.width = w;
		for (EngBlock[] r: this.world) {
			System.out.println(Arrays.toString(r));
			for (EngBlock b: r) {
				b.setWorld(this);
			}
		}
	}
	
	public EngBlock[][] getWorld(){
		return this.world;
	}
	
	public int getWidth(){
		return this.width;
	}
	
	public int getHeight(){
		return this.height;
	}
	
	public void setWorld(EngBlock[][] w){
		this.world = w;
	}

	public EngBlock[] getFoundBlocks() {
		return this.foundBlocks;
	}

	public EngBlock getBlockAt(int x, int y) {
		try {
			return this.world[y][x];
		} catch (ArrayIndexOutOfBoundsException e) {
			return null;
		}
	}
	
	public EngBlock getBlockAtCoord(int x, int y){
		int x1 = x / World.BLOCK_WIDTH;
		int y1 = y / World.BLOCK_WIDTH;
		return getBlockAt(x1, y1);
	}
	
	public void setBlockOn(EngBlock block) {
		this.world[block.getY()][block.getX()] = new EngBlock(block.getX(), block.getY(), block.getType(), block.getInfo());
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		for (EngBlock[] r: this.world) {
			b.append(Arrays.toString(r));
			b.append("\n");
		}
		b.append("Size: "+this.width+"x"+this.height);
		return b.toString();
	}

	public String[] getAtt(int x, int y) {
		boolean n = false;
		boolean e = false;
		boolean s = false;
		boolean w = false;
		if (getBlockAt(x, y - 1) != null) {
			if (!getBlockAt(x, y - 1).getType().equals(EngBlock.AIR)) {
				n = true;
			}
		}
		if (getBlockAt(x + 1, y) != null) {
			if (!getBlockAt(x + 1, y).getType().equals(EngBlock.AIR)) {
				e = true;
			}
		}
		if (getBlockAt(x, y + 1) != null) {
			if (!getBlockAt(x, y + 1).getType().equals(EngBlock.AIR)) {
				s = true;
			}
		}
		if (getBlockAt(x - 1, y) != null) {
			if (!getBlockAt(x - 1, y).getType().equals(EngBlock.AIR)) {
				w = true;
			}
		}
		int a = 0;
		a += (n ? 1 : 0);
		a += (e ? 1 : 0);
		a += (s ? 1 : 0);
		a += (w ? 1 : 0);

		String d = (n ? "n" : "") + (e ? "e" : "") + (s ? "s" : "") + (w ? "w" : "");
		return new String[] {
			Integer.toString(a), d, "cable" + (a == 0 ? "" : "-") + d + ".png"
		};
	}
	
	public static EngWorld createNewEngWorld(int w, int h){
		EngBlock[][] output = new EngBlock[h][w];
		for (int y = 0; y < h; y++){
			for (int x = 0; x < w; x++){
				output[y][x] = new EngBlock(x, y, EngBlock.AIR, null);
			}
		}
		return new EngWorld(output, w, h);
	}

	public int getConnected(int px, int py, String f) {
		int conn = 0;
		String[] dir = new String[] {
			"n", "e", "s", "w"
		};

		if (getBlockAt(px, py - 1) != null && !dir[0].equals(f)) {
			if (getBlockAt(px, py - 1).getCategory().equals(EngBlock.SIGNAL_EXTENDER)) {
				conn += getConnected(px, py - 1, dir[2]);
			} else if (getBlockAt(px, py - 1).isActivable()) {
				if (!Arrays.asList(this.foundBlocks).contains(getBlockAt(px, py - 1))) {
					conn++;
					this.foundBlocks = Arrays.copyOf(this.foundBlocks, this.foundBlocks.length + 1);
					this.foundBlocks[this.foundBlocks.length - 1] = getBlockAt(px, py - 1);
				}
			}
		}

		if (getBlockAt(px + 1, py) != null && !dir[1].equals(f)) {
			if (getBlockAt(px + 1, py).getCategory().equals(EngBlock.SIGNAL_EXTENDER)) {
				conn += getConnected(px + 1, py, dir[3]);
			} else if (getBlockAt(px + 1, py).isActivable()) {
				if (!Arrays.asList(this.foundBlocks).contains(getBlockAt(px + 1, py))) {
					conn++;
					this.foundBlocks = Arrays.copyOf(this.foundBlocks, this.foundBlocks.length + 1);
					this.foundBlocks[this.foundBlocks.length - 1] = getBlockAt(px + 1, py);
				}
			}
		}

		if (getBlockAt(px, py + 1) != null && !dir[2].equals(f)) {
			if (getBlockAt(px, py + 1).getCategory().equals(EngBlock.SIGNAL_EXTENDER)) {
				conn += getConnected(px, py + 1, dir[0]);
			} else if (getBlockAt(px, py + 1).isActivable()) {
				if (!Arrays.asList(this.foundBlocks).contains(getBlockAt(px, py + 1))) {
					conn++;
					this.foundBlocks = Arrays.copyOf(this.foundBlocks, this.foundBlocks.length + 1);
					this.foundBlocks[this.foundBlocks.length - 1] = getBlockAt(px, py + 1);
				}
			}
		}

		if (getBlockAt(px - 1, py) != null && !dir[3].equals(f)) {
			if (getBlockAt(px - 1, py).getCategory().equals(EngBlock.SIGNAL_EXTENDER)) {
				conn += getConnected(px - 1, py, dir[1]);
			} else if (getBlockAt(px - 1, py).isActivable()) {
				if (!Arrays.asList(this.foundBlocks).contains(getBlockAt(px - 1, py))) {
					conn++;
					this.foundBlocks = Arrays.copyOf(this.foundBlocks, this.foundBlocks.length + 1);
					this.foundBlocks[this.foundBlocks.length - 1] = getBlockAt(px - 1, py);
				}
			}
		}
		return conn;
	}
}
