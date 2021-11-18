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
     * @param gm game mechanics
     * @param engine game engine
     * @param dX displacement along x-axis
     * @param dY displacement along y-axis
     * @return outcome of movement
     */
    int move(GameMechanics gm, Engine engine, int dX, int dY) {
        Position newPos = new Position(pos.getX() + dX, pos.getY() + dY);
        if (dX != 0 || dY != 0) {
            updateObjectPosition(engine, newPos, engine.getTilePattern(newPos));
        }
        return 0;
    }

    void updateObjectPosition(Engine engine, Position newPos, TETile _lastTilePattern) {
        engine.changeTilePattern(pos, lastTilePattern);
        engine.changeTilePattern(newPos, avatar);
        pos = newPos;
        lastTilePattern = _lastTilePattern;
    }
}
