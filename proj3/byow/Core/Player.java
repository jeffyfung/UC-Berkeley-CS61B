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
     * Moves player.
     * @param engine game engine
     * @param dX displacement along x-axis
     * @param dY displacement along y-axis
     * @return outcome of movement: 0 - success; 1 - advance level; 2 - no movement; 3 - game ends
     * as player's health falls to 0;
     */
    int move(Engine engine, int dX, int dY) {
        int out = super.move(engine, dX, dY);
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
    void changeHealth(int change) {
        this.health += change;
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
