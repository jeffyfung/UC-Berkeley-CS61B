package byow.Core;

import byow.TileEngine.TETile;

/**
 * Sub-class of GameObject.
 */
public class Player extends GameObject {
    /** Health of player. Game ends when health <= 0  */
    int health = 100;

    /**
     * Constructor for the class.
     * @param pos position of player
     * @param avatar tile pattern representing the player
     */
    public Player(Position pos, TETile avatar) {
        super(pos, avatar);
        this.lastTilePattern = Engine.patternFloor;
    }

    /**
     * Moves player.
     * @param dX displacement along x-axis
     * @param dY displacement along y-axis
     * @return outcome of movement: 0 - success; 1 - advance level; 2 - no change; 3 - game ends
     * as player's health falls <= 0;
     */
    public int move(int dX, int dY) {
        int out = super.move(dX, dY);
        if (out == 0) {
            changeHealth(-1);
            if (health <= 0) {
                return 3;
            }
        }
        return out;
    }

    /**
     * Modify player's health.
     * @param change health change
     */
    public void changeHealth(int change) {
        this.health += change;
    }
}
