package byow.Core;

import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.util.ArrayList;
import java.util.Random;

public class GameMechanism {
    static Random rand;
    static TETile[][] tiles;
    static Player player;
    static Position exit;

    public static void initializeGameplay(Engine engine, ArrayList<Room> rooms) {
        rand = engine.random;
        tiles = engine.tiles;
        player = initializePlayer(Tileset.AVATAR);
        exit = initializeExit(rooms);
        drawPlayer();
        drawExit(Tileset.UNLOCKED_DOOR);
    }

    private static Player initializePlayer(TETile avatar) {
        int x = 0;
        int y = 0;
        while (!tiles[x][y].equals(Tileset.FLOOR)) {
            x = rand.nextInt(Engine.WORLD_WIDTH);
            y = rand.nextInt(Engine.WORLD_HEIGHT);
        }
        Position pos = new Position(x,y);
        return new Player(pos, avatar);
    }

    private static Position initializeExit(ArrayList<Room> rooms) {
        Room randRoom = rooms.get(rand.nextInt(rooms.size()));
        int x = randRoom.lowerLeft.getX();
        int y = randRoom.lowerLeft.getY();
        int dX;
        int dY;
        while (!tiles[x][y].equals(Tileset.WALL)
                || (x == randRoom.lowerLeft.getX() && y == randRoom.lowerLeft.getY())
                || (x == randRoom.lowerLeft.getX() && y == randRoom.upperRight.getY())
                || (x == randRoom.upperRight.getX() && y == randRoom.lowerLeft.getY())
                || (x == randRoom.upperRight.getX() && y == randRoom.upperRight.getY())) {
            dX = rand.nextInt(randRoom.upperRight.getX() - randRoom.lowerLeft.getX() + 1);
            dY = rand.nextInt(randRoom.upperRight.getY() - randRoom.lowerLeft.getY() + 1);
            x = randRoom.lowerLeft.getX() + dX;
            y = randRoom.lowerLeft.getY() + dY;
        }
        return new Position(x,y);
    }

    private static void drawPlayer() {
        tiles[player.getPlayerPos().getX()][player.getPlayerPos().getY()] = player.getAvatar();
    }

    private static void drawExit(TETile tile) {
        tiles[exit.getX()][exit.getY()] = tile;
    }

    // TODO: add a changeTilePattern method at engine

}
