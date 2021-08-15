package byow.Core;

import java.util.LinkedList;

public class Position {
    int x;
    int y;

    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /** Returns 1 if the given array of position forms a vertical line, or 2 if a horizontal line
     * is formed. Returns 0 if neither a vertical nor horizontal line is formed.
     * @param line A array of adjacent positions
     * @return 0, 1 or 2
     */
    static int checkOrientationOfLine(LinkedList<Position> line) {
        // TODO: change all lines to LinkedList<Position> ???
        if (line.get(0).x == line.get(line.size() - 1).x) {
            return 1;
        }
        else if (line.get(0).y == line.get(line.size() - 1).y) {
            return 2;
        }
        else {
            return 0;
        }
    }
}
