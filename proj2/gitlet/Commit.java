package gitlet;

// TODO: any imports you need here

import static gitlet.Utils.*;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;
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
    private Map<String, String> blobMap;
    /** Hash of (first) parent commit. Can be used to trace back to initial commit */
    private String parentCommitHash;
    /** Hash of second parent commit. Null for non-merge commit */
    private String secondParentCommitHash;
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
        Commit initCommit = new Commit("initial commit", initDate,
                null, new Repository.StringTreeMap());
        commitHelper(initCommit);
    }

    // update to accommodate rm command (staged for removal)
    /** Create a new commit. Its parent commit is the HEAD of current branch and is represented by hash.
     *  Record the time of commit. Its blobMap is identical to the parent except for files in STAGE.
     *  If parent commit does not contain a blobMap (i.e. init commit), the new blobMap contains
     *  only reference to files in STAGE in the format of (file name -> blob hash of serialized file content)
     *  */
    public static void makeCommit(String commitMsg) {
        List<String> filesInStage = plainFilenamesIn(Repository.STAGE);
        if (filesInStage.size() == 0) {
            throw Utils.error("No changes added to the commit.");
        }
        Date curDate = new Date();
        String parentCommitHash = Repository.getHeadHash();
        Commit parentCommit = Repository.getCommitFromHash(parentCommitHash);
        Map<String, String> parentBlobMap = parentCommit.getBlobMap();
        Map<String, String> commitBlobMap = new Repository.StringTreeMap();
        if (parentBlobMap != null) {
            commitBlobMap.putAll(parentBlobMap);
        }
        for (String f : filesInStage) {
            // check stage for removal by checking if fileName starts with "-del-"
                // if so remove corresponding key from commitBlobMap
            // if fileName does not exist in current commit (i.e. commitBlobMap)
                // -> address exception in Repository.remove
            int keyStringLen = Repository.keyString.length();
            if (f.length() > keyStringLen
                    && f.substring(0, keyStringLen).equals(Repository.keyString)) {
                commitBlobMap.remove(f.substring(keyStringLen));
                continue;
            }
            byte[] blobBytes = readContents(join(Repository.STAGE, f));
            String blobHash = sha1(blobBytes);
            writeContents(join(Repository.BLOBS, blobHash), blobBytes);
            commitBlobMap.put(f, blobHash);
        }
        Commit curCommit = new Commit(commitMsg, curDate, parentCommitHash, commitBlobMap);
        curCommit.parentCommit = parentCommit;
        commitHelper(curCommit);
    }

    /** Private helper method to dump commit object into newly created file
     * named by serialized byte array's SHA-1 hash. Also update the head commit
     * of current branch and cache the commit object for quick runtime access. */
    private static void commitHelper(Commit c){
        byte[] commitByte = serialize(c);
        String commitHash = sha1(commitByte);
        File cf = join(COMMITS, commitHash);
        writeContents(cf, commitByte);

        Repository.headMap.put(Repository.currentBranch, commitHash);
        writeObject(join(Repository.GITLET_DIR, "headMap"), (Serializable) Repository.headMap);
        commitCache.put(commitHash, c);
    }

    public Map<String, String> getBlobMap(){
        return this.blobMap;
    }
}
