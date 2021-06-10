package gitlet;

import java.io.File;
import java.util.Map;
import java.util.TreeMap;

import static gitlet.Utils.*;

// TODO: any imports you need here

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author Jeffrey Fung
 */
public class Repository {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    /** The staging area directory */
    static final File STAGE = join(GITLET_DIR, "stage");
    /** The blobs directory */
    static final File BLOBS = join(GITLET_DIR, "blobs");
    /** Map from branch name to commit head hash */
    static Map<String, String> headMap = new TreeMap<>();
    /** Name of current branch */
    static String currentBranch = "master";
    /** Map from blob hash to file name */
    static Map<String, String> blobToFileNameMap = new TreeMap<>();
    /* TODO: fill in the rest of this class. */

    /** Call setupPersistence to create folders holding .gitlet, stage, commits, blobs.
     * Make an initial commit by calling makeInitCommit.
     * Print error if .gitlet already exists. */
    static void initRepo() {
        setupPersistence();
        Commit.makeInitCommit();
    }

    private static void setupPersistence(){
        GITLET_DIR.mkdir();
        STAGE.mkdir();
        BLOBS.mkdir();
        Commit.COMMITS.mkdir();
    }

    static void add(String file){

    }
}
