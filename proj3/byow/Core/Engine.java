package byow.Core;

import byow.Input.InputSource;
import byow.Input.StringInputDevice;
import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import edu.princeton.cs.introcs.StdDraw;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

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
        drawSetting();
        drawMenu();
        boolean loop = true;
        while (loop) {
            char inputChar = solicitCharInput();
            switch (inputChar) {
                case 'n':
                    drawText(WORLD_WIDTH / 2.0, WORLD_HEIGHT / 2.0 - 1.5*4
                            , "Enter Seed Then Press s");
                    runEngine(solicitSeed());
                    // TODO: run interactive engine
                    loop = false;
                    break;
                case 'l':
                    // load game; add later
                    clearCanvasAndDrawText(WORLD_WIDTH / 2.0, WORLD_HEIGHT / 2.0 - 1.5*4
                            , "tbi");
                    loop = false;
                    break;
                case 'q':
                    System.exit(0);
                default:
            }
        }
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
            // TODO: cater for other inputs e.g. L, Q, S etc
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
        runEngine(seed);
        return tiles;
    }

    private void drawSetting() {
        StdDraw.setCanvasSize(WORLD_WIDTH * 16, (WORLD_HEIGHT + 3) * 16);
        StdDraw.setXscale(0, WORLD_WIDTH);
        StdDraw.setYscale(0, WORLD_HEIGHT);
        StdDraw.setPenColor(StdDraw.WHITE);
        StdDraw.enableDoubleBuffering();
    }

    private void drawMenu() {
        Font titleFont = new Font("Helvetica", Font.BOLD, 40);
        StdDraw.setFont(titleFont);
        StdDraw.clear(Color.BLACK);
        StdDraw.text(WORLD_WIDTH / 2.0, WORLD_HEIGHT * 3 / 4.0, "Simple Dungeon Game");
        Font optionsFont = new Font("Helvetica", Font.PLAIN, 25);
        StdDraw.setFont(optionsFont);
        StdDraw.text(WORLD_WIDTH / 2.0, WORLD_HEIGHT / 2.0, "New Game (N)");
        StdDraw.text(WORLD_WIDTH / 2.0, WORLD_HEIGHT / 2.0 - 1.5, "Load Game (L)");
        StdDraw.text(WORLD_WIDTH / 2.0, WORLD_HEIGHT / 2.0 - 1.5*2, "Quit (Q)");
        StdDraw.show();
    }

    private void drawText(double x, double y, String str) {
        StdDraw.text(x, y, str);
        StdDraw.show();
    }

    private void drawTextWithFont(double x, double y, String str, Font font) {
        StdDraw.setFont(font);
        StdDraw.text(x, y, str);
        StdDraw.show();
    }

    private void clearCanvasAndDrawText(double x, double y, String str) {
        StdDraw.clear(Color.BLACK);
        StdDraw.text(x, y, str);
        StdDraw.show();
    }

    private char solicitCharInput() {
        char input = ' ';
        char out = ' ';
        while (input == ' ') {
            if (StdDraw.hasNextKeyTyped()) {
                input = StdDraw.nextKeyTyped();
                out = Character.toLowerCase(input);
            }
        }
        System.out.println(out);
        return out;
    }

    private int solicitSeed() {
        StringBuilder sb = new StringBuilder();
        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                char input = Character.toLowerCase(StdDraw.nextKeyTyped());
                System.out.println(input);
                if (input == 's') {
                    try {
                        return Integer.parseUnsignedInt(sb.toString());
                    } catch (NumberFormatException e) {
                        drawText(WORLD_WIDTH / 2.0, WORLD_HEIGHT / 2.0 - 1.5*6
                                , "Accept Positive Integer Only! Try Again!");
                        StdDraw.show();
                        sb = new StringBuilder();
                    }
                } else {
                    sb.append(input);
                    drawMenu();
                    drawText(WORLD_WIDTH / 2.0, WORLD_HEIGHT / 2.0 - 1.5*5, sb.toString());
                    drawText(WORLD_WIDTH / 2.0, WORLD_HEIGHT / 2.0 - 1.5*4
                            , "Enter Seed Then Press s");
                    StdDraw.show();
                }
            }
        }
    }

    public String toString() {
        return TETile.toString(tiles);
    }

    /** Change pattern of the specific tile */
    public TETile[][] changeTilePattern(Position pos, TETile newTilePattern) {
        tiles[pos.getX()][pos.getY()] = newTilePattern;
        return tiles;
    }

    /** Change pattern of the specific tile */
    public TETile[][] changeTilePattern(int x, int y, TETile newTilePattern) {
        tiles[x][y] = newTilePattern;
        return tiles;
    }

    public TETile getTilePattern(Position pos) {
        return tiles[pos.getX()][pos.getY()];
    }

    public TETile getTilePattern(int x, int y) {
        return tiles[x][y];
    }

    public void runEngine(int seed) {
        this.random = new Random(seed);
        ArrayList<Room> rooms = Room.buildRooms(this);
        Room.connectRooms(this, rooms);
        GameMechanism.initializeGameplay(this, rooms);

        ter.initialize(WORLD_WIDTH, WORLD_HEIGHT);
        ter.renderFrame(tiles);
    }
}
