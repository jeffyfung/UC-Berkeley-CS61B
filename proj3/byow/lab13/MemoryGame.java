package byow.lab13;

import edu.princeton.cs.introcs.StdDraw;

import java.awt.*;
import java.util.Random;

public class MemoryGame {
    /** The width of the window of this game. */
    private int width;
    /** The height of the window of this game. */
    private int height;
    /** The current round the user is on. */
    private int round;
    /** The Random object used to randomly generate Strings. */
    private Random rand;
    /** Whether or not the game is over. */
    private boolean gameOver;
    /** The characters we generate random Strings from. */
    private static final char[] CHARACTERS = "abcdefghijklmnopqrstuvwxyz".toCharArray();
    /** Encouraging phrases. Used in the last section of the spec, 'Helpful UI'. */
    private static final String[] ENCOURAGEMENT = {"You can do this!", "I believe in you!",
                                                   "You got this!", "You're a star!", "Go Bears!",
                                                   "Too easy for you!", "Wow, so impressive!"};

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Please enter a seed");
            return;
        }

        long seed = Long.parseLong(args[0]);
        MemoryGame game = new MemoryGame(40, 40, seed);
        game.startGame();
    }

    public MemoryGame(int width, int height, long seed) {
        /* Sets up StdDraw so that it has a width by height grid of 16 by 16 squares as its canvas
         * Also sets up the scale so the top left is (0,0) and the bottom right is (width, height)
         */
        this.width = width;
        this.height = height;
        this.gameOver = false;
        StdDraw.setCanvasSize(this.width * 16, this.height * 16);
        Font font = new Font("Monaco", Font.BOLD, 30);
        StdDraw.setFont(font);
        StdDraw.setXscale(0, this.width);
        StdDraw.setYscale(0, this.height);
        StdDraw.clear(Color.BLACK);
        StdDraw.enableDoubleBuffering();

        this.rand = new Random(seed);
    }

    public String generateRandomString(int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n ; i += 1) {
            int idx = rand.nextInt(CHARACTERS.length);
            sb.append(CHARACTERS[idx]);
        }
        return sb.toString();
    }

    public void drawFrame(String s, int pauseTime, String action) {
        StdDraw.clear(Color.BLACK);
        Font font = new Font("Helvetica", Font.BOLD, 30);
        Font topFont = new Font("Helvetica", Font.BOLD, 20);
        StdDraw.setFont(font);
        StdDraw.setPenColor(StdDraw.WHITE);
        StdDraw.text(this.width / 2.0 , this.height / 2.0, s);
        // draw top row
        StdDraw.setFont(topFont);
        StdDraw.textLeft(0.5, this.height - 1.2, "Round: " + this.round);
        String actionWord = action.equals("watch")? "Watch!" : "Type!";
        StdDraw.text(this.width / 2.0, this.height - 1.2, actionWord);
        String encouragingWord = ENCOURAGEMENT[rand.nextInt(ENCOURAGEMENT.length)];
        StdDraw.textRight(this.width - 0.5, this.height - 1.2, encouragingWord);
        // draw horizontal line
        StdDraw.line(0, this.height - 2, this.width, this.height - 2);
        StdDraw.show();
        StdDraw.pause(pauseTime);
    }

    public void drawFrame(String s) {
        StdDraw.clear(Color.BLACK);
        Font font = new Font("Helvetica", Font.BOLD, 30);
        StdDraw.setFont(font);
        StdDraw.setPenColor(StdDraw.WHITE);
        StdDraw.text(this.width / 2.0 , this.height / 2.0, s);
        StdDraw.show();
    }

    public void flashSequence(String letters) {
        for (int i = 0; i < letters.length(); i += 1) {
            drawFrame(Character.toString(letters.charAt(i)), 1000, "watch");
            drawFrame("", 1000, "watch");
        }
    }

    public String solicitNCharsInput(int n) {
        drawFrame("", 0, "type");
        StringBuilder sb = new StringBuilder();
        while (sb.length() != n) {
            if (StdDraw.hasNextKeyTyped()) {
                char key = StdDraw.nextKeyTyped();
                sb.append(key);
                drawFrame(sb.toString(), 200, "type");
            }
        }
        return sb.toString();
    }

    public void startGame() {
        this.round = 1;
        while (true) {
            drawFrame("Round: " + this.round, 1000, "watch");
            String stringGenerated = generateRandomString(this.round);
            flashSequence(stringGenerated);
            if (!solicitNCharsInput(this.round).equals(stringGenerated)) {
                this.gameOver = true;
                drawFrame("Game Over! You made it to round: " + this.round);
                break;
            }
            this.round += 1;
        }
    }

}
