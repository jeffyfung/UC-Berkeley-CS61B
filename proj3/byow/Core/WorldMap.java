package byow.Core;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class WorldMap {
    static final Random RANDOM = new Random(1000);
    static final int worldWidth = 40;
    static final int worldHeight = 40;
    TETile[][] world;

    public WorldMap() {
        this.world = new TETile[worldWidth][worldHeight];
        for (int x = 0; x < worldWidth; x += 1) {
            for (int y = 0; y < worldHeight; y += 1) {
                world[x][y] = Tileset.NOTHING;
            }
        }
    }

    public void drawRoomsAndHallways(){
        LinkedList<Position> line = new LinkedList<>();
        for (int i = 0; i < 5; i += 1) {
            line.add(new Position(10 + i, 10));
        }
        Room.buildRoom(world, line, RANDOM);
    }

    public static void main(String[] args){
        WorldMap testWorld = new WorldMap();
        testWorld.drawRoomsAndHallways();

        TERenderer ter = new TERenderer();
        ter.initialize(worldWidth, worldHeight);
        ter.renderFrame(testWorld.world);
    }
}
