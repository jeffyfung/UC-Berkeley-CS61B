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

import static byow.Core.GameMechanism.*;

public class Engine {
    public static final int WORLD_WIDTH = 75;
    public static final int WORLD_HEIGHT = 30;
    static final TETile patternWall = Tileset.WALL;
    static final TETile patternRoomFloor = Tileset.FLOOR;
    static final TETile patternHallwayFloor = Tileset.FLOOR;
    static final TETile patternPlayerAvatar = Tileset.AVATAR;
    static final TETile patternExit = Tileset.LOCKED_DOOR;
    Random random;
    TETile[][] tiles;
    TERenderer ter = new TERenderer();
    int turnCount = 0;
    int gameOver = 0;
    String lastTileDescription = "nothing";

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
        while (true) {
            char inputChar = solicitCharInput();
            switch (inputChar) {
                case 'n' -> {
                    drawText(WORLD_WIDTH / 2.0, WORLD_HEIGHT / 2.0 - 1.5 * 4
                            , "Enter Seed Then Press s");
                    runInteractiveEngine(solicitSeed());
                    return;
                }
                case 'l' -> {
                    // load game; add later
                    clearCanvasAndDrawText(WORLD_WIDTH / 2.0, WORLD_HEIGHT / 2.0 - 1.5 * 4
                            , "tbi");
                    return;
                }
                case 'q' -> System.exit(0);
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

    void drawMenu() {
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

    void drawText(double x, double y, String str) {
        StdDraw.text(x, y, str);
        StdDraw.show();
    }

    void drawTextL(double x, double y, String str) {
        StdDraw.textLeft(x, y, str);
        StdDraw.show();
    }

    void drawTextR(double x, double y, String str) {
        StdDraw.textRight(x, y, str);
        StdDraw.show();
    }

    void drawTextWithFont(double x, double y, String str, Font font) {
        StdDraw.setFont(font);
        StdDraw.text(x, y, str);
        StdDraw.show();
    }

    void clearCanvasAndDrawText(double x, double y, String str) {
        StdDraw.clear(StdDraw.BLACK);
        drawText(WORLD_WIDTH / 2.0, WORLD_HEIGHT / 2.0, str);
    }

    void drawHud(int health, String tileDescription, int turn) {
        StdDraw.setPenColor(StdDraw.WHITE);
        drawTextL(0.25, WORLD_HEIGHT - 0.25, String.format("Health: %d", health));
        drawText(WORLD_WIDTH / 2.0, WORLD_HEIGHT - 0.25, tileDescription);
        drawTextR(WORLD_WIDTH - 0.25, WORLD_HEIGHT - 0.25, String.format("Turn: %d", turn));
    }

    private char solicitCharInput() {
        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                char input = Character.toLowerCase(StdDraw.nextKeyTyped());
                System.out.println(input);
                return input;
            }
        }
    }

    private String[] solicitCharAndMouseInput() {
        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                char input = Character.toLowerCase(StdDraw.nextKeyTyped());
                System.out.println(input);
                return new String[]{Character.toString(input), lastTileDescription};
            }
            int cursorX = (int) StdDraw.mouseX();
            int cursorY = (int) StdDraw.mouseY();
            String tileDescription = getTilePattern(cursorX, cursorY).description();
            if (!tileDescription.equals(lastTileDescription)) {
                lastTileDescription = tileDescription;
                return new String[]{"`", tileDescription};
            }
        }
    }


    private int solicitSeed() {
        StringBuilder sb = new StringBuilder();
        char input;
        while (true) {
            input = solicitCharInput();
            if (input == 's') {
                try {
                    return Integer.parseUnsignedInt(sb.toString());
                } catch (NumberFormatException e) {
                    drawText(WORLD_WIDTH / 2.0, WORLD_HEIGHT / 2.0 - 1.5 * 6
                            , "Accept Positive Integer Only! Try Again!");
                    StdDraw.show();
                    sb = new StringBuilder();
                }
            } else {
                sb.append(input);
                drawMenu();
                drawText(WORLD_WIDTH / 2.0, WORLD_HEIGHT / 2.0 - 1.5 * 5, sb.toString());
                drawText(WORLD_WIDTH / 2.0, WORLD_HEIGHT / 2.0 - 1.5 * 4
                        , "Enter Seed Then Press s");
                StdDraw.show();
            }
        }
    }

    public void runInteractiveEngine(int seed) {
        runEngine(seed);
        runInteractiveGameplay();
    }

    public void runEngine(int seed) {
        this.random = new Random(seed);
        ArrayList<Room> rooms = Room.buildRooms(this);
        Room.connectRooms(this, rooms);
        GameMechanism.initializeGameplay(this, rooms);
    }

    public void runInteractiveGameplay() {
        ter.initialize(WORLD_WIDTH, WORLD_HEIGHT + 1, 0, 0);
        ter.renderFrame(tiles);
        String[] input = new String[] {"`", lastTileDescription};
        while (true) {
            drawHud(PLAYER.health, input[1], turnCount);
            input = solicitCharAndMouseInput();
            switch (input[0]) {
                case "w" -> {
                    gameOver = moveGameObject(PLAYER, 0, 1);
                    turnCount += 1;
                }
                case "s" -> {
                    gameOver = moveGameObject(PLAYER, 0, -1);
                    turnCount += 1;
                }
                case "a" -> {
                    gameOver = moveGameObject(PLAYER, -1, 0);
                    turnCount += 1;
                }
                case "d" -> {
                    gameOver = moveGameObject(PLAYER, 1, 0);
                    turnCount += 1;
                }
                case ":" -> {
                    if (solicitCharInput() == 'q') {
                        // save game?
                        System.exit(0);
                    }
                }
                case " " -> turnCount += 1;
                case "`" -> {}
            }
            if (gameOver == 1) {
                StdDraw.setPenColor(StdDraw.WHITE);
                clearCanvasAndDrawText(WORLD_WIDTH / 2.0, WORLD_HEIGHT / 2.0
                        , "Congratulations! You have escaped the Dungeon!");
                return;
            }
            ter.renderFrame(tiles);
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
        if (pos.getX() >= WORLD_WIDTH && pos.getY() >= WORLD_HEIGHT) {
            return Tileset.NOTHING;
        }
        return tiles[pos.getX()][pos.getY()];
    }

    public TETile getTilePattern(int x, int y) {
        if (x >= WORLD_WIDTH || y >= WORLD_HEIGHT) {
            return Tileset.NOTHING;
        }
        return tiles[x][y];
    }

}
