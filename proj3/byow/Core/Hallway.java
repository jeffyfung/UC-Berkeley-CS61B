package byow.Core;

import java.util.Set;

public class Hallway {
    /** Path i.e. floor of the hallway. Widths are fixed to 1. */
    private Set<Position> path;
    /** Walls surrounding path. */
    private Set<Position> walls;

    Hallway(Set<Position> path, Set<Position> walls) {
        this.path = path;
        this.walls = walls;
    }

    Set<Position> getPath() {
        return path;
    }

    Set<Position> getWalls() {
        return walls;
    }
}
