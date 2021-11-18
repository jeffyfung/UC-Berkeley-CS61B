package byow.Core;

import byow.Input.InputSource;
import byow.Input.StringInputDevice;
import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import edu.princeton.cs.introcs.StdDraw;

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

    static final TETile patternWall = Tileset.TREE;
    static final TETile patternFloor = Tileset.SOIL;
    static TETile patternPlayerAvatar = Tileset.AVATAR_LEFT;
    static final TETile patternExit = Tileset.LOCKED_DOOR;
    static final TETile patternTorch = Tileset.TORCH;
    static final TETile patternBread = Tileset.BREAD;
    static final TETile patternPortal = Tileset.PORTAL;

    /** Current working directory */
    static final File CWD = new File(System.getProperty("user.dir"));
    /** Directory for saving and loading game. */
    static final File GAMESAVE = join(CWD, "byow", ".gamesave");

    /** RNG */
    Random random;
    /** 2D array of tiles representing game state. */
    TETile[][] tiles;
    /** Renderer for tiles. */
    TERenderer ter = new TERenderer();
    /** Object that controls operations and interactions of game objects */
    GameMechanics gameMech;
    /** Tracks game progress. Do not reset when loading a game. */
    int level;
    /** Description of tile at cursor. */
    String tileDescriptionAtCursor = "";

    /** Constructor for Engine objects. Initialize the game state with empty tiles. */
    public Engine() {
        this.level = 1;
        this.tiles = setTilesToBackground(new TETile[WORLD_WIDTH][WORLD_HEIGHT]);
    }

    /**
     * Run the game engine. This method should handle all inputs, including inputs from the main
     * menu. Uses "wasd" keys to move player. Press ":q" to save game and quit.
     */
    public void interactWithKeyboard() {
        setUpPersistence();
        DrawingUtils.drawSetting();
        DrawingUtils.drawMenu();
        while (true) {
            char inputChar = solicitCharOrMouseInputForMenu();
            switch (inputChar) {
                case 'n' -> {
                    int seed = solicitSeed();
                    String playerName = solicitPlayerName();
                    runInteractiveEngine(seed, playerName, GameMechanics.INIT_PLAYER_HEALTH);
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
     * Initializes engine and gameplay setting. Then run interactive gameplay. Called when user
     * calls interactWithKeyboard().
     * @param seed seed for RNG
     * @param playerName name of player
     * @param playerHealth health of player
     */
    void runInteractiveEngine(int seed, String playerName, int playerHealth) {
        runEngine(seed, playerName, playerHealth);
        runInteractiveGameplay();
    }

    /**
     * Pseudo-randomly generates rooms and hallways and initialize game objects.
     * @param seed seed for RNG
     * @param playerName name of player
     * @param playerHealth health of player
     */
    void runEngine(int seed, String playerName, int playerHealth) {
        this.random = new Random(seed);
        setTilesToBackground(tiles);
        ArrayList<Room> rooms = Room.buildRooms(this);
        Room.connectRooms(this, rooms);
        gameMech = new GameMechanics(this, rooms, playerName, playerHealth);
    }

    /**
     * Change the game state according to keyboard input from user in a turn-based way, and
     * draw the game state and HUD accordingly. "wasd" moves player, ":q" saves game and quit.
     * Display score and leaderboard, and prompt for user input if game over.
     */
    void runInteractiveGameplay() {
        ter.initialize(WORLD_WIDTH + WORLD_XOFFSET, WORLD_HEIGHT + WORLD_YOFFSET
                , WORLD_XOFFSET, WORLD_YOFFSET);
        String[] input = new String[] {"`", tileDescriptionAtCursor};
        int outcome = 0;
        while (true) {
            DrawingUtils.drawGameState(ter, gameMech.fieldOfView(tiles));
            DrawingUtils.drawHud(gameMech.player.health, input[1], Integer.toString(level));
            System.out.println(this);
            input = solicitCharInputAndCursorLocation();
            switch (input[0]) {
                case "w" -> outcome = gameMech.moveGameObject(gameMech.player, 0, 1);
                case "s" -> outcome = gameMech.moveGameObject(gameMech.player, 0, -1);
                case "a" -> outcome = gameMech.moveGameObject(gameMech.player, -1, 0);
                case "d" -> outcome = gameMech.moveGameObject(gameMech.player, 1, 0);
                case " " -> outcome = gameMech.idle();
                case "h" -> outcome = gameMech.teleport();
                case "t" -> gameMech.lightSwitch();
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
                    String advanceMsg = String.format("Advance Level -> Level %d !", level);
                    System.out.println(advanceMsg);
                    runInteractiveEngine(random.nextInt(), gameMech.player.name, gameMech.player.health);
                }
                case -1 -> {
                    System.out.println("Game Over!");
                    Leaderboard lb = updateLeaderboard(gameMech.player.name, level);
                    DrawingUtils.drawEndDisplay(level, lb);
                    restartGame();
                }
            }
        }
    }

    /**
     * Load, update and save the leaderboard. Only the top ENTRIES_TO_KEEP entries are kept on
     * the leaderboard. The entry with the highest level attained will be displayed at the top.
     * @param playerName name of the current player
     * @param level level that the current player attain
     * @return updated leaderboard
     */
    Leaderboard updateLeaderboard(String playerName, int level) {
        Leaderboard lb = new Leaderboard();
        lb.load(join(GAMESAVE, "lbEntries"));
        lb.update(playerName, level);
        lb.save();
        return lb;
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

    public String toString() {
        return TETile.toString(tiles);
    }

    /* Methods for debugging map generation */

    /**
     * Run the static game engine by parsing user's input string. The engine exhibits the same
     * behavior as using interactWithKeyboard(), with the following exceptions:
     *      - no interactive display is rendered
     *      - the engine neither asks for nor takes in player's name
     *      - the engine doesnt ask if the player wants to restart at the end of a game
     *
     * The game state in the form of a tile array is returned if the game is not over after parsing
     * all of user's input.
     *
     * @param input the input string to prompt new game, load game or quit program.
     * @return tile array representing game state
     */
    public TETile[][] interactWithInputString(String input) {
        setUpPersistence();
        InputSource inputSource = new StringInputDevice(input.toLowerCase());
        switch (collectMenuOption(inputSource)) {
            case 'n' -> {
                int seed = collectSeedFromInputString(inputSource);
                return runStaticEngine(seed, inputSource, "placeholder"
                        , GameMechanics.INIT_PLAYER_HEALTH);
            }
            case 'l' -> {
                boolean loadStatus = loadGame(false);
                if (loadStatus) {
                    return runStaticGamePlay(inputSource);
                }
                return null;
            }
            default -> {
                return null;
            }
        }
    }

    /**
     * Initializes engine and gameplay setting. Then changes game state according to input string
     * sequence from user. Return the final game state. Called when user calls interactWithString().
     * @param seed seed for RNG
     * @param inputSource parses input string from user
     * @param playerName name of player
     * @param playerHealth health of player
     * @return array representing tiles in current game state
     */
    TETile[][] runStaticEngine(int seed, InputSource inputSource, String playerName,
                               int playerHealth) {
        runEngine(seed, playerName, playerHealth);
        return runStaticGamePlay(inputSource);
    }

    /**
     * Run gameplay by parsing user's input string. Similar to runInteractiveGameplay() but lacks a
     * few features (see interactWithInputString()).
     * @param inputSource parses input string from user
     * @return array representing game state
     */
    TETile[][] runStaticGamePlay(InputSource inputSource) {
        int outcome = 0;
        while (inputSource.possibleNextInput()) {
            char c = inputSource.getNextKey();
            switch (c) {
                case 'w' -> outcome = gameMech.moveGameObject(gameMech.player, 0, 1);
                case 's' -> outcome = gameMech.moveGameObject(gameMech.player, 0, -1);
                case 'a' -> outcome = gameMech.moveGameObject(gameMech.player, -1, 0);
                case 'd' -> outcome = gameMech.moveGameObject(gameMech.player, 1, 0);
                case ' ' -> outcome = gameMech.idle();
                case 'h' -> outcome = gameMech.teleport();
                case ':' -> {
                    if (inputSource.getNextKey() == 'q') {
                        saveGame();
                        System.out.println(this);
                        System.exit(0);
                    }
                }
            }
            switch (outcome) {
                case 1 -> {
                    level += 1;
                    String advanceMsg = String.format("Advance Level -> Level %d !", level);
                    System.out.println(advanceMsg);
                    runStaticEngine(random.nextInt(), inputSource, "placeholder"
                            , gameMech.player.health);
                }
                case -1 -> {
                    System.out.println("Game Over!");
                    System.exit(0);
                }
            }
        }
        return tiles;
    }

    /* Methods for soliciting player's keyboard input */

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
        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                char input = Character.toLowerCase(StdDraw.nextKeyTyped());
                System.out.println(input);
                return input;
            }
            if (StdDraw.isMousePressed()) {
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
        if (Double.compare(DrawingUtils.optionNMinX, x) <= 0
                && Double.compare(x, DrawingUtils.optionNMaxX) <= 0
                && Double.compare(DrawingUtils.optionNMinY, y) <= 0
                && Double.compare(y, DrawingUtils.optionNMaxY) <= 0) {
            return 'n';
        } else if (Double.compare(DrawingUtils.optionLMinX, x) <= 0
                && Double.compare(x, DrawingUtils.optionLMaxX) <= 0
                && Double.compare(DrawingUtils.optionLMinY, y) <= 0
                && Double.compare(y, DrawingUtils.optionLMaxY) <= 0) {
            return 'l';
        } else if (Double.compare(DrawingUtils.optionQMinX, x) <= 0
                && Double.compare(x, DrawingUtils.optionQMaxX) <= 0
                && Double.compare(DrawingUtils.optionQMinY, y) <= 0
                && Double.compare(y, DrawingUtils.optionQMaxY) <= 0) {
            return 'q';
        } else {
            return ' ';
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
        DrawingUtils.drawText(WORLD_WIDTH / 2.0, WORLD_HEIGHT / 2.0 - 10
                , "Enter Seed Then Press s");
        while (true) {
            input = solicitCharInput();
            if (input == 's') {
                try {
                    return Integer.parseUnsignedInt(sb.toString());
                } catch (NumberFormatException e) {
                    DrawingUtils.drawText(WORLD_WIDTH / 2.0, WORLD_HEIGHT / 2.0 - 13
                            , "Accept Positive Integer Only! Try Again!");
                    StdDraw.show();
                    sb = new StringBuilder();
                }
            } else {
                sb.append(input);
                DrawingUtils.drawMenu();
                DrawingUtils.drawText(WORLD_WIDTH / 2.0, WORLD_HEIGHT / 2.0 - 11.5
                        , sb.toString());
                DrawingUtils.drawText(WORLD_WIDTH / 2.0, WORLD_HEIGHT / 2.0 - 10
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
        DrawingUtils.drawMenu();
        DrawingUtils.drawText(WORLD_WIDTH / 2.0, WORLD_HEIGHT / 2.0 - 10
                , "Enter Your Name Then Press /:");
        while (true) {
            input = solicitCharInput();
            if (input == '/') {
                return sb.toString();
            } else {
                sb.append(input);
                DrawingUtils.drawMenu();
                DrawingUtils.drawText(WORLD_WIDTH / 2.0, WORLD_HEIGHT / 2.0 - 11.5
                        , sb.toString());
                DrawingUtils.drawText(WORLD_WIDTH / 2.0, WORLD_HEIGHT / 2.0 - 10
                        , "Enter Your Name Then Press /:");
                StdDraw.show();
            }
        }
    }

    /**
     * Gets each keyboard input from user and description of the tile that the mouse cursor is
     * currently over. The function only returns when there is any unparsed input or a change in
     * tile description.
     * @return input character and description of the mouse-over tile
     */
    private String[] solicitCharInputAndCursorLocation() {
        TETile[][] fovTiles = null;
        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                char input = Character.toLowerCase(StdDraw.nextKeyTyped());
                System.out.println(input);
                return new String[]{Character.toString(input), tileDescriptionAtCursor};
            }
            int cursorX = (int) StdDraw.mouseX() - WORLD_XOFFSET;
            int cursorY = (int) StdDraw.mouseY() - WORLD_YOFFSET;
            if (fovTiles == null) {
                fovTiles = gameMech.fieldOfView(tiles);
            }
            String tileDescription = getTilePattern(fovTiles, cursorX, cursorY).description();
            if (tileDescription.equals("Player")) {
                tileDescription = gameMech.player.name;
            }
            if (!tileDescription.equals(tileDescriptionAtCursor)) {
                tileDescriptionAtCursor = tileDescription;
                return new String[]{"`", tileDescriptionAtCursor};
            }
        }
    }

    /* Methods for collecting player's input by parsing input string */

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

    /* Methods for interaction with tiles on map */

    /**
     * Set all tiles to empty.
     * @param tArray tile array to alter
     * @return array of empty tiles
     */
    static TETile[][] setTilesToBackground(TETile[][] tArray) {
        for (int x = 0; x < tArray.length; x += 1) {
            for (int y = 0; y < tArray[0].length; y += 1) {
                tArray[x][y] = Tileset.NOTHING;
            }
        }
        return tArray;
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
        if (pos.getX() >= 0 && pos.getX() < WORLD_WIDTH &&
                pos.getY() >= 0 && pos.getY() < WORLD_HEIGHT) {
            return tiles[pos.getX()][pos.getY()];
        }
        return Tileset.NOTHING;
    }

    /** Get TETile at specific position. */
    public TETile getTilePattern(int x, int y) {
        if (x >= 0 && x < WORLD_WIDTH && y >= 0 && y < WORLD_HEIGHT) {
            return tiles[x][y];
        }
        return Tileset.NOTHING;
    }

    /** Get TETile at specific position. */
    public TETile getTilePattern(TETile[][] tArray, int x, int y) {
        if (x >= 0 && x < tArray.length && y >= 0 && y < tArray[0].length) {
            return tArray[x][y];
        }
        return Tileset.NOTHING;
    }

    /* Methods for persistence */

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
        if (!join(GAMESAVE, "gameState").exists()) {
            System.out.println("There is no saved game");
            if (drawMsg) {
                DrawingUtils.clearCanvasAndDrawText(WORLD_WIDTH / 2.0
                        , WORLD_HEIGHT * 0.5, "There is no saved game");
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
