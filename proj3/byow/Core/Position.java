package byow.Core;

import java.io.Serializable;

/**
 * Class to represent locations in game state.
 */
public class Position implements Serializable {
    /** A size 2 array tracking the x and y coordinates of the position. */
    int[] xy;

    public Position(int x, int y) {
        this.xy = new int[]{x, y};
    }

    /** Calculate pythagorean distance between pos1 and pos2. **/
    static Double dist(Position pos1, Position pos2) {
        if (pos1 == null || pos2 == null) {
            return null;
        }
        return Math.sqrt((pos1.xy[0] - pos2.xy[0]) * (pos1.xy[0] - pos2.xy[0])
                + (pos1.xy[1] - pos2.xy[1]) * (pos1.xy[1] - pos2.xy[1]));
    }

    /** Calculate the area inside a rectangle bounded by the give lowerLeftCorner and
     * upperRightCorner. */
    static Integer rectangularArea(Position lowerLeftCorner, Position upperRightCorner) {
        if (lowerLeftCorner == null || upperRightCorner == null
                || lowerLeftCorner.equals(upperRightCorner)) {
            return null;
        }
        return (upperRightCorner.xy[0] - lowerLeftCorner.xy[0])
                * (upperRightCorner.xy[1] - lowerLeftCorner.xy[1]);
    }

    /** Compare the 2 given positions on the given axis. Axis = 0 and axis = 1 refer to comparison
     * on x-axis and y-axis respectively. Return true if cmp is greater than or equal to pos on
     * the given axis and vice versa. */
    static Boolean compare(Position cmp, Position pos, int axis) {
        return cmp.xy[axis] >= pos.xy[axis];
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Position)) {
            return false;
        }
        Position pos = (Position) o;
        return this.xy[0] == pos.xy[0] && this.xy[1] == pos.xy[1];
    }

    public int getX() {
        return xy[0];
    }

    public int getY() {
        return xy[1];
    }
}
