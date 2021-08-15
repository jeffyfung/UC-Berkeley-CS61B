package byow.Core;

import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.util.*;

public class Room {
    Position topLeftCorner;
    Position topRightCorner;
    Position bottomLeftCorner;
    Position bottomRightCorner;
    LinkedList<Position> topWall;
    LinkedList<Position> leftWall;
    LinkedList<Position> bottomWall;
    LinkedList<Position> rightWall;
    LinkedList<LinkedList<Position>> walls;

    public Room() {}

    /** Build a room by filling in wall tiles and floor tiles. Call buildHallway at the end. To be
     * updated.
     *
     * @param world : world that the room will be built in
     * @param anchorLine : the side of the room connected to an existing hallway
     * @param random : random number generator
     */
    static void buildRoom(TETile[][] world, LinkedList<Position> anchorLine, Random random) {
        int orientation = Position.checkOrientationOfLine(anchorLine);
        if (orientation == 0) {
            throw new IllegalArgumentException("Anchor line should be either horizontal or " +
                    "vertical and consists of a part of wall tiles of an existing room or " +
                    "hallway");
        }
        int startX = anchorLine.get(0).x;
        int startY = anchorLine.get(0).y;
        int h = anchorLine.size();
        int w = random.nextInt((10 - 3) + 1) + 3; // TODO: take input => legRoom
        if (orientation == 2) { // swap height and width if anchor line is horizontal
            int tmp = h;
            h = w;
            w = tmp;
        }
        Room room = new Room();
        buildRectangularSpace(world, startX, startY, w, h, Tileset.GRASS);
        buildRectanglePeriphery(world, startX, startY, w, h, room, Tileset.WALL);
        buildFromLine(world, anchorLine, Tileset.FLOWER); // TODO: should not be anchor line???
        LinkedList<LinkedList<Position>> newAnchorLines = getNewAnchorLines();

        // select next anchorLine (check eligibility)
        // anchor line must be sorted
        // calculate available space til next obstacle / edge -> sample h / w of next room / hallway
//        buildHallway(world, newAnchorLine, c, random);
    }

    static void buildHallway(TETile[][] world, LinkedList<Position> anchorLine,
                             int emptyLength, Random random) {
        int orientation = Position.checkOrientationOfLine(anchorLine);
        if (orientation == 0) {
            throw new IllegalArgumentException("Anchor line should be either horizontal or " +
                    "vertical and consists of a part of wall tiles of an existing room or " +
                    "hallway");
        }
        int startX = anchorLine.get(0).x;
        int startY = anchorLine.get(0).y;
        int h = anchorLine.size(); // always = 3
        int w = random.nextInt((emptyLength - 3) + 1) + 3;
        if (orientation == 2) { // swap height and width if anchor line is horizontal
            int tmp = h;
            h = w;
            w = tmp; // always = 3
        }
        // choose eligible anchor line
        // buildRoom(world, newAnchorLine, c, random);
    }

    /** Build a rectangular space filled with given tilePattern. Perimeters of the rectangle are
     * left empty.
     * @param world : world that the space will be built in
     * @param x : x coordinate of the bottom left corner of the rectangle
     * @param y : y coordinate of the bottom right corner of the rectangle
     * @param w : width of rectangle
     * @param h : height of rectangle
     * @param tilePattern : pattern filled in the rectangle
     */
    private static void buildRectangularSpace(TETile[][] world, int x, int y, int w, int h,
                                              TETile tilePattern) {
        for (int i = 1; i < w - 1; i += 1) {
            for (int j = 1; j < h - 1; j += 1) {
                world[x + i][y + j] = tilePattern;
            }
        }
    }

    /** Fill the perimeters of a rectangular space with given tilePattern. Record coordinates of
     * walls and corners of the room to a Room object.
     * @param world : world that the space will be built in
     * @param x : x coordinate of the bottom left corner of the rectangle
     * @param y : y coordinate of the bottom right corner of the rectangle
     * @param w : width of rectangle
     * @param h : height of rectangle
     * @param room : Room object
     * @param tilePattern : pattern filled in the rectangle
     */
    private static void buildRectanglePeriphery(TETile[][] world, int x, int y, int w, int h,
                                                       Room room, TETile tilePattern) {
        // bottomWall
        for (int i = 0; i < w; i += 1) {
            world[x + i][y] = tilePattern;
            room.bottomWall.add(new Position(x + i, y));
        }
        // topWall
        for (int i = 0; i < w; i += 1) {
            world[x + i][y + h - 1] = tilePattern;
            room.topWall.add(new Position(x + i, y + h - 1));
        }
        // leftWall
        for (int j = 0; j < h; j += 1) {
            world[x][y + j] = tilePattern;
            room.leftWall.add(new Position(x, y + j));
        }
        // rightWall
        for (int j = 0; j < h; j += 1) {
            world[x + w - 1][y + j] = tilePattern;
            room.rightWall.add(new Position(x + w - 1, y + j));
        }
        room.walls.add(room.topWall);
        room.walls.add(room.leftWall);
        room.walls.add(room.bottomWall);
        room.walls.add(room.rightWall);
        room.bottomLeftCorner = room.bottomWall.get(0);
        room.bottomRightCorner = room.bottomWall.get(w - 1);
        room.topLeftCorner = room.topWall.get(0);
        room.topRightCorner = room.topWall.get(w - 1);
    }

    /** Fill all tiles overlapping with anchorLine with given tilePattern. */
    private static void buildFromLine(TETile[][] world, LinkedList<Position> line,
                                      TETile tilePattern) {
        for (Position pos : line) {
            world[pos.x][pos.y] = tilePattern;
        }
    }

    // TODO: multiple output: positions and legRoom
    private static LinkedList<LinkedList<Position>> getNewAnchorLines(TETile[][] world, Room room,
                                                  int x, int y, int lengthAlong, Random random) {
        // randomly choose either 1 or 2 sides max to be sampled
//        int linesToAdd = random.nextInt(2) + 1;
        int linesToAdd = 1;
        // shuffle a list of walls
        LinkedList<LinkedList<Position>> shuffledWalls = room.walls;
        Collections.shuffle(shuffledWalls);
        // TODO: except source anchor side
        int counter = 0;
        LinkedList<LinkedList<Position>> output = new LinkedList<>();
        while (linesToAdd > 0 && counter < 10){
            LinkedList<Position> w = shuffledWalls.remove();
            // sample a starting point from wall randomly
            int c = w.size() - 1 - lengthAlong;
            if (c <= 0) {
                shuffledWalls.add(w);
                counter -= 1;
                continue;
            }
            // add a new anchor line to output
            int startIdx = random.nextInt(c) + 1;
            LinkedList<Position> tmpLine = new LinkedList<>();
            for (int i = 0; i < lengthAlong; i += 1) {
                tmpLine.add(w.get(startIdx + i));
            }
            output.add(tmpLine);
            linesToAdd -= 1;
        }
        return output;
    }
}