package byow.Core;

import byow.TileEngine.TETile;

import java.io.Serializable;
import java.util.NoSuchElementException;

/**
 * Sub-class of GameObject.
 */
public class PortalPair implements Serializable {
    /** Minimum distance between two sides of a portal. */
    static final int MIN_PAIR_DIST = 25;
    /** One side of the pair. */
    private final Portal a;
    /** The other side of the pair. */
    private final Portal b;

    /** Constructor of the class. */
    PortalPair(Position posA, Position posB, TETile pattern) {
        this.a = new Portal(posA, pattern);
        this.b = new Portal(posB, pattern);
    }

    /** Get one side of the pair. */
    Portal getPortal() {
        return a;
    }

    /** Get the other side of the pair. */
    Portal getOtherPortal() {
        return b;
    }

    /**
     * Given the position of either side of the pair, get the position of the other side.
     * @param pos position of either side of the pair
     * */
    Position getOtherPortalPos(Position pos) {
        if (pos.equals(a.pos)) {
            return b.pos;
        }
        if (pos.equals(b.pos)) {
            return a.pos;
        }
        throw new NoSuchElementException();
    }

    /**
     * Given a portal at either side of the pair, get the position of the other side.
     * @param portal either side of the pair
     * */
    Position getOtherPortalPos(Portal portal) {
        return getOtherPortalPos(portal.pos);
    }

    /** Side of a portal pair. */
    class Portal extends GameObject implements Serializable {

        /** Constructor of the nested class. */
        Portal(Position pos, TETile avatar) {
            super(pos, avatar);
        }

        /** Portal cannot move. RuntimeException is thrown if this method is called. */
        int move(GameMechanics gm, Engine engine, int dX, int dY) {
            throw new RuntimeException("Portal cannot be moved");
        }
    }
}
