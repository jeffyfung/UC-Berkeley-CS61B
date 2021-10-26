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

    // TODO:　set bounds for move
    // TODO: exit if run into exit door
    public GameObject move(int dX, int dY) {
        Position _pos = new Position(pos.getX() + dX, pos.getY() + dY);
        TETile _lastTilePattern = ENGINE.getTilePattern(_pos);
        ENGINE.changeTilePattern(pos, lastTilePattern);
        ENGINE.changeTilePattern(_pos, avatar);
        pos = _pos;
        lastTilePattern = _lastTilePattern;
        return this;
    }

}
