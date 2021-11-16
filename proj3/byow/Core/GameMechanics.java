package byow.Core;

import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.io.Serializable;
import java.util.*;

import static byow.Core.Engine.*;

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
    /** A set of torches that can light up the whole map. */
    Set<Torch> torches;
    /** Initial health of player. Game ends when health drops to 0 */
    static final int INIT_PLAYER_HEALTH = 500;
    /** Minimum distance between player and exit at initialization. */
    static final float MIN_DIST_PLAYER_EXIT = 20;
    /** Whether lights are toggled on / off. Only field of view is visible when lights are off. */
    boolean lightsOn = false;
    /** Variable to track positions within field of view during recursion. See getFovPos(). */
    List<Position> fovPos;
    /** Radius of field of view when lights are off. */
    static final int LIGHT_RADIUS = 5;
    /** Number of torches randomly placed on map. */
    static final int NUM_TORCHES = 8;

    /**
     * Constructor of the class. Set player and exit at random locations of the world.
     */
    GameMechanics(Engine engine, ArrayList<Room> rooms, String playerName, int playerHealth) {
        this.engine = engine;
        this.player = initializePlayer(playerName, playerHealth);
        this.exit = initializeExit(rooms);
        this.torches = initializeTorches();
        this.lightsOn = false;
    }

    /**
     * Randomly initializes player. Player must be placed within the confines of walls.
     * @param playerName name of player
     * @param playerHealth health of player
     * @return player set at a random location
     */
    private Player initializePlayer(String playerName, int playerHealth) {
        int x = engine.random.nextInt(WORLD_WIDTH);
        int y = engine.random.nextInt(WORLD_HEIGHT);
        while (!engine.getTilePattern(x, y).isSameType(Engine.patternFloor)) {
            x = engine.random.nextInt(WORLD_WIDTH);
            y = engine.random.nextInt(WORLD_HEIGHT);
        }
        Position pos = new Position(x,y);
        engine.changeTilePattern(pos, Engine.patternPlayerAvatar);
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
        engine.changeTilePattern(exitPos, Engine.patternExit);
        return exitPos;
    }

    private Set<Torch> initializeTorches() {
        Set<Torch> out = new HashSet<>();
        for (int i = 0; i < NUM_TORCHES; i += 1) {
            int x = engine.random.nextInt(WORLD_WIDTH);
            int y = engine.random.nextInt(WORLD_HEIGHT);
            while (!engine.getTilePattern(x, y).isSameType(Engine.patternFloor)) {
                x = engine.random.nextInt(WORLD_WIDTH);
                y = engine.random.nextInt(WORLD_HEIGHT);
            }
            Position pos = new Position(x, y);
            engine.changeTilePattern(pos, Engine.patternTorch);
            out.add(new Torch(pos, Engine.patternTorch));
        }
        return out;
    }

    /**
     * Moves game object.
     * @param go game object to be moved
     * @param dX x-axis displacement of game object
     * @param dY y-axis displacement of game object
     * @return output of movement:
     *     -1 - player's health falls to <=0 after movement;
     *      0 - successful movement;
     *      1 - no movement as player cannot walks into a wall;
     *      2 - exit current level and advance;
     */
    int moveGameObject(GameObject go, int dX, int dY) {
        int out = go.move(engine, dX, dY);
        if (out == 10 || out == 11) {
            lightsOn = out == 10;
            out = 0;
        }
        return out;
    }

    /** Deprecated */
    Torch getTorch(Position pos) {
        for (Torch t : torches) {
            if (t.pos.equals(pos)) {
                return t;
            }
        }
        throw new NoSuchElementException("Cannot find torch");
    }

    void lightSwitch() {
        lightsOn = !lightsOn;
    }

    /**
     * Return an array representing the field of view of player when lights are toggled off. The
     * field of view displays tiles that are at most LIGHT_RADIUS tiles away from player's
     * position and stops at walls.
     * @param tArray tile array representing game state
     * @return modified tile array that only shows the tiles close to player. All other tiles are
     * set to empty (Tileset.NOTHING)
     **/
    TETile[][] fieldOfView(TETile[][] tArray) {
        if (lightsOn) {
            return tArray;
        } else {
            fovPos = new LinkedList<>();
            getFovPos(player.pos, LIGHT_RADIUS);
            TETile[][] _tArray = new TETile[tArray.length][tArray[0].length];
            Engine.setTilesToBackground(_tArray);
            for (Position pos : fovPos) {
                _tArray[pos.getX()][pos.getY()] = tArray[pos.getX()][pos.getY()];
            }
            return _tArray;
        }
    }

    /** Recursive helper function to get a list of positions within field of view of player. See
     * fieldOfView(). */
    private void getFovPos(Position pos, int lr) {
        TETile curTilePattern = engine.getTilePattern(pos);
        if (lr >= 0 && !curTilePattern.isSameType(Tileset.NOTHING)) {
            fovPos.add(pos);
            if (!curTilePattern.isSameType(Engine.patternWall)) {
                getFovPos(new Position(pos.getX() + 1, pos.getY()), lr - 1);
                getFovPos(new Position(pos.getX() - 1, pos.getY()), lr - 1);
                getFovPos(new Position(pos.getX(), pos.getY() + 1), lr - 1);
                getFovPos(new Position(pos.getX(), pos.getY() - 1), lr - 1);
            }
        }
    }


}
