package byow.Core;

import byow.TileEngine.TETile;

public class Player {
    Position pos;
    TETile avatar;

    public Player(Position pos, TETile avatar) {
        this.pos = pos;
        this.avatar = avatar;
    }

    public void movePlayer(Position newPosition) {

    }

    public Position getPlayerPos() {
        return this.pos;
    }

    public TETile getAvatar() {
        return this.avatar;
    }
}
