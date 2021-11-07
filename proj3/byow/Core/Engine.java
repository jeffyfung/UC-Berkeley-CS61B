package byow.Core;

import byow.Input.InputSource;
import byow.Input.StringInputDevice;
import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import edu.princeton.cs.introcs.StdDraw;

import java.awt.*;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import static byow.Core.GameMechanism.PLAYER;
import static byow.Core.GameMechanism.moveGameObject;
import static byow.Core.PersistenceUtils.*;

/**
 * Runs the game. Called from Main and allows either interactive keyboard input or
 * input string from user.
 */
public class Engine {
    /** Width of display window. */
    public static final int WORLD_WIDTH = 75;
    /** Height of display window. */
    public static final int WORLD_HEIGHT = 30;
    /** X-axis distance between bottom of display window and bottom of frame to draw tiles. */
    static final int WORLD_XOFFSET = 0;
    /** Y-axis distance between bottom of display window and bottom of frame to draw tiles. */
    static final int WORLD_YOFFSET = 2;
    static final TETile patternWall = Tileset.WALL;
    static final TETile patternRoomFloor = Tileset.FLOOR;
    static final TETile patternHallwayFloor = Tileset.FLOOR;
    static final TETile patternPlayerAvatar = Tileset.AVATAR;
    static final TETile patternExit = Tileset.LOCKED_DOOR;
    static final File CWD = new File(System.getProperty("user.dir"));
    /** Directory for saving and loading game. */
    static final File GAMESAVE = join(CWD, ".gamesave");
    /** RNG */
    Random random;
    /** 2D array of tiles representing game state. */
    TETile[][] tiles;
    /** Renderer for tiles. */
    TERenderer ter = new TERenderer();
    /** Tracks the number of turn passed. Do not reset when loading a game. */
    int turnCount = 0;
    /** Tracks whether the game is finished. */
    int gameOver = 0;
    String lastTileDescription = "";

    /** Constructor for Engine objects. Initialize the game state with empty tiles. */
    public Engine() {
        this.tiles = new TETile[WORLD_WIDTH][WORLD_HEIGHT];
        for (int x = 0; x < WORLD_WIDTH; x += 1) {
            for (int y = 0; y < WORLD_HEIGHT; y += 1) {
                tiles[x][y] = Tileset.NOTHING;
            }
        }
    }

    /**
     * Run the game engine. This method should handle all inputs, including inputs from the main
     * menu. Uses "wasd" keys to move player. Press ":q" to save game and quit.
     */
    // TODO: cater for other inputs e.g. L, Q, S etc
    public void interactWithKeyboard() {
        setUpPersistence();
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
                    boolean loadStatus = loadGame(true);
                    if (loadStatus) {
                        runInteractiveGameplay();
                    }
                    return;
                }
                case 'q' -> System.exit(0);
            }
        }
    }

    /**
     * Run the game engine. The engine should behave exactly as if the user typed these characters
     * into the engine using interactWithKeyboard. If the first valid input character is "l", the
     * last saved game state, if any, will be loaded. For instance, the call
     * interactWithInputString("n123sss:q") followed by the call interactWithInputString("lww")
     * should yield the exact same world state as interactWithInputString("n123sssww").
     * @param input the input string to feed to your program
     * @return the 2D TETile[][] representing the state of the world
     */
    public TETile[][] interactWithInputString(String input) {
        setUpPersistence();
        InputSource inputSource = new StringInputDevice(input.toLowerCase());
        switch (collectMenuOption(inputSource)) {
            case 'n' -> {
                int seed = collectSeedFromInputString(inputSource);
                return runStaticEngine(seed, inputSource);
            }
            case 'l' -> {
                boolean loadStatus = loadGame(false);
                if (loadStatus) {
                    return runStaticGamePlay(inputSource);
                } else {
                    return null;
                }
            }
            default -> {
                return null;
            }
        }
    }

    /**
     * Initialize setting for drawing the menu and gameplay. Used when users enter the game by
     * calling interactWithKeyboard().
     */
    private void drawSetting() {
        StdDraw.setCanvasSize(WORLD_WIDTH * 16, (WORLD_HEIGHT + 3) * 16);
        StdDraw.setXscale(0, WORLD_WIDTH);
        StdDraw.setYscale(0, WORLD_HEIGHT);
        StdDraw.setPenColor(StdDraw.WHITE);
        StdDraw.enableDoubleBuffering();
    }

    /** Draw game menu. */
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

    /**
     * Helper function for drawing centered text.
     * @param x x coordinate of center of the string to be drawn.
     * @param y y coordinate of center of the string to be drawn.
     * @param str string to be drawn
     * */
    void drawText(double x, double y, String str) {
        StdDraw.text(x, y, str);
        StdDraw.show();
    }

    /**
     * Helper function for drawing left aligned text.
     * @param x x coordinate of left boundary of the string to be drawn.
     * @param y y coordinate of left boundary of the string to be drawn.
     * @param str string to be drawn
     */
    void drawTextL(double x, double y, String str) {
        StdDraw.textLeft(x, y, str);
        StdDraw.show();
    }

    /**
     * Helper function for drawing right aligned text.
     * @param x x coordinate of right boundary of the string to be drawn.
     * @param y y coordinate of right boundary of the string to be drawn.
     * @param str string to be drawn
     */
    void drawTextR(double x, double y, String str) {
        StdDraw.textRight(x, y, str);
        StdDraw.show();
    }

    /**
     * Helper function for drawing centered text with new font.
     * @param x x coordinate of right boundary of the string to be drawn.
     * @param y y coordinate of right boundary of the string to be drawn.
     * @param str string to be drawn
     * @param font font to be used
     */
    void drawTextWithFont(double x, double y, String str, Font font) {
        StdDraw.setFont(font);
        StdDraw.text(x, y, str);
        StdDraw.show();
    }

    /**
     * Erase all drawings on the canvas and draw centered text.
     * @param x x coordinate of right boundary of the string to be drawn.
     * @param y y coordinate of right boundary of the string to be drawn.
     * @param str string to be drawn
     */
    void clearCanvasAndDrawText(double x, double y, String str) {
        StdDraw.clear(StdDraw.BLACK);
        drawText(WORLD_WIDTH / 2.0, WORLD_HEIGHT / 2.0, str);
    }

    /**
     * Draw HUD at the bottom of the window during gameplay. The HUD displays information about
     * player's health, description of a tile, and the number of turn passed.
     * @param health health of the player
     * @param tileDescription description of a tile
     * @param turn number of turn passed
     */
    void drawHud(int health, String tileDescription, int turn) {
        StdDraw.setPenColor(StdDraw.GRAY);
        StdDraw.filledRectangle(WORLD_WIDTH / 2.0, 0.75, WORLD_WIDTH / 2.0, 0.75);
        StdDraw.setPenColor(StdDraw.WHITE);
        drawTextL(0.5, 0.75, String.format("Health: %d", health));
        drawText(WORLD_WIDTH / 2.0, 0.75, tileDescription);
        drawTextR(WORLD_WIDTH - 0.25, 0.75, String.format("Turn: %d", turn));
    }

    /**
     * Gets each keyboard input as character. Converts to lower case alphabets if applicable.
     * @return keyboard input
     */
    private char solicitCharInput() {
        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                char input = Character.toLowerCase(StdDraw.nextKeyTyped());
                System.out.println(input);
                return input;
            }
        }
    }

    /**
     * Gets each keyboard input from user and description of the tile that the mouse cursor is
     * currently over. The function only returns when there is any unparsed input or a change in
     * tile description.
     * @return input character and description of the mouse-over tile
     */
    private String[] solicitCharAndMouseInput() {
        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                char input = Character.toLowerCase(StdDraw.nextKeyTyped());
                System.out.println(input);
                return new String[]{Character.toString(input), lastTileDescription};
            }
            int cursorX = (int) StdDraw.mouseX() - WORLD_XOFFSET;
            int cursorY = (int) StdDraw.mouseY() - WORLD_YOFFSET;
            String tileDescription;
            if (cursorX < 0 || cursorY < 0) {
                tileDescription = Tileset.NOTHING.description();
            } else {
                tileDescription = getTilePattern(cursorX, cursorY).description();
            }
            if (!tileDescription.equals(lastTileDescription)) {
                lastTileDescription = tileDescription;
                return new String[]{"`", tileDescription};
            }
        }
    }

    /**
     * Gets seed for random number generation from user. The input must end with "s". If the
     * input contains alphabets, except for the "s" at the end, user will be asked to enter the
     * seed again and input is reset.
     * @return seed for RNG.
     */
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

    /**
     * Extract character for menu options from user-input string. Return a valid character once
     * it is parsed.
     */
    private char collectMenuOption(InputSource inputSource) {
        while (inputSource.possibleNextInput()) {
            char c = inputSource.getNextKey();
            switch (c) {
                case 'n', 'l', 'q' -> {
                    return c;
                }
            }
        }
        throw new IllegalArgumentException();
    }

    /**
     * Extract characters that represents the seed for RNG from user-input string. Reset the seed
     * if an invalid character (i.e. alphabetic character) is parsed.
     */
    private int collectSeedFromInputString(InputSource inputSource) {
        int seed = 0;
        while (inputSource.possibleNextInput()) {
            char c = inputSource.getNextKey();
            if (c == 's') {
                return seed;
            } else if (Character.isAlphabetic(c)) {
                seed = 0;
            } else {
                seed = seed * 10 + Character.getNumericValue(c);
            }
        }
        throw new IllegalArgumentException();
    }

    /**
     * Initializes engine and gameplay setting. Then run interactive gameplay. Called when user
     * calls interactWithKeyboard().
     * @param seed seed for RNG
     */
    void runInteractiveEngine(int seed) {
        runEngine(seed);
        runInteractiveGameplay();
    }

    /**
     * Initializes engine and gameplay setting. Then change game state according to input string
     * sequence from user. Return the final game state. Called when user calls
     * interactWithKeyboard().
     * @param seed seed for RNG
     * @param inputSource parses input string from user
     * @return array representing tiles in current game state
     */
    TETile[][] runStaticEngine(int seed, InputSource inputSource) {
        runEngine(seed);
        return runStaticGamePlay(inputSource);
    }

    /**
     * Pseudo-randomly generates rooms and hallways. Then generates gameplay features e.g. player
     * and exit.
     * @param seed seed for RNG
     */
    void runEngine(int seed) {
        this.random = new Random(seed);
        ArrayList<Room> rooms = Room.buildRooms(this);
        Room.connectRooms(this, rooms);
        GameMechanism.initializeGameplay(this, rooms);
    }

    /**
     * Change the game state according to keyboard input from user in a turn-based way, and
     * draw the game state and HUD accordingly. "wasd" moves player, ":q" saves game and quit.
     * Draw a message if the player successfully finishes the game.
     */
    void runInteractiveGameplay() {
        ter.initialize(WORLD_WIDTH + WORLD_XOFFSET, WORLD_HEIGHT + WORLD_YOFFSET
                , WORLD_XOFFSET, WORLD_YOFFSET);
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
                        saveGame();
                        System.exit(0);
                    }
                }
                case " " -> turnCount += 1;
                case "`" -> {}
            }
            if (gameOver == 1) {
                System.out.println("Congratulations! You have escaped the Dungeon!");
                StdDraw.setPenColor(StdDraw.WHITE);
                clearCanvasAndDrawText(WORLD_WIDTH / 2.0, WORLD_HEIGHT / 2.0
                        , "Congratulations! You have escaped the Dungeon!");
                return;
            }
            ter.renderFrame(tiles);
        }
    }

    /**
     * Similar to runInteractiveGameplay() but takes user-input string.
     * @param inputSource parses input string from user
     * @return array representing game state
     */
    TETile[][] runStaticGamePlay(InputSource inputSource) {
        while (inputSource.possibleNextInput()) {
            char c = inputSource.getNextKey();
            switch (c) {
                case 'w' -> {
                    gameOver = moveGameObject(PLAYER, 0, 1);
                    turnCount += 1;
                }
                case 's' -> {
                    gameOver = moveGameObject(PLAYER, 0, -1);
                    turnCount += 1;
                }
                case 'a' -> {
                    gameOver = moveGameObject(PLAYER, -1, 0);
                    turnCount += 1;
                }
                case 'd' -> {
                    gameOver = moveGameObject(PLAYER, 1, 0);
                    turnCount += 1;
                }
                case ':' -> {
                    if (inputSource.getNextKey() == 'q') {
                        // save game?
                        saveGame();
                        System.exit(0);
                    }
                }
                case ' ' -> turnCount += 1;
            }
            if (gameOver == 1) {
                System.out.println("Congratulations! You have escaped the Dungeon!");
                System.exit(0);
            }
        }
        return tiles;
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

    /** Get TETile at specific position. */
    public TETile getTilePattern(Position pos) {
        if (pos.getX() >= WORLD_WIDTH && pos.getY() >= WORLD_HEIGHT) {
            return Tileset.NOTHING;
        }
        return tiles[pos.getX()][pos.getY()];
    }

    /** Get TETile at specific position. */
    public TETile getTilePattern(int x, int y) {
        if (x >= WORLD_WIDTH || y >= WORLD_HEIGHT) {
            return Tileset.NOTHING;
        }
        return tiles[x][y];
    }

    /**
     * Create a directory, if not already exists, to store engine state and states of game
     * objects for saving and loading games.
     */
    static void setUpPersistence() {
        GAMESAVE.mkdir();
    }

    /** Saves engine state and states of game objects to .gamesave directory. */
    void saveGame() {
        saveEngineState();
        GameMechanism.saveGameObjects();
    }

    /**
     * Serializes and saves Random object, game state array and turn count to a
     * .gamesave/engineState file.
     * */
    void saveEngineState() {
        HashMap<String, Serializable> engineState = new HashMap<>();
        engineState.put("random", random);
        engineState.put("tiles", tiles);
        engineState.put("turnCount", turnCount);
        writeObject(join(GAMESAVE, "engineState"), engineState);
    }

    /**
     * Load previously saved engine state and game objects from .gamesave directory. Check if a
     * previous save exists.
     * @param drawMsg draw message if there is no previous gamesave
     * @return whether a save is successfully loaded
     */
    boolean loadGame(boolean drawMsg) {
        if (GAMESAVE.list().length == 0) {
            System.out.println("There is no saved game");
            if (drawMsg) {
                clearCanvasAndDrawText(WORLD_WIDTH / 2.0, WORLD_HEIGHT / 2.0 - 1.5 * 4
                        , "There is no saved game");
            }
            return false;
        }
        loadEngineState(join(GAMESAVE, "engineState"));
        GameMechanism.loadGameObjects(join(GAMESAVE, "gameObjects"), this);
        return true;
    }

    /**
     * Load engine state from file.
     * @param file file to load from
     */
    void loadEngineState(File file) {
        HashMap<String, Serializable> engineState = readObject(file, HashMap.class);
        random = (Random) engineState.get("random");
        tiles = (TETile[][]) engineState.get("tiles");
        turnCount = (int) engineState.get("turnCount");
    }
}
