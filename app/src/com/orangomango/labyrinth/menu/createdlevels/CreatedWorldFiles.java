package com.orangomango.labyrinth.menu.createdlevels;

import java.io.*;
import java.util.Arrays;

import static com.orangomango.labyrinth.menu.editor.Editor.PATH;
import com.orangomango.labyrinth.Logger;

public class CreatedWorldFiles {
	private String[] paths;
	private int worldFiles;

	public CreatedWorldFiles() {
		createFile();
	}

	public String[] getPaths() {
		return this.paths;
	}

	@Override
	public String toString() {
		String out = "";
		for (String p: this.paths) {
			out = out + p + "\n";
		}
		return out;
	}

	private void createFile() {
		File f = new File(PATH + ".labyrinthgame" + File.separator + "Editor" + File.separator + "Cache" + File.separator + "createdWorldFiles.data");
		try {
			if (!f.exists()) {
				f.createNewFile();
				BufferedWriter writer = new BufferedWriter(new FileWriter(f));
				writer.write("0");
				writer.close();
			}
			BufferedReader reader = new BufferedReader(new FileReader(f));
			int files = Integer.parseInt(reader.readLine());
			this.paths = new String[files];
			for (int x = 0; x<files; x++) {
				this.paths[x] = reader.readLine();
			}
			reader.close();
		} catch (IOException ex) {
			Logger.error("IOException caught on createdWorldFiles.data creation: "+ex.getMessage());
		}
	}

	private void updateFile() {
		File f = new File(PATH + ".labyrinthgame" + File.separator + "Editor" + File.separator + "Cache" + File.separator + "createdWorldFiles.data");
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(f));
			writer.write(String.format("%s", this.paths.length));
			if (this.paths.length > 0) {
				writer.newLine();
			}
			int counter = 0;
			for (String p: this.paths) {
				writer.write(p);
				if (counter != this.paths.length - 1) {
					writer.newLine();
				}
				counter++;
			}
			writer.close();
		} catch (IOException ex) {
			Logger.error("IOException caught on createdWorldFiles.data update: "+ex.getMessage());
		}
	}

	public void addToList(String path) {
		if (Arrays.asList(this.paths).contains(path)) {
			Logger.info("Could not add level item to mylevelslist because already exists");
			return;
		}
		String[] temp = Arrays.copyOf(this.paths, this.paths.length + 1);
		temp[temp.length - 1] = path;
		this.paths = Arrays.copyOf(temp, temp.length);
		updateFile();
	}

	public void removeFromList(String path) {
		if (!Arrays.asList(this.paths).contains(path)) {
			Logger.info("Level item does not exists so it could not be deleted");
			return;
		}
		int x = 0;
		String[] temp = new String[this.paths.length - 1];
		for (String p: this.paths) {
			if (p != path) {
				temp[x++] = p;
			}
		}
		this.paths = Arrays.copyOf(temp, temp.length);
		updateFile();
	}
}
