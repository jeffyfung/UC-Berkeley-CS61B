package byow.Core;

import byow.TileEngine.TETile;

public class Player extends GameObject {

    public Player(Position pos, TETile avatar) {
        super(pos, avatar);
        this.lastTilePattern = Engine.patternRoomFloor;
    }

//    public Position getPos() {
//        return this.pos;
//    }
//
//    public TETile getAvatar() {
//        return this.avatar;
//    }
}
