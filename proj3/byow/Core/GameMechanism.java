package byow.Core;

import byow.TileEngine.Tileset;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import static byow.Core.PersistenceUtils.*;

/**
 * Class to allow game objects, which do not include rooms and hallways, to interact with game
 * state and among themselves.
 */
public class GameMechanism {
    static Engine ENGINE;
    static Player PLAYER;
    /** A door. Game ends successfully if player reaches the exit. */
    static Position EXIT;
    /** Minimum distance between player and exit at initialization. */
    static final float MIN_DIST_PLAYER_EXIT = 20;

    /**
     * Constructor of the class.
     */
    public static void initializeGameplay(Engine engine, ArrayList<Room> rooms) {
        ENGINE = engine;
        PLAYER = initializePlayer();
        EXIT = initializeExit(rooms);
        ENGINE.changeTilePattern(PLAYER.pos, Engine.patternPlayerAvatar);
        ENGINE.changeTilePattern(EXIT, Engine.patternExit);
    }

    /** Randomly initializes player object. Player must be placed within the walls. */
    private static Player initializePlayer() {
        int x = 0;
        int y = 0;
        while (!ENGINE.getTilePattern(x, y).equals(Tileset.FLOOR)) {
            x = ENGINE.random.nextInt(Engine.WORLD_WIDTH);
            y = ENGINE.random.nextInt(Engine.WORLD_HEIGHT);
        }
        Position pos = new Position(x,y);
        return new Player(pos, Tileset.AVATAR);
    }

    /**
     * Randomly initializes exit. Exit must be placed on a wall of a room.
     * @param rooms rooms
     * @return position of exit
     */
    private static Position initializeExit(ArrayList<Room> rooms) {
        Room randRoom = rooms.get(ENGINE.random.nextInt(rooms.size()));
        int x = randRoom.lowerLeft.getX();
        int y = randRoom.lowerLeft.getY();
        int dX;
        int dY;
        Position exitPos = new Position(x, y);
        while (!ENGINE.getTilePattern(x, y).equals(Tileset.WALL)
                || Position.dist(exitPos, PLAYER.pos) < MIN_DIST_PLAYER_EXIT
                || (x == randRoom.lowerLeft.getX() && y == randRoom.lowerLeft.getY())
                || (x == randRoom.lowerLeft.getX() && y == randRoom.upperRight.getY())
                || (x == randRoom.upperRight.getX() && y == randRoom.lowerLeft.getY())
                || (x == randRoom.upperRight.getX() && y == randRoom.upperRight.getY())) {
            randRoom = rooms.get(ENGINE.random.nextInt(rooms.size()));
            dX = ENGINE.random.nextInt(randRoom.upperRight.getX()
                    - randRoom.lowerLeft.getX() + 1);
            dY = ENGINE.random.nextInt(randRoom.upperRight.getY()
                    - randRoom.lowerLeft.getY() + 1);
            x = randRoom.lowerLeft.getX() + dX;
            y = randRoom.lowerLeft.getY() + dY;
            exitPos = new Position(x, y);
        }
        return exitPos;
    }

    /**
     * Moves game object.
     * @param go game object to be moved
     * @param dX x-axis displacement of game object
     * @param dY y-axis displacement of game object
     * @return  whether the movement leads to the end of a game
     */
    static boolean moveGameObject(GameObject go, int dX, int dY) {
        return go.move(dX, dY);
    }

    /** Serializes and saves game objects to a .gamesave/gameObjects file. */
    static void saveGameObjects() {
        HashMap<String, Serializable> gameObjects = new HashMap<>();
        gameObjects.put("player", PLAYER);
        gameObjects.put("exit", EXIT);
        writeObject(join(Engine.GAMESAVE, "gameObjects"), gameObjects);
    }

    /**
     * Load game objects from file.
     * @param file file to load game from
     * @param engine engine that the game runs on
     */
    static void loadGameObjects(File file, Engine engine) {
        ENGINE = engine;
        HashMap<String, Serializable> gameObjects = readObject(file, HashMap.class);
        PLAYER = (Player) gameObjects.get("player");
        EXIT = (Position) gameObjects.get("position");
    }
}
