package byow.Core;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.util.Random;

import static byow.Core.Room.*;

public class WorldMap {
    static final Random RANDOM = new Random(120); // 1000
    static final int worldWidth = 50;
    static final int worldHeight = 50;
    TETile[][] world;

    public WorldMap() {
        this.world = new TETile[worldWidth][worldHeight];
        for (int x = 0; x < worldWidth; x += 1) {
            for (int y = 0; y < worldHeight; y += 1) {
                world[x][y] = Tileset.NOTHING;
            }
        }
    }

    public static void main(String[] args){
        WorldMap testWorld = new WorldMap();
        buildRooms(testWorld);
//        buildHallways(testWorld, rooms);

        TERenderer ter = new TERenderer();
        ter.initialize(worldWidth, worldHeight);
        ter.renderFrame(testWorld.world);
    }

    // public static void drawHallways(WorldMap map, List<Room> rooms) {
    //     return;
    // }
}
