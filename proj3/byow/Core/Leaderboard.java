package byow.Core;

import java.io.File;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static byow.Core.PersistenceUtils.join;
import static byow.Core.PersistenceUtils.writeObject;

/**
 * Allows to save, update and load a leaderboard that records the top ENTRIES_TO_KEEP entries,
 * sorted by levels attained by players in descending order. The entry of the highest level
 * is displayed at the top. Name of player and time of completion are also recorded.
 */
public class Leaderboard implements Serializable {
    /** The number of top entries to keep. */
    static int ENTRIES_TO_KEEP = 4;
    /** Array recording the top entries. */
    ArrayList<LBEntry> LBEntries;

    Leaderboard() {}

    /**
     * Load existing leaderboard.
     * @param file file to load from
     * @return leaderboard loaded
     */
    Leaderboard load(File file) {
        if (file.exists()) {
            LBEntries = PersistenceUtils.readObject(file, ArrayList.class);
        } else {
            LBEntries = new ArrayList<>();
            System.out.println("creating new LBEntries");
        }
        return this;
    }

    /**
     * Update leaderboard. If the level attained by the current player is higher than any of the
     * current top 3 entries, insert the current entry to the leaderboard such that it still
     * records the top 3 entries in descending order (sorted by level). Insert directly to the
     * leaderboard if the leaderboard is empty.
     * @param playerName name of current player
     * @param level current level attained
     */
    void update(String playerName, int level) {
        boolean inserted = false;
        for (int i = 0; i < LBEntries.size(); i += 1) {
            if (level > LBEntries.get(i).level) {
                LBEntry lbEntry = new LBEntry(playerName, level);
                LBEntries.add(i, lbEntry);
                LBEntries.remove(LBEntries.size() - 1);
                inserted = true;
                return;
            }
        }
        if (!inserted && LBEntries.size() < ENTRIES_TO_KEEP) {
            LBEntries.add(new LBEntry(playerName, level));
        }
    }

    /** Save leaderboard to ./.gamesave/lbEntries. */
    void save() {
        writeObject(join(Engine.GAMESAVE, "lbEntries"), LBEntries);
    }

    /**
     * Nested class that records details of each entry including name, level attained and date.
     */
    class LBEntry implements Serializable {
        /** Name of player. */
        String playerName;
        /** Level attained. */
        int level;
        /** Date and time of completion. */
        LocalDateTime date;

        /** Constructor of the nested class. Date and time are recorded as the current time. */
        LBEntry(String playerName, int level) {
            this.playerName = playerName;
            this.level = level;
            this.date = LocalDateTime.now();
        }
    }
}