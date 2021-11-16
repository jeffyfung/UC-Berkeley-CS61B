package byow.Core;

import byow.TileEngine.TETile;

/**
 * Sub-class of GameObject.
 */
public class Bread extends GameObject {
    /** Amount of health boost when consumed by player. */
    static final int BREAD_BOOST = 20;

    /** Constructor of the class. */
    Bread(Position pos, TETile avatar) {
        super(pos, avatar);
        this.lastTilePattern = Engine.patternFloor;
    }

    /** Torch cannot move. RuntimeException is thrown if this method is called. */
    int move(GameMechanics gm, Engine engine, int dX, int dY) {
        throw new RuntimeException("Bread cannot be moved");
    }

}
