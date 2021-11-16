package byow.TileEngine;

import java.awt.Color;

/**
 * Contains constant tile objects, to avoid having to remake the same tiles in different parts of
 * the code.
 */
public class Tileset {
    public static final TETile NOTHING = new TETile(' ', Color.black, Color.black, "");
    public static final TETile AVATAR_LEFT = new TETile('@', Color.white, Color.black,
            "Player", "./tileImage/avatarLeft.png");
    public static final TETile AVATAR_RIGHT = new TETile('-', Color.white, Color.black,
            "Player", "./tileImage/avatarRight.png");
    public static final TETile TREE = new TETile('♠', Color.green, Color.black,
            "Tree", "./tileImage/tree.png");
    public static final TETile GRASS = new TETile('"', Color.green, Color.black,
            "Grass", "./tileImage/grass.png");
    public static final TETile LOCKED_DOOR = new TETile('█', Color.orange, Color.black,
            "Exit", "./tileImage/ice.png");
    public static final TETile FIREBALL = new TETile('1', Color.red, Color.black,
            "Fireball", "/tileImage/fireball.png");
    public static final TETile SOIL = new TETile('▒', Color.yellow, Color.black,
            "Soil", "./tileImage/soil.png");
    public static final TETile TORCH = new TETile('!', Color.black, Color.black
            , "Torch", "./tileImage/lamp.png");

    /* Unused tileset */
    public static final TETile FLOOR = new TETile('·', new Color(128, 192, 128), Color.black,
            "Floor");
    public static final TETile WATER = new TETile('≈', Color.blue, Color.black, "Water");
    public static final TETile FLOWER = new TETile('❀', Color.magenta, Color.pink, "Flower");
    public static final TETile UNLOCKED_DOOR = new TETile('▢', Color.orange, Color.black,
            "Unlocked Door");
    public static final TETile MOUNTAIN = new TETile('▲', Color.gray, Color.black, "Mountain");
    public static final TETile WALL = new TETile('#', new Color(216, 128, 128), Color.darkGray,
            "Wall", "./tileImage/brownWall.png");
}


