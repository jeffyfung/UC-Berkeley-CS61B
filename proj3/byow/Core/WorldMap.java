package byow.Core;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.util.ArrayList;
import java.util.Random;

import static byow.Core.Room.*;

public class WorldMap {
    static final Random RANDOM = new Random(120); // 120
    static final int worldWidth = 50;
    static final int worldHeight = 50;
    TETile[][] tiles;

    public WorldMap() {
        this.tiles = new TETile[worldWidth][worldHeight];
        for (int x = 0; x < worldWidth; x += 1) {
            for (int y = 0; y < worldHeight; y += 1) {
                tiles[x][y] = Tileset.NOTHING;
            }
        }
    }

    public static void main(String[] args){
        WorldMap testWorld = new WorldMap();
        ArrayList<Room> rooms = buildRooms(testWorld);
        System.out.println("rooms built");
        connectRooms(testWorld, rooms);

        TERenderer ter = new TERenderer();
        ter.initialize(worldWidth, worldHeight);
        ter.renderFrame(testWorld.tiles);
    }
}
