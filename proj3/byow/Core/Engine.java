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
    /** Coordinates of center, half-width and half-height of the 'New Game' option box on menu. */
    static final double[] optionN = new double[]{WORLD_WIDTH / 2.0, WORLD_HEIGHT / 2.0 + 2, 8, 1.5};
    /** Coordinates of center, half-width and half-height of the 'Load Game' option box on menu. */
    static final double[] optionL = new double[]{WORLD_WIDTH / 2.0, WORLD_HEIGHT / 2.0 - 2, 8, 1.5};
    /** Coordinates of center, half-width and half-height of the 'Quit' option box on menu. */
    static final double[] optionQ = new double[]{WORLD_WIDTH / 2.0, WORLD_HEIGHT / 2.0 - 6, 8, 1.5};
    static final TETile patternWall = Tileset.TREE;
    static final TETile patternFloor = Tileset.SOIL;
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
    /** Object that controls operations and interactions of game objects */
    GameMechanics gameMech;
    /** Tracks game progress. Do not reset when loading a game. */
    int level = 1;
    /** Description of tile at cursor. */
    String tileDescriptionAtCursor = "";

    /** Constructor for Engine objects. Initialize the game state with empty tiles. */
    public Engine() {
        this.tiles = new TETile[WORLD_WIDTH][WORLD_HEIGHT];
        setTilesToBackground();
    }

    /**
     * Set all tiles to empty.
     * @return array of empty tiles
     */
    private TETile[][] setTilesToBackground() {
        for (int x = 0; x < WORLD_WIDTH; x += 1) {
            for (int y = 0; y < WORLD_HEIGHT; y += 1) {
                tiles[x][y] = Tileset.NOTHING;
            }
        }
        return tiles;
    }

    /**
     * Run the game engine. This method should handle all inputs, including inputs from the main
     * menu. Uses "wasd" keys to move player. Press ":q" to save game and quit.
     */
    public void interactWithKeyboard() {
        setUpPersistence();
        drawSetting();
        drawMenu();
        while (true) {
            char inputChar = solicitCharOrMouseInputForMenu();
            switch (inputChar) {
                case 'n' -> {
                    int seed = solicitSeed();
                    String playerName = solicitPlayerName();
                    runInteractiveEngine(seed, playerName);
                }
                case 'l' -> {
                    boolean loadStatus = loadGame(true);
                    if (loadStatus) {
                        runInteractiveGameplay();
                    }
                }
                case 'q' -> System.exit(0);
            }
        }
    }

    /**
     * Run the game engine. The engine should behave exactly as if the user typed these characters
     * into the engine using interactWithKeyboard(), except that the only thing the user is
     * prompted to input, apart from the gameplay, is the seed for RNG.
     * If the first valid input character is "l", the last saved game state, if any, will be
     * loaded. For instance, the call interactWithInputString("n123sss:q") followed by the call
     * interactWithInputString("lww") should yield the exact same world state as
     * interactWithInputString("n123sssww").
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
        Font titleFont = new Font("Serif", Font.BOLD, 60);
        StdDraw.setPenColor(StdDraw.BOOK_RED);
        StdDraw.setFont(titleFont);
        StdDraw.clear(Color.BLACK);
        StdDraw.text(WORLD_WIDTH / 2.0, WORLD_HEIGHT - 5, "Simple Maze Game");

        Font optionsFont = new Font("Serif", Font.BOLD, 25);
        StdDraw.setFont(optionsFont);
        StdDraw.setPenRadius(0.01);

        StdDraw.rectangle(optionN[0], optionN[1], optionN[2], optionN[3]);
        StdDraw.text(optionN[0], optionN[1], "New Game (N)");
        StdDraw.rectangle(optionL[0], optionL[1], optionL[2], optionL[3]);
        StdDraw.text(optionL[0], optionL[1], "Load Game (L)");
        StdDraw.rectangle(optionQ[0], optionQ[1], optionQ[2], optionQ[3]);
        StdDraw.text(optionQ[0], optionQ[1], "Quit (Q)");

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
        drawText(x, y, str);
    }

    /**
     * Draw HUD at the bottom of the window during gameplay. The HUD displays information about
     * player's health, number of turn passed, description of a tile and current date.
     * @param health health of the player
     * @param tileDescription description of a tile
     */
    void drawHud(int health, String tileDescription, String level) {
        StdDraw.setPenColor(StdDraw.GRAY);
        StdDraw.filledRectangle(WORLD_WIDTH / 2.0, 0.75, WORLD_WIDTH / 2.0, 0.75);
        StdDraw.setPenColor(StdDraw.WHITE);
        drawTextL(0.5, 0.75, String.format("Health: %d", health));
        drawText(WORLD_WIDTH * 1 / 3.0, 0.75, tileDescription);
        drawText(WORLD_WIDTH * 2 / 3.0, 0.75, "Level: " + level);
        drawTextR(WORLD_WIDTH - 0.25, 0.75, java.time.LocalDate.now().toString());
    }

    /**
     * Draws a player's score (i.e. number of rounds they survive) and a leaderboard. Also
     * draws a question asking if they want to restart the game. The function is called at the end
     * of a game.
     */
    void drawEndDisplay() {
        Font font = new Font("Serif", Font.BOLD, 40);
        StdDraw.setPenColor(StdDraw.BOOK_RED);
        StdDraw.setFont(font);
        clearCanvasAndDrawText(WORLD_WIDTH / 2.0, WORLD_HEIGHT * 4.5 / 5.0,
                String.format("Game Over! You have survived %d round(s)!", Math.max(0, level - 1)));
        // TODO: update leaderboard
        String leaderBoard = "leader board placeholder";
        drawText(WORLD_WIDTH / 2.0, WORLD_HEIGHT * 3.0 / 5.0, leaderBoard);

        String tryAgain = "Would You Like To Try Again? Y/N";
        drawText(WORLD_WIDTH / 2.0, WORLD_HEIGHT * 1.0 / 5.0, tryAgain);
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
     * Gets each keyboard input as character. Converts to lower case alphabets if applicable. If
     * mouse is clicked, return the user input for menu options, or spacebar.
     * @return user input
     */
    private char solicitCharOrMouseInputForMenu() {
        // TODO: mouse press bug
        while (true) {
            System.out.println("triggered");
            if (StdDraw.hasNextKeyTyped()) {
                char input = Character.toLowerCase(StdDraw.nextKeyTyped());
                System.out.println(input);
                return input;
            }
            if (StdDraw.isMousePressed()) {
                System.out.println(StdDraw.isMousePressed());
                System.out.println(StdDraw.isMousePressed());
                System.out.println(StdDraw.isMousePressed());
                System.out.println("mouse pressed");
                return solicitInputFromMouseForMenu();
            }
        }
    }

    /**
     * Gets user input according to the menu options. Return spacebar if the mouse is located
     * outside of boxes represented by all options.
     * @return user input represented by the mouse location
     */
    private char solicitInputFromMouseForMenu() {
        double x = StdDraw.mouseX();
        double y = StdDraw.mouseY();
        if (Double.compare(optionN[0] - optionN[2], x) <= 0
                && Double.compare(x, optionN[0] + optionN[2]) <= 0
                && Double.compare(optionN[1] - optionN[3], y) <= 0
                && Double.compare(y, optionN[1] + optionN[3]) <= 0) {
            return 'n';
        } else if (Double.compare(optionL[0] - optionL[2], x) <= 0
                && Double.compare(x, optionL[0] + optionL[2]) <= 0
                && Double.compare(optionL[1] - optionL[3], y) <= 0
                && Double.compare(y, optionL[1] + optionL[3]) <= 0) {
            return 'l';
        } else if (Double.compare(optionQ[0] - optionQ[2], x) <= 0
                && Double.compare(x, optionQ[0] + optionQ[2]) <= 0
                && Double.compare(optionQ[1] - optionQ[3], y) <= 0
                && Double.compare(y, optionQ[1] + optionQ[3]) <= 0) {
            return 'q';
        } else {
            return ' ';
        }
    }

    /**
     * Gets each keyboard input from user and description of the tile that the mouse cursor is
     * currently over. The function only returns when there is any unparsed input or a change in
     * tile description.
     * @return input character and description of the mouse-over tile
     */
    private String[] solicitCharInputAndCursorLocation() {
        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                char input = Character.toLowerCase(StdDraw.nextKeyTyped());
                System.out.println(input);
                return new String[]{Character.toString(input), tileDescriptionAtCursor};
            }
            int cursorX = (int) StdDraw.mouseX() - WORLD_XOFFSET;
            int cursorY = (int) StdDraw.mouseY() - WORLD_YOFFSET;
            String tileDescription;
            if (cursorX < 0 || cursorY < 0) {
                tileDescription = Tileset.NOTHING.description();
            } else {
                tileDescription = getTilePattern(cursorX, cursorY).description();
                if (tileDescription.equals("Player")) {
                    tileDescription = gameMech.player.name;
                }
            }
            if (!tileDescription.equals(tileDescriptionAtCursor)) {
                tileDescriptionAtCursor = tileDescription;
                return new String[]{"`", tileDescriptionAtCursor};
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
        drawText(WORLD_WIDTH / 2.0, WORLD_HEIGHT / 2.0 - 10
                , "Enter Seed Then Press s");
        while (true) {
            input = solicitCharInput();
            if (input == 's') {
                try {
                    return Integer.parseUnsignedInt(sb.toString());
                } catch (NumberFormatException e) {
                    drawText(WORLD_WIDTH / 2.0, WORLD_HEIGHT / 2.0 - 13
                            , "Accept Positive Integer Only! Try Again!");
                    StdDraw.show();
                    sb = new StringBuilder();
                }
            } else {
                sb.append(input);
                drawMenu();
                drawText(WORLD_WIDTH / 2.0, WORLD_HEIGHT / 2.0 - 11.5, sb.toString());
                drawText(WORLD_WIDTH / 2.0, WORLD_HEIGHT / 2.0 - 10
                        , "Enter Seed Then Press s");
                StdDraw.show();
            }
        }
    }

    /**
     * Get player name from user. Name must be followed by '/'.
     * @return playerName
     */
    private String solicitPlayerName() {
        StringBuilder sb = new StringBuilder();
        char input;
        drawMenu();
        drawText(WORLD_WIDTH / 2.0, WORLD_HEIGHT / 2.0 - 10, "Enter Your Name Then Press /:");
        while (true) {
            input = solicitCharInput();
            if (input == '/') {
                return sb.toString();
            } else {
                sb.append(input);
                drawMenu();
                drawText(WORLD_WIDTH / 2.0, WORLD_HEIGHT / 2.0 - 11.5, sb.toString());
                drawText(WORLD_WIDTH / 2.0, WORLD_HEIGHT / 2.0 - 10
                        , "Enter Your Name Then Press /:");
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
     * @param playerName name of player
     */
    void runInteractiveEngine(int seed, String playerName) {
        runEngine(seed, playerName);
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
     * Pseudo-randomly generates rooms and hallways and initialize game objects.
     * @param seed seed for RNG
     * @param playerName name of player
     */
    void runEngine(int seed, String playerName) {
        this.random = new Random(seed);
        setTilesToBackground();
        ArrayList<Room> rooms = Room.buildRooms(this);
        Room.connectRooms(this, rooms);
        gameMech = new GameMechanics(this, rooms, playerName);
    }

    /**
     * Pseudo-randomly generates rooms and hallways and initialize game objects.
     * @param seed seed for RNG
     */
    void runEngine(int seed) {
        this.random = new Random(seed);
        ArrayList<Room> rooms = Room.buildRooms(this);
        Room.connectRooms(this, rooms);
        gameMech = new GameMechanics(this, rooms, "placeholder");
    }

    /**
     * Change the game state according to keyboard input from user in a turn-based way, and
     * draw the game state and HUD accordingly. "wasd" moves player, ":q" saves game and quit.
     * Draw a message if the player successfully finishes the game.
     */
    void runInteractiveGameplay() {
        ter.initialize(WORLD_WIDTH + WORLD_XOFFSET, WORLD_HEIGHT + WORLD_YOFFSET
                , WORLD_XOFFSET, WORLD_YOFFSET);
        String[] input = new String[] {"`", tileDescriptionAtCursor};
        int outcome = 0;
        while (true) {
            ter.renderFrame(tiles);
            drawHud(gameMech.player.health, input[1], Integer.toString(level));
            input = solicitCharInputAndCursorLocation();
            switch (input[0]) {
                case "w" -> outcome = gameMech.moveGameObject(gameMech.player, 0, 1);
                case "s" -> outcome = gameMech.moveGameObject(gameMech.player, 0, -1);
                case "a" -> outcome = gameMech.moveGameObject(gameMech.player, -1, 0);
                case "d" -> outcome = gameMech.moveGameObject(gameMech.player, 1, 0);
                case " " -> outcome = gameMech.moveGameObject(gameMech.player, 0, 0);
                case ":" -> {
                    if (solicitCharInput() == 'q') {
                        saveGame();
                        System.exit(0);
                    }
                }
            }
            switch (outcome) {
                case 1 -> {
                    level += 1;
                    // TODO : keep health from last level; indicate new level in HUD
                    String advanceMsg = String.format("Advance Level -> Level %d !", level);
                    System.out.println(advanceMsg);
                    runInteractiveEngine(random.nextInt(), gameMech.player.name);
                }
                case 3 -> {
                    System.out.println("Game Over!");
                    drawEndDisplay();
                    restartGame();
                }
            }
        }
    }

    /**
     * Ask whether user would like to restart the game. If 'y' (case-insensitive) is received,
     * user will be prompted to the game menu for a fresh new game. If 'n' (case-insensitive) is
     * received, display window will close and the program halts.
     */
    void restartGame() {
        char restart;
        while (true) {
            restart = solicitCharInput();
            if (restart == 'y') {
                Main.main(new String[]{});
                System.exit(0);
            } else if (restart == 'n') {
                System.exit(0);
            }
        }
    }

    /**
     * Similar to runInteractiveGameplay() but takes user-input string.
     * @param inputSource parses input string from user
     * @return array representing game state
     */
    // TODO: to match interactive gameplay
    TETile[][] runStaticGamePlay(InputSource inputSource) {
        int outcome = 0;
        while (inputSource.possibleNextInput()) {
            char c = inputSource.getNextKey();
            switch (c) {
                case 'w' -> outcome = gameMech.moveGameObject(gameMech.player, 0, 1);
                case 's' -> outcome = gameMech.moveGameObject(gameMech.player, 0, -1);
                case 'a' -> outcome = gameMech.moveGameObject(gameMech.player, -1, 0);
                case 'd' -> outcome = gameMech.moveGameObject(gameMech.player, 1, 0);
                case ' ' -> outcome = gameMech.moveGameObject(gameMech.player, 0, 0);
                case ':' -> {
                    if (solicitCharInput() == 'q') {
                        saveGame();
                        System.exit(0);
                    }
                }
            }
            if (outcome == 1) {
                level += 1;
                String advanceMsg = String.format("Advance Level -> Level %d !", level);
                System.out.println(advanceMsg);
                runStaticEngine(random.nextInt(), inputSource);
            } else if (outcome == 3) {
                System.out.println("Game Over!");
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
     * Create a directory, if not already exists, to store state of game for saving and loading
     * games.
     */
    static void setUpPersistence() {
        GAMESAVE.mkdir();
    }

    /** Saves state of game to .gamesave directory. */
    void saveGame() {
        HashMap<String, Serializable> gameState = new HashMap<>();
        gameState.put("random", random);
        gameState.put("tiles", tiles);
        gameState.put("level", level);
        gameState.put("gameMech", gameMech);
        writeObject(join(GAMESAVE, "gameState"), gameState);
    }

    /**
     * Load state of game from .gamesave directory. Check if a previous save exists.
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

        File f = join(GAMESAVE, "gameState");
        HashMap<String, Serializable> gameState = readObject(f, HashMap.class);
        random = (Random) gameState.get("random");
        tiles = (TETile[][]) gameState.get("tiles");
        level = (int) gameState.get("level");
        gameMech = (GameMechanics) gameState.get("gameMech");
        gameMech.engine = this;

        return true;
    }
}
