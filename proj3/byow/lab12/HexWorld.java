package byow.lab12;
import org.junit.Test;
import static org.junit.Assert.*;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Draws a world consisting of hexagonal regions.
 */
public class HexWorld {
    static final Random RANDOM = new Random(50000);
    static final int RECURSION_DEPTH = 2;
    static final int BORDER = 4;
    int hexSideLen;
    int worldWidth;
    int worldHeight;
    int hexWidth;
    int hexHeight;
    TETile[][] world;

    public HexWorld(int hexSideLen) {
        this.hexSideLen = hexSideLen;
        this.worldWidth = 11 * hexSideLen - 6 + BORDER;
        this.worldHeight = 10 * hexSideLen + BORDER;
        this.hexWidth = 3 * hexSideLen - 2;
        this.hexHeight = 2 * hexSideLen;
        // initialize world with empty tiles
        world = new TETile[worldWidth][worldHeight];
        for (int x = 0; x < worldWidth; x += 1) {
            for (int y = 0; y < worldHeight; y += 1) {
                world[x][y] = Tileset.NOTHING;
            }
        }
    }

    public Hexagon addHexagon(int x, int y, TETile tilePattern) {
        return new Hexagon(x, y, tilePattern);
    }

    public void tessellateHex() {
        // start at the center tile
        int initX = worldWidth / 2 - hexSideLen;
        int initY = worldHeight / 2 - hexSideLen;
        tessellateHex(initX, initY, 0, new Direction[]
                {Direction.N, Direction.NW, Direction.SW, Direction.S, Direction.SE, Direction.NE});
    }

    private void tessellateHex(int x, int y, int recursionDepth, Direction[] dirList) {
        addHexagon(x, y, randomTile());
        if (recursionDepth >= RECURSION_DEPTH) {
            return;
        }
        for (Direction dir : dirList) {
            IndexPair newDrawingPoint = getNewDrawingPoint(dir, x, y);
            if (world[newDrawingPoint.x + hexSideLen][newDrawingPoint.y].equals(Tileset.NOTHING)){
                tessellateHex(newDrawingPoint.x, newDrawingPoint.y, recursionDepth + 1,
                        Direction.adjacentDirList(dir));
            }
        }
    }

    private IndexPair getNewDrawingPoint(Direction dir, int x, int y) {
        return switch (dir) {
            case N -> new IndexPair(x, y + hexHeight);
            case NW -> new IndexPair(x - 2 * hexSideLen + 1, y + hexSideLen);
            case SW -> new IndexPair(x - 2 * hexSideLen + 1, y - hexSideLen);
            case S -> new IndexPair(x, y - hexHeight);
            case SE -> new IndexPair(x + 2 * hexSideLen - 1, y - hexSideLen);
            case NE -> new IndexPair(x + 2 * hexSideLen - 1, y + hexSideLen);
        };
    }

    private static TETile randomTile() {
        int tileNum = RANDOM.nextInt(4);
        switch (tileNum) {
            case 0: return Tileset.WALL;
            case 1: return Tileset.FLOWER;
            case 2: return Tileset.WATER;
            default: return Tileset.GRASS;
        }
    }

    class Hexagon {
        Hexagon(int x, int y, TETile tilePattern) {
            for (int r = 0; r < hexHeight; r += 1) {
                for (int c = findHexLeftBoundary(r); c < findHexRightBoundary(r); c += 1) {
                    world[x + c][y + r] = tilePattern;
                }
            }
        }

        private int findHexLeftBoundary(int r) {
            if (r >= hexSideLen) {
                r = hexHeight - 1 - r;
            }
            return hexSideLen - 1 - r;
        }

        private int findHexRightBoundary(int r) {
            if (r >= hexSideLen) {
                r = hexHeight - 1 - r;
            }
            return hexWidth - hexSideLen + 1 + r;
        }
    }

    enum Direction {
        N, NW, SW, S, SE, NE;

        static Direction[] adjacentDirList(Direction dir) {
            return switch (dir) {
                case N -> new Direction[]{NW, N, NE};
                case NW -> new Direction[]{SW, NW, N};
                case SW -> new Direction[]{S, SW, NW};
                case S -> new Direction[]{SE, S, SW};
                case SE -> new Direction[]{NE, SE, S};
                case NE -> new Direction[]{N, NE, SE};
            };
        }
    }

    class IndexPair {
        int x;
        int y;

        IndexPair(int xCoor, int yCoor) {
            this.x = xCoor;
            this.y = yCoor;
        }
    }

    public static void main(String[] args) {
        int sideLen = 3;
        // create a world
        HexWorld testWorld = new HexWorld(sideLen);
        // fill the world
        testWorld.tessellateHex();
        // render the world
        TERenderer ter = new TERenderer();
        ter.initialize(testWorld.worldWidth, testWorld.worldHeight);
        ter.renderFrame(testWorld.world);
    }
}
