package byow.Core;

import byow.TileEngine.TETile;

/**
 * Sub-class of GameObject.
 */
public class Player extends GameObject {
    /** Health of player. Starts with 10. Game ends when health <= 0  */
    // TODO: no use for now
    int health = 10;

    /**
     * Constructor for the class.
     * @param pos position of player
     * @param avatar tile pattern representing the player
     */
    public Player(Position pos, TETile avatar) {
        super(pos, avatar);
        this.lastTilePattern = Engine.patternFloor;
    }
}
