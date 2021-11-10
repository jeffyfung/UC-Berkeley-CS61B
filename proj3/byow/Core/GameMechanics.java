package byow.Core;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Class to allow game objects, which do not include rooms and hallways, to interact with game
 * state and among themselves.
 */
public class GameMechanics implements Serializable {
    /** Game engine */
    transient Engine engine;
    /** Player */
    Player player;
    /** A door. Game ends successfully if player reaches the exit. */
    Position exit;
    /** Minimum distance between player and exit at initialization. */
    static final float MIN_DIST_PLAYER_EXIT = 20;

    /**
     * Constructor of the class. Set player and exit at random locations of the world.
     */
    GameMechanics(Engine engine, ArrayList<Room> rooms, String playerName, int playerHealth) {
        this.engine = engine;
        this.player = initializePlayer(playerName, playerHealth);
        this.exit = initializeExit(rooms);
        engine.changeTilePattern(player.pos, Engine.patternPlayerAvatar);
        engine.changeTilePattern(exit, Engine.patternExit);
    }

    /**
     * Randomly initializes player. Player must be placed within the confines of walls.
     * @param playerName name of player
     * @param playerHealth health of player
     * @return player set at a random location
     */
    private Player initializePlayer(String playerName, int playerHealth) {
        int x = 0;
        int y = 0;
        while (!engine.getTilePattern(x, y).isSameType(Engine.patternFloor)) {
            x = engine.random.nextInt(Engine.WORLD_WIDTH);
            y = engine.random.nextInt(Engine.WORLD_HEIGHT);
        }
        Position pos = new Position(x,y);
        return new Player(pos, Engine.patternPlayerAvatar, playerName, playerHealth);
    }

    /**
     * Randomly initializes exit. Exit must be placed on a wall of a room.
     * @param rooms rooms
     * @return position of exit
     */
    private Position initializeExit(ArrayList<Room> rooms) {
        Room randRoom = rooms.get(engine.random.nextInt(rooms.size()));
        int x = randRoom.lowerLeft.getX();
        int y = randRoom.lowerLeft.getY();
        int dX;
        int dY;
        Position exitPos = new Position(x, y);
        while (!engine.getTilePattern(x, y).equals(Engine.patternWall)
                || Position.dist(exitPos, player.pos) < MIN_DIST_PLAYER_EXIT
                || (x == randRoom.lowerLeft.getX() && y == randRoom.lowerLeft.getY())
                || (x == randRoom.lowerLeft.getX() && y == randRoom.upperRight.getY())
                || (x == randRoom.upperRight.getX() && y == randRoom.lowerLeft.getY())
                || (x == randRoom.upperRight.getX() && y == randRoom.upperRight.getY())) {
            randRoom = rooms.get(engine.random.nextInt(rooms.size()));
            dX = engine.random.nextInt(randRoom.upperRight.getX()
                    - randRoom.lowerLeft.getX() + 1);
            dY = engine.random.nextInt(randRoom.upperRight.getY()
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
     * @return outcome of movement: 0 - success; 1 - advance level; 2 - no movement
     */
    int moveGameObject(GameObject go, int dX, int dY) {
        return go.move(engine, dX, dY);
    }
}
