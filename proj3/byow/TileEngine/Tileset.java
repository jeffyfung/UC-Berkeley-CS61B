package byow.TileEngine;

import java.awt.Color;

/**
 * Contains constant tile objects, to avoid having to remake the same tiles in different parts of
 * the code.
 */

public class Tileset {
    public static final TETile NOTHING = new TETile(' ', Color.black, Color.black, "");
    public static final TETile AVATAR = new TETile('@', Color.white, Color.black,
            "Player", "./tileImage/avatar.png");
    public static final TETile WALL = new TETile('#', new Color(216, 128, 128), Color.darkGray,
            "Wall", "./tileImage/brownWall.png");
    public static final TETile GRASS = new TETile('"', Color.green, Color.black,
            "Grass", "./tileImage/grass.png");
    public static final TETile LOCKED_DOOR = new TETile('█', Color.orange, Color.black,
            "Exit", "./tileImage/door1a.png");

    /* Unused tileset */
    public static final TETile FLOOR = new TETile('·', new Color(128, 192, 128), Color.black,
            "Floor");
    public static final TETile WATER = new TETile('≈', Color.blue, Color.black, "Water");
    public static final TETile FLOWER = new TETile('❀', Color.magenta, Color.pink, "Flower");
    public static final TETile UNLOCKED_DOOR = new TETile('▢', Color.orange, Color.black,
            "Unlocked Door");
    public static final TETile SAND = new TETile('▒', Color.yellow, Color.black, "Sand");
    public static final TETile MOUNTAIN = new TETile('▲', Color.gray, Color.black, "Mountain");
    public static final TETile TREE = new TETile('♠', Color.green, Color.black, "Tree");
}


