package byow.Core;

import byow.Input.InputSource;
import byow.Input.StringInputDevice;
import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.util.ArrayList;
import java.util.Random;

import static byow.Core.Room.buildRooms;
import static byow.Core.Room.connectRooms;

public class Engine {
    TERenderer ter = new TERenderer();
    Random random;
    public static final int WORLD_WIDTH = 75;
    public static final int WORLD_HEIGHT = 30;
    TETile[][] tiles;

    /** Initial the world with empty tiles. */
    public Engine() {
        this.tiles = new TETile[WORLD_WIDTH][WORLD_HEIGHT];
        for (int x = 0; x < WORLD_WIDTH; x += 1) {
            for (int y = 0; y < WORLD_HEIGHT; y += 1) {
                tiles[x][y] = Tileset.NOTHING;
            }
        }
    }

    /**
     * Method used for exploring a fresh world. This method should handle all inputs,
     * including inputs from the main menu.
     */
    public void interactWithKeyboard() {
    }

    /**
     * Method used for autograding and testing your code. The input string will be a series
     * of characters (for example, "n123sswwdasdassadwas", "n123sss:q", "lwww". The engine should
     * behave exactly as if the user typed these characters into the engine using
     * interactWithKeyboard.
     *
     * Recall that strings ending in ":q" should cause the game to quite save. For example,
     * if we do interactWithInputString("n123sss:q"), we expect the game to run the first
     * 7 commands (n123sss) and then quit and save. If we then do
     * interactWithInputString("l"), we should be back in the exact same state.
     *
     * In other words, both of these calls:
     *   - interactWithInputString("n123sss:q")
     *   - interactWithInputString("lww")
     *
     * should yield the exact same world state as:
     *   - interactWithInputString("n123sssww")
     *
     * @param input the input string to feed to your program
     * @return the 2D TETile[][] representing the state of the world
     */
    public TETile[][] interactWithInputString(String input) {
        InputSource inputSource = new StringInputDevice(input.toUpperCase());
        Integer seed = null;
        boolean startSeed = false;
        while (inputSource.possibleNextInput()) {
            char c = inputSource.getNextKey();
            if (c == 'N') {
                startSeed = true;
                continue;
            } else if (c == 'S') {
                startSeed = false;
            }
            // other inputs e.g. L, Q, S etc
            if (startSeed) {
                Integer num = Character.getNumericValue(c);
                seed = seed == null ? num : seed * 10 + num;
            }
        }
        if (startSeed || seed == null) {
            System.out.println("Input must contain a positive integer seed phases bounded by N " +
                    "and S, for example, N123S. Please re-enter.");
            return null;
        }
        this.random = new Random(seed);

        ArrayList<Room> rooms = Room.buildRooms(this);
        Room.connectRooms(this, rooms);
        GameMechanism.initializeGameplay(this, rooms);
        System.out.println("done with initialization");

        ter.initialize(WORLD_WIDTH, WORLD_HEIGHT);
        ter.renderFrame(tiles);
        return tiles;
    }

    public String toString() {
        return TETile.toString(tiles);
    }

    // TODO: to remove
    public static void main(String[] args){
        Engine test = new Engine();
        ArrayList<Room> rooms = buildRooms(test);
        connectRooms(test, rooms);

        test.ter.initialize(WORLD_WIDTH, WORLD_HEIGHT);
        test.ter.renderFrame(test.tiles);
    }

}
