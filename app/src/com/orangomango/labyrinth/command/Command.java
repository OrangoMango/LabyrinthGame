package com.orangomango.labyrinth.command;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.control.Tab;

import java.io.*;

import com.orangomango.labyrinth.menu.editor.*;
import com.orangomango.labyrinth.menu.Menu;

public class Command {
	private String command;
	private VBox vbox;
	private Editor editor;
	private EditableWorld edworld;
	
	public static final String NEW_FILE = "new";
	public static final String OPEN_FILE = "open";
	public static final String SAVE_FILE = "save";
	public static final String DELETE = "del";
	public static final String LIST_FILES = "list";
	public static final String GET_ABS_PATH = "abs";
	public static final String MODIFY_WORLD = "modify";
	public static final String CURRENT_PATH = "current_path";
	public static final String WORKING_PATH = "working_path";
	public static final String EXIT = "exit";
	public static final String RUN = "run";
	public static final String HELP = "help";
	
	public Command(String command, VBox v){
		this.command = command;
		this.vbox = v;
	}
	
	public void setEditor(Editor e){
		this.editor = e;
	}
	
	public void setEditableWorld(EditableWorld e){
		this.edworld = e;
	}
	
	public void execute(boolean layout){
		String[] args = this.command.split(" ");
		String cmd = args[0];
		String outputText = "";
		switch (cmd.toLowerCase()){
			case NEW_FILE:
				NewWidget wid = new NewWidget(false);
				wid.setEditor(this.editor);
				outputText = "File creation started";
				break;
			case GET_ABS_PATH:
				if (args.length == 1){
					outputText = "ABS path: "+this.editor.getCurrentFilePath();
					break;
				}
				String p = Editor.worldList.getPath(args[1]);
				if (p.equals("")){
					outputText = "File not available in your worldList";
				} else {
					outputText = "ABS path: "+p;
				}
				break;
			case OPEN_FILE:
				if (args.length == 1){
					outputText = "usage: open <file-name>";
					break;
				}
				if (!(new File(Editor.worldList.getPath(args[1])).exists())){
					outputText = "File not found";
					break;
				}
				if (Editor.worldList.getPath(args[1]).equals("")){
					this.editor.open(new File(args[1]));
				} else {
					this.editor.open(new File(Editor.worldList.getPath(args[1])));
				}
				outputText = "File opened";
				break;
			case SAVE_FILE:
				this.editor.saved(true);
				outputText = "File saved";
				break;
			case EXIT:
				this.editor.exit();
				Menu m = new Menu(this.editor.stage);
				break;
			case LIST_FILES:
				String[] list = Editor.worldList.getPaths();
				StringBuilder out = new StringBuilder();
				for (String f : list){
					out.append((new File(f)).getName()+"\n");
				}
				outputText = out.toString();
				break;
			case CURRENT_PATH:
				outputText = "CURRENT_PATH: "+this.editor.getCurrentFilePath();
				break;
			case WORKING_PATH:
				outputText = "WORKING_PATH: "+this.edworld.getFilePath();
				break;
			case MODIFY_WORLD:
				if (args.length == 1){
					outputText = "usage: modify <command>\ntype \"help modify\" for more info";
					break;
				}
				String mod = args[1];
				int times = 1;
				if (args.length == 3){
					try {
						times = Integer.parseInt(args[2]);
					} catch (Exception e){
						outputText = "Invalid argument: "+e.getMessage();
						break;
					}
				}
				switch (mod){
					case "ar":
						for (int i = 0; i < times; i++){
							if (this.editor.checkValidityMax("h")){
								this.edworld.addRow();
								this.editor.unsaved();
								outputText = "Row(s) added successfully";
							} else {
								outputText = "Could not add row, WORLD_MAX_SIZE_ERROR";
								break;
							}
						}
						break;
					case "ac":
						for (int i = 0; i < times; i++){
							if (this.editor.checkValidityMax("w")){
								this.edworld.addColumn();
								this.editor.unsaved();
								outputText = "Column(s) added successfully";
							} else {
								outputText = "Could not add column, WORLD_MAX_SIZE_ERROR";
								break;
							}
						}
						break;
					case "rr":
						for (int i = 0; i < times; i++){
							boolean valueR = this.edworld.removeRow();
							if (valueR){
								outputText = "Row(s) removed successfully";
							} else {
								outputText = "Could not remove row";
								break;
							}
						}
						break;
					case "rc":
						for (int i = 0; i < times; i++){
							boolean valueC = this.edworld.removeColumn();
							if (valueC){
								outputText = "Column(s) removed successfully";
							} else {
								outputText = "Could not remove column";
								break;
							}
						}
						break;
					case "start_end":
						new SESetup(this.edworld, this.edworld.width, this.edworld.height, this.edworld.start, this.edworld.end);
						this.editor.unsaved();
						outputText = "Start/End positions changed";
						break;
					case "description":
						new ChangeDescription(this.edworld);
						this.editor.unsaved();
						outputText = "Description changed";
						break;
					default:
						outputText = "Invalid argument!";
						break;
				}
				break;
			case RUN:
				String toRun = "";
				if (args.length > 1){
					toRun = this.editor.worldList.getPath(args[1]);
					if (toRun.equals("")){
						outputText = "File not available in your worldList";
						break;
					}
				} else {
					toRun = Editor.getCurrentFilePath();
				}
				new LevelExe(toRun, (new File(toRun)).getName(), this.editor.isSaved(), this.editor.getMode());
				LevelExe.setOnFinish(null);
				outputText = "Level running";
				break;
			case DELETE:
				if (args.length == 1){
					outputText = "usage: del <file-name>";
					break;
				}
				String file = this.editor.worldList.getPath(args[1]);
				if (file.equals("")){
					outputText = "File not available in your worldList";
					break;
				}
				if (file.equals(this.editor.getCurrentFilePath())){
					outputText = "Could not delete current file";
					break;
				}
				this.editor.worldList.removeFromList(file);
				File toDel = new File(file);
				toDel.delete();
				Tab t = this.editor.getTab(file);
				if (t != null){
					t.setDisable(true);
				}
				outputText = "File deleted successfully";
				break;
			case HELP:
				if (args.length > 1){
					switch (args[1]){
						case MODIFY_WORLD:
							outputText = "usage: modify <command>\nCommands:\nac [n] - add column n times\nar [n] - add row n times\nrc [n] - remove column n times\nrr [n] - remove row n times\nstart_end - modify start/end position\ndescription - change level description";
							break;
						default:
							outputText = "No documentation available for command \""+args[1]+"\"";
					}
				} else {
					outputText = "Available commands:\nCURRENT_PATH, get current_path: current_path\nDELETE, delete a level: del <file-name>\nEXIT, exit the editor: exit\nGET_ABS_PATH, get abs path of a file: abs [file-name]\nHELP, help for a command: help [command-name]\nLIST, list worlds: list\nMODIFY, modify level: modify <command>\nNEW, create a new file: new\nOPEN, open a level file: open <file-name>\nRUN, run a level: run [level-name]\nSAVE, save current file: save\nWORKING_PATH, get working path: working_path";
				}
				break;
			default:
				outputText = "Command not found, type \"help\" to get the list of available commands";
				break;
		}
		if (layout){
			makeLayout(outputText);
		}
	}
	
	private void makeLayout(String outputText){
		this.vbox.getChildren().add(new Label(outputText));
	}
}
