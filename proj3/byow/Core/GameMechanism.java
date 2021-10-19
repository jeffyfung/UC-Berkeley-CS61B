package byow.Core;

import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.util.ArrayList;
import java.util.Random;

public class GameMechanism {
    static Random RAND;
    static TETile[][] TILES;
    static Player PLAYER;
    static Position EXIT;
    static final float MIN_DIST_PLAYER_EXIT = 20; //20

    public static void initializeGameplay(Engine engine, ArrayList<Room> rooms) {
        System.out.println("start init gameplay");
        RAND = engine.random;
        TILES = engine.tiles;
        PLAYER = initializePlayer(Tileset.AVATAR);
        EXIT = initializeExit(rooms);
        System.out.println("done init gameplay");
        drawPlayer();
        drawExit(Tileset.UNLOCKED_DOOR);
    }

    private static Player initializePlayer(TETile avatar) {
        int x = 0;
        int y = 0;
        while (!TILES[x][y].equals(Tileset.FLOOR)) {
            x = RAND.nextInt(Engine.WORLD_WIDTH);
            y = RAND.nextInt(Engine.WORLD_HEIGHT);
        }
        Position pos = new Position(x,y);
        return new Player(pos, avatar);
    }

    private static Position initializeExit(ArrayList<Room> rooms) {
//        Room randRoom = rooms.get(RAND.nextInt(rooms.size()));
//        int x = randRoom.lowerLeft.getX();
//        int y = randRoom.lowerLeft.getY();
//        int dX;
//        int dY;
//        while (!tiles[x][y].equals(Tileset.WALL)
//                || (x == randRoom.lowerLeft.getX() && y == randRoom.lowerLeft.getY())
//                || (x == randRoom.lowerLeft.getX() && y == randRoom.upperRight.getY())
//                || (x == randRoom.upperRight.getX() && y == randRoom.lowerLeft.getY())
//                || (x == randRoom.upperRight.getX() && y == randRoom.upperRight.getY())) {
//            dX = RAND.nextInt(randRoom.upperRight.getX() - randRoom.lowerLeft.getX() + 1);
//            dY = RAND.nextInt(randRoom.upperRight.getY() - randRoom.lowerLeft.getY() + 1);
//            x = randRoom.lowerLeft.getX() + dX;
//            y = randRoom.lowerLeft.getY() + dY;
//        }
//        return new Position(x,y);
        Room randRoom = rooms.get(RAND.nextInt(rooms.size()));
        int x = randRoom.lowerLeft.getX();
        int y = randRoom.lowerLeft.getY();
        int dX;
        int dY;
        Position exitPos = new Position(x, y);
        while (!TILES[x][y].equals(Tileset.WALL)
                || Position.dist(exitPos, PLAYER.getPlayerPos()) < MIN_DIST_PLAYER_EXIT
                || (x == randRoom.lowerLeft.getX() && y == randRoom.lowerLeft.getY())
                || (x == randRoom.lowerLeft.getX() && y == randRoom.upperRight.getY())
                || (x == randRoom.upperRight.getX() && y == randRoom.lowerLeft.getY())
                || (x == randRoom.upperRight.getX() && y == randRoom.upperRight.getY())) {
            randRoom = rooms.get(RAND.nextInt(rooms.size()));
            dX = RAND.nextInt(randRoom.upperRight.getX() - randRoom.lowerLeft.getX() + 1);
            dY = RAND.nextInt(randRoom.upperRight.getY() - randRoom.lowerLeft.getY() + 1);
            x = randRoom.lowerLeft.getX() + dX;
            y = randRoom.lowerLeft.getY() + dY;
            exitPos = new Position(x, y);
            System.out.println(String.format("%d,%d", x, y));
        }
        return exitPos;
    }

    private static void drawPlayer() {
        TILES[PLAYER.getPlayerPos().getX()][PLAYER.getPlayerPos().getY()] = PLAYER.getAvatar();
    }

    private static void drawExit(TETile tile) {
        TILES[EXIT.getX()][EXIT.getY()] = tile;
    }

    // TODO: add a changeTilePattern method at engine

}
