package byow.Core;

import java.util.LinkedList;

public class Position {
    int x;
    int y;

    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    static Double dist(Position pos1, Position pos2) {
        if (pos1 == null || pos2 == null) {
            return null;
        }
        return Math.sqrt((pos1.x - pos2.x) * (pos1.x - pos2.x)
                + (pos1.y - pos2.y) * (pos1.y - pos2.y));
    }
}
