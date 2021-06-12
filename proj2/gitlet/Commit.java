package gitlet;

// TODO: any imports you need here

import static gitlet.Utils.*;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.time.Instant;
import java.util.Map;
import java.util.TreeMap;
import java.util.Date; // TODO: You'll likely use this in this class

/** Represents a gitlet commit object.
 *  Allows commit object to be hashed and serialized to file named with the hash.
 *  @author Jeffrey Fung
 */
public class Commit implements Serializable {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The commits directory */
    static final File COMMITS = join(Repository.GITLET_DIR, "commits");
    /** The message of this Commit. */
    private String commitMsg;
    /** Commit date */
    private Date commitDate;
    /** Map from file name to blob hash. Indicates which version of content does the commit tracks. */
    Map<String, String> blobMap;
    /** Hash of (first) parent commit. Can be used to trace back to initial commit */
    private String parentCommitHash;
    /** Hash of second parent commit. Null for non-merge commit */
    private String secondParentCommitHash;
//    /** Hash of this commit */
//    private String commitHash;
    /** Map from commit hash to runtime commit object. Not serialized */
    static transient Map<String, Commit> commitCache = new TreeMap<>();
    /** Parent commit object. Not serialized. */
    private transient Commit parentCommit;
    /** Second parent commit object. Not serialized. */
    private transient Commit secondParentCommit;

    public Commit(String commitMsg, Date commitDate, String parentCommitHash, Map<String, String> blobMap) {
        this.commitMsg = commitMsg;
        this.commitDate = commitDate;
        this.parentCommitHash = parentCommitHash;
        this.blobMap = blobMap;
    }

    /** Create a commit object with commitDate = 00:00:00 UTC, Thursday, 1 January 1970,
     * commitMsg = "initial commit" and no parent. */
    static void makeInitCommit(){
        Date initDate = Date.from(Instant.parse("1970-01-01T00:00:00Z"));
        Commit initCommit = new Commit("initial commit", initDate, null, new TreeMap<>());
        commitHelper(initCommit);
    }

    /** Private helper method to dump commit object into newly created file
     * named by serialized byte array's SHA-1 hash. Also update the head commit
     * of current branch and cache the commit object for quick runtime access. */
    private static void commitHelper(Commit c){
        byte[] commitByte = serialize(c);
        String commitHash = sha1(commitByte);
        File cf = join(COMMITS, commitHash);
        try {
            cf.createNewFile();
        }
        catch (IOException e){
            System.err.println(e);
        }
        writeContents(cf, commitByte);

        Repository.headMap.put(Repository.currentBranch, commitHash);
        writeObject(join(Repository.GITLET_DIR, "headMap"), (Serializable) Repository.headMap);
        commitCache.put(commitHash, c);

    }
}
