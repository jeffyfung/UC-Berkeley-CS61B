package byow.Core;

import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

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
     * @param gm game mechanics
     * @param engine game engine
     * @param dX displacement along x-axis
     * @param dY displacement along y-axis
     * @return outcome of movement:
     *      -1 - player's health falls to <=0 after movement;
     *       0 - successful movement or no movement;
     *       1 - exit current level and advance;
     */
    int move(GameMechanics gm, Engine engine, int dX, int dY) {
        if (dX == 0 && dY == 0) {
            throw new IllegalArgumentException();
        }
        checkAvatarOrientation(dX);
        Position newPos = new Position(pos.getX() + dX, pos.getY() + dY);
        TETile _lastTilePattern = engine.getTilePattern(newPos);

        if (_lastTilePattern.isSameType(Engine.patternWall)) { return 0; }
        if (!changeHealth(-1)) { return -1; }
        if (_lastTilePattern.isSameType(Engine.patternExit)) { return 1; }

        gm.lightsOn = false;
        gm.portalPreviewPos = null;

        if (_lastTilePattern.isSameType(Engine.patternPortal)) {
            gm.portalPreviewPos = gm.findPortalPairFmPos(newPos).getOtherPortalPos(newPos);
        } else if (_lastTilePattern.isSameType(Engine.patternBread)) {
            changeHealth(Bread.BREAD_BOOST);
            gm.breads.remove(gm.findBreadFmPos(newPos));
            _lastTilePattern = Engine.patternFloor;
        } else if (_lastTilePattern.isSameType(Engine.patternTorch)) {
            gm.lightsOn = true;
        }
        updateObjectPosition(engine, newPos, _lastTilePattern);
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
     * Flips horizontal orientation of player's avatar when appropriate.
     * @param dX displacement of player along x-axis
     */
    void checkAvatarOrientation(int dX) {
        if (dX == -1) {
            changeAvatar(Tileset.AVATAR_LEFT);
        } else if (dX == 1) {
            changeAvatar(Tileset.AVATAR_RIGHT);
        }
    }

    /**
     * Changes avatar of game object.
     * @param t tile of the new avatar
     */
    void changeAvatar(TETile t) {
        Engine.patternPlayerAvatar = t;
        avatar = t;
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
