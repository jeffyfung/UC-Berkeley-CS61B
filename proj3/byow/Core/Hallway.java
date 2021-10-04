package byow.Core;

import java.util.Set;

public class Hallway {
    private Set<Position> path;
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
