package byow.Core;

import byow.TileEngine.TETile;
import java.io.Serializable;
import static byow.Core.GameMechanism.ENGINE;

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
    public GameObject(Position pos, TETile avatar) {
        this.pos = pos;
        this.avatar = avatar;
    }

    /**
     * Moves gameObject.
     * @param dX displacement along x-axis
     * @param dY displacement along y-axis
     * @return whether the movement leads to the end of a game
     */
    public int move(int dX, int dY) {
        Position _pos = new Position(pos.getX() + dX, pos.getY() + dY);
        TETile _lastTilePattern = ENGINE.getTilePattern(_pos);
        if (_lastTilePattern.isSameType(Engine.patternWall)) {
            return 0;
        }
        if (_lastTilePattern.isSameType(Engine.patternExit)) {
            return 1;
        }
        ENGINE.changeTilePattern(pos, lastTilePattern);
        ENGINE.changeTilePattern(_pos, avatar);
        pos = _pos;
        lastTilePattern = _lastTilePattern;
        return 0;
    }

}
