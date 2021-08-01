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
    int hexSideLen;
    int worldWidth;
    int worldHeight;
    TETile[][] world;

    public HexWorld(int hexSideLen) {
        this.hexSideLen = hexSideLen;
        this.worldWidth = 11 * hexSideLen - 6;
        this.worldHeight = 10 * hexSideLen;
        // initialize tiles
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
        // start recursion at the bottom middle tile
        int initX = 4 * hexSideLen - 2;
        int initY = 0;
        tessellateHex(initX, initY);
    }

    private void tessellateHex(int x, int y) {
        System.out.println("start");
        Hexagon hex = addHexagon(x, y, randomTile());
        int leftHexX = hex.leftUpperEdge.x - 2 * hexSideLen + 1;
        int leftHexY = hex.leftUpperEdge.y;
        System.out.println("left -> (" + leftHexX + "," + leftHexY + ")");
        if (leftHexX >= 0 && worldHeight - hex.leftUpperEdge.y >= hex.hexHeight) {
            if (world[leftHexX][leftHexY].equals(Tileset.NOTHING)) {
                System.out.println("left call");
                tessellateHex(leftHexX, leftHexY);
            }
        }
        int rightHexX = hex.rightUpperEdge.x - hexSideLen + 2;
        int rightHexY = hex.rightUpperEdge.y;
        System.out.println(hex.rightUpperEdge.x + ", " + hex.rightUpperEdge.y);
        System.out.println("right -> (" + rightHexX + "," + rightHexY + ")");
        if (rightHexX + hex.hexWidth <= worldWidth
                && worldHeight - hex.rightUpperEdge.y >= hex.hexHeight) {
            if (world[rightHexX + hex.hexWidth - 1][rightHexY].equals(Tileset.NOTHING)) {
                System.out.println("right call");
                tessellateHex(rightHexX, rightHexY);
            }
        }
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
//        List<IndexPair> tileIndices = new ArrayList<>();
        int hexWidth;
        int hexHeight;
        IndexPair leftUpperEdge;
        IndexPair rightUpperEdge;

        // rectangular -> hexagon
        Hexagon(int x, int y, TETile tilePattern) {
            hexWidth = 3 * hexSideLen - 2;
            hexHeight = 2 * hexSideLen;
            for (int r = 0; r < hexHeight; r += 1) {
                for (int c = findHexLeftBoundary(r); c < findHexRightBoundary(r); c += 1) {
                    world[x + c][y + r] = tilePattern;
//                    tileIndices.add(new IndexPair(x + c, y + r));
                    if (r == hexSideLen && c == 0) {
                        leftUpperEdge = new IndexPair(x + c, y + r);
                    }
                    if (r == hexSideLen && c == hexWidth - 1) {
                        rightUpperEdge = new IndexPair(x + c, y + r);
                    }
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

    class IndexPair {
        int x;
        int y;

        IndexPair(int xCoor, int yCoor) {
            this.x = xCoor;
            this.y = yCoor;
        }
    }

    // TODO: write a separate test class
    public static void main(String[] args) {
        int sideLen = 3;
        // creat a world
        HexWorld testWorld = new HexWorld(sideLen);
        // fill the world
        testWorld.tessellateHex();

        TERenderer ter = new TERenderer();
        ter.initialize(testWorld.worldWidth, testWorld.worldHeight);
        ter.renderFrame(testWorld.world);
    }
}
