package byow.Core;

import byow.TileEngine.TETile;

import static byow.Core.GameMechanism.ENGINE;

public class GameObject {
    Position pos;
    TETile avatar;
    TETile lastTilePattern;

    public GameObject(Position pos, TETile avatar) {
        this.pos = pos;
        this.avatar = avatar;
    }

    public int move(int dX, int dY) {
        Position _pos = new Position(pos.getX() + dX, pos.getY() + dY);
        TETile _lastTilePattern = ENGINE.getTilePattern(_pos);
        // check if new pos is a wall
        if (_lastTilePattern.equals(Engine.patternWall)) {
            return 0;
        }
        // check if new pos is an exit
        if (_lastTilePattern.equals(Engine.patternExit)) {
            return 1;
        }
        ENGINE.changeTilePattern(pos, lastTilePattern);
        ENGINE.changeTilePattern(_pos, avatar);
        pos = _pos;
        lastTilePattern = _lastTilePattern;
        return 0;
    }

}
