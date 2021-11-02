package byow.Core;

import byow.TileEngine.TETile;

import java.io.Serializable;

import static byow.Core.GameMechanism.ENGINE;

public class GameObject implements Serializable {
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
        if (_lastTilePattern.isSameType(Engine.patternWall)) {
            return 0;
        }
        if (_lastTilePattern.isSameType(Engine.patternExit)) {
            return 1;
        }
        ENGINE.changeTilePattern(pos, lastTilePattern);
        ENGINE.changeTilePattern(_pos, avatar);
        pos = _pos;
        lastTilePattern = _lastTilePattern;
        return 0;
    }

}
