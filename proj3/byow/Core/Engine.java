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

public class Engine {
    public static final int WORLD_WIDTH = 75;
    public static final int WORLD_HEIGHT = 30;
    static final int WORLD_XOFFSET = 0;
    static final int WORLD_YOFFSET = 2;
    static final TETile patternWall = Tileset.WALL;
    static final TETile patternRoomFloor = Tileset.FLOOR;
    static final TETile patternHallwayFloor = Tileset.FLOOR;
    static final TETile patternPlayerAvatar = Tileset.AVATAR;
    static final TETile patternExit = Tileset.LOCKED_DOOR;
    static final File CWD = new File(System.getProperty("user.dir"));
    static final File GAMESAVE = join(CWD, ".gamesave");
    Random random;
    TETile[][] tiles;
    TERenderer ter = new TERenderer();
    int turnCount = 0;
    int gameOver = 0;
    String lastTileDescription = "";

    /** Initialize the world with empty tiles. */
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
    // TODO: cater for other inputs e.g. L, Q, S etc
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
        StdDraw.setPenColor(StdDraw.GRAY);
        StdDraw.filledRectangle(WORLD_WIDTH / 2.0, 0.75, WORLD_WIDTH / 2.0, 0.75);
        StdDraw.setPenColor(StdDraw.WHITE);
        drawTextL(0.5, 0.75, String.format("Health: %d", health));
        drawText(WORLD_WIDTH / 2.0, 0.75, tileDescription);
        drawTextR(WORLD_WIDTH - 0.25, 0.75, String.format("Turn: %d", turn));
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

    void runInteractiveEngine(int seed) {
        runEngine(seed);
        runInteractiveGameplay();
    }

    TETile[][] runStaticEngine(int seed, InputSource inputSource) {
        runEngine(seed);
        return runStaticGamePlay(inputSource);
    }

    void runEngine(int seed) {
        this.random = new Random(seed);
        ArrayList<Room> rooms = Room.buildRooms(this);
        Room.connectRooms(this, rooms);
        GameMechanism.initializeGameplay(this, rooms);
    }

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

    static void setUpPersistence() {
        GAMESAVE.mkdir();
    }

    void saveGame() {
        saveEngineState();
        GameMechanism.saveGameObjects();
    }

    void saveEngineState() {
        HashMap<String, Serializable> engineState = new HashMap<>();
        engineState.put("random", random);
        engineState.put("tiles", tiles);
        engineState.put("turnCount", turnCount);
        writeObject(join(GAMESAVE, "engineState"), engineState);
    }

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

    void loadEngineState(File file) {
        HashMap<String, Serializable> engineState = readObject(file, HashMap.class);
        random = (Random) engineState.get("random");
        tiles = (TETile[][]) engineState.get("tiles");
        turnCount = (int) engineState.get("turnCount");
    }
}
