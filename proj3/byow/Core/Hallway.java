package byow.Core;

import java.util.List;

public class Hallway {
    private List<Position> path;
    private List<Position> walls;

    Hallway(List<Position> path, List<Position> walls) {
        this.path = path;
        this.walls = walls;
    }

    List<Position> getPath() {
        return path;
    }

    List<Position> getWalls() {
        return walls;
    }
}
