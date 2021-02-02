package com.orangomango.labyrinth;

class LabyrinthMain {
  public static void main(String[] args) {
    // Create a simple world
    World world = new World("../lib/world1.wld");
    System.out.println(world);
    Block block = world.getBlockAt(3, 1); // Get block at X:3 Y:1
    System.out.println("\n"+block+"\n");

    // Create a player on start position
    Player player = new Player(world.start[0], world.start[1], world);
    System.out.println(player);

    // Move player example from start to end
    player.moveOn(player.Y, player.NEGATIVE);
    player.moveOn(player.X, player.NEGATIVE);
    player.moveOn(player.Y, player.POSITIVE);
    player.moveOn(player.X, player.NEGATIVE);
    player.moveOn(player.Y, player.NEGATIVE);
    player.moveOn(player.X, player.POSITIVE);
    player.moveOn(player.Y, player.NEGATIVE);
    player.moveOn(player.X, player.POSITIVE);

    System.out.println(player); // Show current player state
  }
}