package byow.Core;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import edu.princeton.cs.introcs.StdDraw;

import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

import static byow.Core.Engine.WORLD_HEIGHT;
import static byow.Core.Engine.WORLD_WIDTH;

public class DrawingUtils {
    /** Coordinates of center, half-width and half-height of the 'New Game' option box on menu. */
    static final double[] optionN = new double[]{WORLD_WIDTH / 2.0, WORLD_HEIGHT / 2.0 + 2, 8, 1.5};
    /** X-coordinate of lower left corner of the "New Game" option box on menu. */
    static final double optionNMinX = optionN[0] - optionN[2];
    /** X-coordinate of upper right corner of the "New Game" option box on menu. */
    static final double optionNMaxX = optionN[0] + optionN[2];
    /** Y-coordinate of lower left corner of the "New Game" option box on menu. */
    static final double optionNMinY = optionN[1] - optionN[3];
    /** Y-coordinate of upper right corner of the "New Game" option box on menu. */
    static final double optionNMaxY = optionN[1] + optionN[3];

    /** Coordinates of center, half-width and half-height of the 'Load Game' option box on menu. */
    static final double[] optionL = new double[]{WORLD_WIDTH / 2.0, WORLD_HEIGHT / 2.0 - 2, 8, 1.5};
    static final double optionLMinX = optionL[0] - optionL[2];
    static final double optionLMaxX = optionL[0] + optionL[2];
    static final double optionLMinY = optionL[1] - optionL[3];
    static final double optionLMaxY = optionL[1] + optionL[3];

    /** Coordinates of center, half-width and half-height of the 'Quit' option box on menu. */
    static final double[] optionQ = new double[]{WORLD_WIDTH / 2.0, WORLD_HEIGHT / 2.0 - 6, 8, 1.5};
    static final double optionQMinX = optionQ[0] - optionQ[2];
    static final double optionQMaxX = optionQ[0] + optionQ[2];
    static final double optionQMinY = optionQ[1] - optionQ[3];
    static final double optionQMaxY = optionQ[1] + optionQ[3];

    /**
     * Initialize setting for drawing the menu and gameplay. Used when users enter the game by
     * calling interactWithKeyboard().
     */
    static void drawSetting() {
        StdDraw.setCanvasSize(WORLD_WIDTH * 16, (WORLD_HEIGHT + 3) * 16);
        StdDraw.setXscale(0, WORLD_WIDTH);
        StdDraw.setYscale(0, WORLD_HEIGHT);
        StdDraw.setPenColor(StdDraw.WHITE);
        StdDraw.enableDoubleBuffering();
    }

    /** Draw game menu. */
    static void drawMenu() {
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
    static void drawText(double x, double y, String str) {
        StdDraw.text(x, y, str);
        StdDraw.show();
    }

    /**
     * Helper function for drawing left aligned text.
     * @param x x coordinate of left boundary of the string to be drawn.
     * @param y y coordinate of left boundary of the string to be drawn.
     * @param str string to be drawn
     */
    static void drawTextL(double x, double y, String str) {
        StdDraw.textLeft(x, y, str);
        StdDraw.show();
    }

    /**
     * Helper function for drawing right aligned text.
     * @param x x coordinate of right boundary of the string to be drawn.
     * @param y y coordinate of right boundary of the string to be drawn.
     * @param str string to be drawn
     */
    static void drawTextR(double x, double y, String str) {
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
    static void drawTextWithFont(double x, double y, String str, Font font) {
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
    static void clearCanvasAndDrawText(double x, double y, String str) {
        StdDraw.clear(StdDraw.BLACK);
        drawText(x, y, str);
    }

    static void drawGameState(TERenderer ter, TETile[][] tiles) {
        ter.renderFrame(tiles);
    }

    /**
     * Draw HUD at the bottom of the window during gameplay. The HUD displays information about
     * player's health, number of turn passed, description of a tile and current date.
     * @param health health of the player
     * @param tileDescription description of a tile
     */
    static void drawHud(int health, String tileDescription, String level) {
        StdDraw.setPenColor(StdDraw.GRAY);
        StdDraw.filledRectangle(WORLD_WIDTH / 2.0, 0.75, WORLD_WIDTH / 2.0, 0.75);
        StdDraw.setPenColor(StdDraw.WHITE);
        String td = tileDescription.length() == 0? "" : "Tile: " + tileDescription;
        drawTextL(0.5, 0.75, String.format("Health: %d", health));
        drawText(WORLD_WIDTH / 3.0, 0.75, td);
        drawText(WORLD_WIDTH * 2 / 3.0, 0.75, "Level: " + level);
        drawTextR(WORLD_WIDTH - 0.25, 0.75, LocalDate.now().toString());
    }

    /**
     * Draws the level attained by the current player (i.e. score) and a leaderboard. Also
     * draws a question asking if they want to restart the game. The function is called at the end
     * of a game.
     * @param level progress of player at the end of a game (i.e. score)
     * @param lb leaderboard to be drawn
     */
    static void drawEndDisplay(int level, Leaderboard lb) {
        Font font = new Font("Serif", Font.BOLD, 40);
        StdDraw.setPenColor(StdDraw.BOOK_RED);
        StdDraw.setFont(font);
        clearCanvasAndDrawText(WORLD_WIDTH / 2.0, WORLD_HEIGHT * 0.95,
                String.format("Game Over! Game ends at level %d!", level));

        // draws leaderboard
        drawTextL(WORLD_WIDTH * 0.205, WORLD_HEIGHT * 0.8
                , "-------------------- Leaderboard --------------------");
        drawTextL(WORLD_WIDTH * 0.2, WORLD_HEIGHT * 0.7,
                "| Name             Level                    Time              |");
        for (int i = 0; i < lb.LBEntries.size(); i += 1) {
            Leaderboard.LBEntry entry = lb.LBEntries.get(i);
            drawTextL(WORLD_WIDTH * 0.2, WORLD_HEIGHT * 0.6 - i * 3
                    , "| " + entry.playerName);
            drawText(WORLD_WIDTH * 0.45, WORLD_HEIGHT * 0.6 - i * 3
                    , Integer.toString(entry.level));
            drawTextR(WORLD_WIDTH * 0.85, WORLD_HEIGHT * 0.6 - i * 3
            ,entry.date.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)) + " |");
        }
        drawTextL(WORLD_WIDTH * 0.205, WORLD_HEIGHT * 0.6 - lb.LBEntries.size() * 3,
        "-----------------------------------------------------------");

        String tryAgain = "Would You Like To Try Again? Y/N";
        drawText(WORLD_WIDTH / 2.0, WORLD_HEIGHT * 0.1, tryAgain);
    }
}
