package gitlet;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
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
    /** label for files that are staged to be removed */
    static String keyString = "[[del[[";
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
            join(STAGE, fileName).delete();
            // current commit contains the same version of file as the one in CWD
            // delete the file from CWD without any staging
            // return true if successfully deleted; return false if the file is not in STAGE
            // no further action required for both cases
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

    /** 1) If the file is currently staged for addition, unstage it.
     *  2) If the file is tracked in the current commit, stage it for removal and
     *  if the file is in CWD, remove it from CWD*/
    static void remove(String fileName){
        if (plainFilenamesIn(STAGE).contains(fileName)){
            if (!join(STAGE, fileName).delete()){
                throw Utils.error("Error when deleting %s from staging area.", fileName);
            }
        }
        // check if current commit contains fileName
        else if (getCommitFromHash(getHeadHash()).getBlobMap().containsKey(fileName)) {
            File fileStagedForRemoval = join(STAGE, keyString.concat(fileName));
            createFile(fileStagedForRemoval);
            // delete fileName from CWD. return false if fileName does not exist in CWD
            restrictedDelete(fileName);
        }
        else {
            throw Utils.error("No reason to remove the file.");
        }
    }

    /** Display the history of commit starting from current head commit to initial commit,
     *  starting with current commit. For merge commits, ignore second parents.
     *  Print out the commit id, time and commit message of each commit. */
    // update to account for merge commits
    static void log() {
        logHelper(getHeadHash());
    }

    /** Display information of every commit in the gitlet repository in lexicographic
     *  order of their hash strings, formatted as specified in displayCommitInfo. */
    static void globalLog() {
        for (String curCommitHash : plainFilenamesIn(Commit.COMMITS)) {
            Commit curCommit = getCommitFromHash(curCommitHash);
            displayCommitInfo(curCommitHash, curCommit);
        }
    }

    /** Display the hashes of commits that have the input commit message, one per line,
     *  in lexicographical order of the hashes. */
    static void find(String msg) {
        // for each loop over a list of all commits hashes in COMMIT
        // for each hash, restore the commit object and its commitMsg -> display if equals
        boolean msgOutputted = false;
        for (String curCommitHash : plainFilenamesIn(Commit.COMMITS)) {
            if (getCommitFromHash(curCommitHash).getCommitMsg().equals(msg)) {
                System.out.println(curCommitHash);
                msgOutputted = true;
            }
        }
        if (!msgOutputted){
            throw Utils.error("Found no commit with that message.");
        }
    }

    /** Traverse up the commit tree (from current head to initial commit) recursively.
     *  Print out the commit id, time and commit message for each commit traversed. */
    private static void logHelper(String curCommitHash) {
        if (curCommitHash == null) {
            return;
        }
        Commit curCommit = getCommitFromHash(curCommitHash);
        displayCommitInfo(curCommitHash, curCommit);
        logHelper(curCommit.getParentCommitHash());
    }

    /** Display information of a commit. */
    // update to account for merge commits
    private static void displayCommitInfo(String hash, Commit commit){
        System.out.println("===");
        System.out.println("commit ".concat(hash));
        // insert merge numerals for merge commits -> "Merge: "
        System.out.println("Date: ".concat(commit.getCommitDate().toString()));
        System.out.println(commit.getCommitMsg().concat("\n"));
    }

    /** Clear the STAGE directory */
    private static void clearStage(){
        for (String f : plainFilenamesIn(STAGE)){
            if (!join(STAGE, f).delete()) {
                throw Utils.error("File cannot be deleted");
            }
        }
    }

    /** Get hash string of current branch head. Check if headMap is empty.
     *  If empty, deserialize headMap file. */
    static String getHeadHash() {
        if (headMap.isEmpty()) {
            headMap = readObject(join(GITLET_DIR, "headMap"), StringTreeMap.class);
        }
        return headMap.get(currentBranch);
    }

    /** Get a commit object from its hash.
     * Cache the (hash, commit) pair if it has not been done so. */
    static Commit getCommitFromHash(String hash) {
        if (!Commit.commitCache.containsKey(hash)) {
            Commit targetCommit = readObject(join(Commit.COMMITS, hash), Commit.class);
            Commit.commitCache.put(hash, targetCommit); // hashmap -> O(1) for insertion, access
            return targetCommit;
        }
        return Commit.commitCache.get(hash);
    }

    static void createFile(File f){
        try {
            f.createNewFile();
        }
        catch (IOException e){
            throw Utils.error("Encounter IOException when creating new file ((%s))", f);
        }
    }

    public static class StringTreeMap extends TreeMap<String, String> {}
}
