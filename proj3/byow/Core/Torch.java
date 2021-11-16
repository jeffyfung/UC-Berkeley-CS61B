package byow.Core;

import byow.TileEngine.TETile;

/**
 * Sub-class of GameObject.
 */
public class Torch extends GameObject {

    /** Constructor of the class. */
    Torch(Position pos, TETile avatar) {
        super(pos, avatar);
        this.lastTilePattern = Engine.patternFloor;
    }

    /** Torch cannot move. RuntimeException is thrown if this method is called. */
    int move(GameMechanics gm, Engine engine, int dX, int dY) {
        throw new RuntimeException("Torch cannot be moved");
    }
}
