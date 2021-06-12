package gitlet;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import static java.nio.file.StandardCopyOption.*;

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
    static Map<String, String> headMap = new StringTreeMap();
    /** Name of current branch */
    static String currentBranch = "master"; // retrieve from serializable?
//    /** Map from blob hash to file name */
//    static Map<String, String> blobToFileNameMap = new TreeMap<>();
    /* TODO: fill in the rest of this class. */

    /** Call setupPersistence to create folders holding .gitlet, stage, commits, blobs.
     * Make an initial commit by calling makeInitCommit.
     * Print error if .gitlet already exists. */
    static void initRepo() {
        setupPersistence();
        Commit.makeInitCommit();
    }

    private static void setupPersistence(){
        if (plainFilenamesIn(GITLET_DIR) != null) {
            throw Utils.error("A Gitlet version-control system already exists in the current directory.");
        }
        GITLET_DIR.mkdir();
        STAGE.mkdir();
        BLOBS.mkdir();
        Commit.COMMITS.mkdir();
    }

    /** Copy a file from CWD to staging area. Overwrite previous entry of the same name
     *  in the staging area. No action if the blob hash already exists in the staging
     *  area (i.e. content already exists).
     */
    static void add(String fileName){
        if (!plainFilenamesIn(CWD).contains(fileName)) {
            throw Utils.error("File does not exist.");
        }
        String fileHash = sha1(readContents(join(CWD, fileName)));
        String targetBlobHash = getCommitFromHash(getHeadHash()).getBlobMap().get(fileName); // O(log N)
        if (targetBlobHash != null && targetBlobHash.equals(fileHash)) {
            if (plainFilenamesIn(STAGE).contains(fileName)) {
                // if contains same file (by name) in staging area -> remove file from staging area
                join(STAGE, fileName).delete();
            }
        }
        else {
            try {
                Files.copy(join(CWD, fileName).toPath(), join(STAGE, fileName).toPath(), REPLACE_EXISTING);
            }
            catch (IOException e) {
                throw Utils.error("IOException during file copy operation.");
            }
        }
    }

    /** Create a new commit whose parent commit is the HEAD of current branch.
     *  The new commit's blobMap is identical to its parent except for files in STAGE.
     *  Clear all files in STAGE afterwards */
    static void commit(String commitMsg){
        Commit.makeCommit(commitMsg);
        clearStage();
    }

    private static void clearStage(){
        for (String f : plainFilenamesIn(STAGE)){
            if (!join(STAGE, f).delete()) {
                throw Utils.error("File cannot be deleted");
            }
        }
    }

    /** Get hash string of current branch HEAD Check if headMap is empty.
     *  If empty, deserialize headMap file */
    static String getHeadHash() {
        if (headMap.isEmpty()) {
            headMap = readObject(join(GITLET_DIR, "headMap"), StringTreeMap.class);
        }
//        System.out.println("headmap: " + headMap);
        return headMap.get(currentBranch);
    }

    /** Get blobMap of a commit from its hash. Cache the (hash, commit) pair if it has not been done so*/
    static Commit getCommitFromHash(String hash) {
        if (!Commit.commitCache.containsKey(hash)) {
            Commit targetCommit = readObject(join(Commit.COMMITS, hash), Commit.class);
            Commit.commitCache.put(hash, targetCommit);
            return targetCommit;
        }
        return Commit.commitCache.get(hash);
    }

    public static class StringTreeMap extends TreeMap<String, String> {}
}
