package byow.Core;

import byow.TileEngine.TETile;

public class Player extends GameObject {
    int health = 10;

    public Player(Position pos, TETile avatar) {
        super(pos, avatar);
        this.lastTilePattern = Engine.patternRoomFloor;
    }
}
