package byow.Core;

import byow.TileEngine.TETile;

/**
 * Sub-class of GameObject.
 */
public class Player extends GameObject {
    /** Health of player. Game ends when health <= 0  */
    int health;
    /** Name of player */
    String name;

    /** Constructor of the class. */
    Player(Position pos, TETile avatar, String name, int health) {
        super(pos, avatar);
        this.name = name;
        this.health = health;
        this.lastTilePattern = Engine.patternFloor;
    }

    /**
     * Moves player and interacts with other game objects as indicated by the method output.
     * @param engine game engine
     * @param dX displacement along x-axis
     * @param dY displacement along y-axis
     * @return output of movement:
     *      -1 - player's health falls to <=0 after movement;
     *       0 - successful movement;
     *       1 - no movement as player cannot walks into a wall;
     *       2 - exit current level and advance;
     *       10 - successful movement into a torch -> make entire map visible
     *       11 - successful movement out of a torch -> re-enable field of view
     */
    int move(Engine engine, int dX, int dY) {
        Position _pos = new Position(pos.getX() + dX, pos.getY() + dY);
        TETile _lastTilePattern = engine.getTilePattern(_pos);
        if (_lastTilePattern.isSameType(Engine.patternWall)) {
            return 1;
        }
        if (_lastTilePattern.isSameType(Engine.patternExit)) {
            return 2;
        }
        if (!changeHealth(-1)) {
            return -1;
        }
        if (dX != 0 || dY != 0) {
            engine.changeTilePattern(pos, lastTilePattern);
            engine.changeTilePattern(_pos, avatar);
            TETile tmpTilePattern = lastTilePattern;
            pos = _pos;
            lastTilePattern = _lastTilePattern;
            if (_lastTilePattern.isSameType(Engine.patternTorch)) {
                return 10; // moving into a torch
            }
            if (tmpTilePattern.isSameType(Engine.patternTorch)) {
                return 11; // moving out of a torch
            }
        }
        return 0;
    }

    /**
     * Modify player's health.
     * @param change health change
     * @return true if updated health > 0, false otherwise.
     */
    boolean changeHealth(int change) {
        this.health += change;
        return health > 0;
    }

    /**
     * Set name of player.
     * @param name name of player
     * @return name of player
     */
    String setName(String name) {
        this.name = name;
        return name;
    }
}
