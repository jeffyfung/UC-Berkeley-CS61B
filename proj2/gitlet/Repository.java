package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static java.nio.file.StandardCopyOption.*;

import static gitlet.Utils.*;

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author Jeffrey Fung
 */
public class Repository {
    /** List all instance variables of the Repository class here with a useful
     *  comment above them describing what that variable represents and how that
     *  variable is used. We've provided two examples for you. */

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
    static String currentBranch;
    /** label for files that are staged to be removed */
    static String keyString = "[[del[[";
    static int keyStringLen = keyString.length();
//    /** Map from blob hash to file name */
//    static Map<String, String> blobToFileNameMap = new TreeMap<>();

    /** Call setupPersistence to create folders holding .gitlet, stage, commits, blobs.
     * Make an initial commit by calling makeInitCommit.
     * Print error if .gitlet already exists. */
    static void initRepo() {
        if (plainFilenamesIn(GITLET_DIR) != null) {
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            return;
        }
        setupPersistence();
        Commit.makeInitCommit();
    }

    private static void setupPersistence(){
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
            System.out.println("File does not exist.");
            return;
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
        if (plainFilenamesIn(Repository.STAGE).size() == 0) {
            System.out.println("No changes added to the commit.");
            return;
        }
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
            System.out.println("No reason to remove the file.");
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
            System.out.println("Found no commit with that message.");
        }
    }

    /** Display information on branches, file(s) staged for addition or removal,
     *  modifications not staged for commit and untracked files. Mark the current
     *  branch with an asterisk. Entries listed in lexicographical order, not
     *  counting asterisk. */
    // confirm compatibility with reset and merge
    static void status() {
        List<String> filesInCWD = plainFilenamesIn(CWD);
        List<String> filesInStage = plainFilenamesIn(STAGE);
        Map<String, String> curCommitBlobMap = getCommitFromHash(getHeadHash()).getBlobMap();
        displayBranchInfo();
        displayStagedFiles(filesInStage);
        displayModificationsNotStagedForCommit(filesInCWD, filesInStage, curCommitBlobMap);
        displayUntrackedFiles(filesInCWD, filesInStage, curCommitBlobMap);
    }

    /** Copy the file as it exists in the head commit to CWD. Overwrite if necessary.
     *  No need to stage the file. */
    static void checkout(String fileName) {
        String blobHash = getCommitFromHash(getHeadHash()).getBlobMap().get(fileName);
        if (blobHash == null) {
            System.out.println("File does not exist in that commit.");
            return;
        }
        String fileContent = readContentsAsString(join(BLOBS, blobHash));
        writeContents(new File(fileName), fileContent);
    }

    /** Copy the file as it exists in the commit specified by the hash to CWD. Overwrite
     *  if necessary. No need to stage the file. */
    static void checkout(String commitHash, String fileName) {
        Commit commitFromHash = getCommitFromHash(commitHash);
        if (commitFromHash == null) {
            return;
        }
        String blobHash = commitFromHash.getBlobMap().get(fileName);
        if (blobHash == null) {
            System.out.println("File does not exist in that commit.");
            return;
        }
        String fileContent = readContentsAsString(join(BLOBS, blobHash));
        writeContents(new File(fileName), fileContent);
    }

    /** Copy all files tracked by the head commit of the given branch to CWD. Overwrite
     *  if necessary. Any file not tracked by the head commit of given branch
     *  will be deleted from CWD. Clear the staging area unless the given branch
     *  is the current branch. Set this as the current branch. */
    static void checkoutBranch(String branchName){
        String targetHeadHash = getHeadHash(branchName);
        if (targetHeadHash == null) {
            System.out.println("No such branch exists.");
            return;
        }

        Map<String, String> curHeadBlobMap = getCommitFromHash(getHeadHash()).getBlobMap();
        System.out.println(currentBranch); // <-- print what?
        for (String f : plainFilenamesIn(CWD)) {
            if (!curHeadBlobMap.containsKey(f)) {
                System.out.println("There is an untracked file in the way; delete it, " +
                        "or add and commit it first.");
                return;
            }
        }
        // Delete all files in CWD
        for (String f : plainFilenamesIn(CWD)) {
            restrictedDelete(f);
        }
        // Copy files from branch head to CWD
        for (Map.Entry<String, String> blobPair : getCommitFromHash(targetHeadHash).getBlobMap().entrySet()) {
            writeContents(new File(blobPair.getKey()),
                    readContentsAsString(join(BLOBS, blobPair.getValue())));
        }
        // Clear stage if target branch is not current branch
        if (branchName.equals(currentBranch)) {
            System.out.println("No need to checkout the current branch");
        }
        else {
            clearStage();
        }
        // Set target branch to current branch
        currentBranch = branchName;
        writeContents(join(GITLET_DIR, "currentBranch"), currentBranch);
    }

    /** Loop over all files in STAGE to print out a list of files staged for addition and a list of
     *  files staged for removal. Listed in lexicographical order. */
    static void displayStagedFiles(List<String> filesInStage) {
        StringBuilder stagedFiles = new StringBuilder();
        StringBuilder filesStagedForRemoval = new StringBuilder();
        for (String f : filesInStage) {
            if (f.length() > keyStringLen
                    && f.substring(0, keyStringLen).equals(Repository.keyString)) {
                filesStagedForRemoval.append(f.substring(keyStringLen));
                filesStagedForRemoval.append("\n");
            }
            else {
                stagedFiles.append(f);
                stagedFiles.append("\n");
            }
        }
        System.out.println("=== Staged Files ===");
        System.out.println(stagedFiles);
        System.out.println("=== Removed Files ===");
        System.out.println(filesStagedForRemoval);
    }

    /** Create a new branch and point its head to current commit (via commit hash).
     *  Do not switch to the new branch. */
    static void branch(String branchName) {
        if (headMap.isEmpty()) {
            headMap = readObject(join(GITLET_DIR, "headMap"), StringTreeMap.class);
        }
        if (headMap.containsKey(branchName)) {
            System.out.println("A branch with that name already exists.");
            return;
        }
        String headHash = getHeadHash();
        headMap.put(branchName, headHash);
        writeObject(join(GITLET_DIR, "headMap"), (Serializable) headMap);
    }

    /** Loop over headMap and print all branch names in lexicographical order. Add an asterisk in front
     *  of the name of current branch. */
    static void displayBranchInfo() {
        System.out.println("=== Branches ===");
        if (headMap.isEmpty()) {
            headMap = readObject(join(GITLET_DIR, "headMap"), StringTreeMap.class);
        }
        currentBranch = readContentsAsString(join(GITLET_DIR, "currentBranch"));
        for (Map.Entry<String, String> branchPair : headMap.entrySet()) {
            if (branchPair.getKey().equals(currentBranch)) {
                System.out.println("*".concat(branchPair.getKey()));
            }
            else { System.out.println(branchPair.getKey()); }
        }
        System.out.println();
    }

    /** Loop over blobMap of current commit and blobMap of STAGE to print out names of files
     *  that satisfy any one of the following criteria:
     *      1) file version in CurCommit different from file version in CWD, file not staged
     *      2) file version in STAGE different from file version in CWD
     *      3) exists in STAGE but deleted from CWD
     *      4) tracked in current commit but deleted from CWD; file not staged for removal */
    static void displayModificationsNotStagedForCommit(List<String> filesInCWD, List<String> filesInStage,
                                                       Map<String, String> curCommitBlobMap) {
        System.out.println("=== Modifications Not Staged For Commit ===");
        // serialize and hash files in CWD for ease of comparison of contents
        // CWDBlobMap = new HashMap<String, String>
        // STAGEBlobMap = new HashMap<String, String>
        // out = new LinkedList<>()
        // loop over STAGEBlobMap
        // if fileName !in CWDBlobMap.keySet()
        // print fileName (3)
        // elif blob hash in STAGE != blob hash in CWD
        // print fileName (2)
        // remove from curCommitBlobMap
        // loop over curCommitBlobMap <-O(N)
        // if fileName !in CWD
        // if keyString + fileName !in STAGE -> print fileName (4)
        // elif blob hash in curCommit != blob hash in CWD
        // if fileName !in STAGE(??) -> print fileName (1)

        // Create blobMap for CWD and STAGE
        Map<String, String> CWDBlobMap = new HashMap<>();
        for (String f : filesInCWD) {
            String blobHash = sha1(readContents(new File(f)));
            CWDBlobMap.put(f, blobHash);
        }
        Map<String, String> STAGEBlobMap = new HashMap<>();
        for (String sf : filesInStage) {
            String sBlobHash = sha1(readContents(join(STAGE, sf)));
            STAGEBlobMap.put(sf, sBlobHash);
        }
        // Create set to store unique file names that satisfy any criteria
        Set<String> out = new HashSet<>();

        for (Map.Entry<String, String> blobPair : STAGEBlobMap.entrySet()) {
            String file = blobPair.getKey();
            if (file.substring(0, keyStringLen).equals(keyString)) {
                continue; }
            String blobHash = blobPair.getValue();
            if (!CWDBlobMap.containsKey(file) || !blobHash.equals(CWDBlobMap.get(file))) {
                out.add(file);
            }
        }

        for (Map.Entry<String, String> blobPair : curCommitBlobMap.entrySet()) {
            String file = blobPair.getKey();
            String blobHash = blobPair.getValue();
            if (!CWDBlobMap.containsKey(file) && !STAGEBlobMap.containsKey(keyString.concat(file))) {
                out.add(file);
            }
            if (CWDBlobMap.get(file) != null && !blobHash.equals(CWDBlobMap.get(file))
                     && !STAGEBlobMap.containsKey(file)) {
                out.add(file);
            }
        }

        // Sort and print output list
        List<String> output = new LinkedList<>(out);
        Collections.sort(output);
        for (String i : output) { System.out.println(i); }
        System.out.println();
    }

    /** Print out all files in CWD that are neither staged for addition nor
     *  tracked in current commit, in lexicographical order. Also include files staged
     *  for removal but then re-created without Gitlet's knowledge. */
    static void displayUntrackedFiles(List<String> filesInCWD, List<String> filesInStage,
                                      Map<String, String> curCommitBlobMap) {
        System.out.println("=== Untracked Files ===");
        for (String f : filesInCWD) {
            if (!curCommitBlobMap.containsKey(f) && !filesInStage.contains(f)) {
                System.out.println(f);
            }
        }
        System.out.println();
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
        DateFormat dateFormat = new SimpleDateFormat("E MMM dd HH:mm:ss yyyy Z");
        System.out.println("Date: ".concat(dateFormat.format(commit.getCommitDate())));
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

    /** Get hash string of input branch. Check if headMap is empty.
     *  If empty, deserialize headMap file. */
    static String getHeadHash(String branchName) {
        if (headMap.isEmpty()) {
            headMap = readObject(join(GITLET_DIR, "headMap"), StringTreeMap.class);
        }
        return headMap.get(branchName);
    }

    /** Get hash string of current branch head. Check if headMap is empty.
     *  If empty, deserialize headMap file. */
    static String getHeadHash() {
        if (headMap.isEmpty()) {
            headMap = readObject(join(GITLET_DIR, "headMap"), StringTreeMap.class);
        }
        currentBranch = readContentsAsString(join(GITLET_DIR, "currentBranch"));
        System.out.println("inside func: ".concat(currentBranch));
        return headMap.get(currentBranch);
    }

    /** Get a commit object from its hash.
     * Cache the (hash, commit) pair if it has not been done so. */
    static Commit getCommitFromHash(String hash) {
        if (!Commit.commitCache.containsKey(hash)) {
            try {
                Commit targetCommit = readObject(join(Commit.COMMITS, hash), Commit.class);
                Commit.commitCache.put(hash, targetCommit);
                return targetCommit;
            }
            catch (IllegalArgumentException iae) {
                System.out.println("No commit with that id exists.");
            }
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
