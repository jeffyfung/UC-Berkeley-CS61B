package byow.Core;

import byow.TileEngine.TETile;
import java.io.Serializable;

/**
 * Super class for all game objects.
 */
public class GameObject implements Serializable {
    /** Position of gameObject. */
    Position pos;
    /** Tile pattern representing the gameObject. */
    TETile avatar;
    /** Pattern of the tile before gameObject moves there. */
    TETile lastTilePattern;

    /**
     * Constructor for GameObjects.
     * @param pos position of game object
     * @param avatar tile pattern representing the game object
     */
    GameObject(Position pos, TETile avatar) {
        this.pos = pos;
        this.avatar = avatar;
    }

    /**
     * Moves gameObject.
     * @param engine game engine
     * @param dX displacement along x-axis
     * @param dY displacement along y-axis
     * @return outcome of movement: 0 - success; 1 - advance level; 2 - no movement
     */
    int move(Engine engine, int dX, int dY) {
        Position _pos = new Position(pos.getX() + dX, pos.getY() + dY);
        TETile _lastTilePattern = engine.getTilePattern(_pos);
        if (_lastTilePattern.isSameType(Engine.patternExit)) {
            return 1;
        }
        if (_lastTilePattern.isSameType(Engine.patternWall)) {
            return 2;
        }
        if (dX != 0 || dY != 0) {
            engine.changeTilePattern(pos, lastTilePattern);
            engine.changeTilePattern(_pos, avatar);
            pos = _pos;
            lastTilePattern = _lastTilePattern;
        }
        return 0;
    }

}
