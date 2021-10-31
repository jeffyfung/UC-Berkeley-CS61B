package byow.Core;

import byow.TileEngine.TETile;

public class Player extends GameObject {

    public Player(Position pos, TETile avatar) {
        super(pos, avatar);
        this.lastTilePattern = Engine.patternRoomFloor;
    }
}
