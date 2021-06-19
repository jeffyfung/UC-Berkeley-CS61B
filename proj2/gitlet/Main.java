package gitlet;

import gitlet.Utils;
import gitlet.GitletException;
import gitlet.Repository;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author Jeffrey Fung
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            throw Utils.error("Must have at least one argument");
        }
        String firstArg = args[0];
        switch (firstArg) {
            case "init" -> {
                validateNumArgs("init", args, 1);
                Repository.initRepo();
            }
            case "add" -> {
                validateNumArgs("add", args, 2);
                Repository.add(args[1]);
            }
            case "commit" -> {
                if (args.length == 1) {
                    System.out.println("Please enter a commit message.");
                    return;
                }
                validateNumArgs("commit", args, 2);
                Repository.commit(args[1]);
            }
            case "rm" -> {
                validateNumArgs("rm", args, 2);
                Repository.remove(args[1]);
            }
            case "log" -> {
                validateNumArgs("log", args, 1);
                Repository.log();
            }
            case "global-log" -> {
                validateNumArgs("global-log", args, 1);
                Repository.globalLog();
            }
            case "find" -> {
                validateNumArgs("find", args, 2);
                Repository.find(args[1]);
            }
            case "status" -> {
                validateNumArgs("status", args, 1);
                Repository.status();
            }
            case "checkout" -> {
                if (args.length == 1 || args.length > 4) {
                    throw Utils.error("Invalid number of arguments for: %s.", args[0]);
                }
                else if (args.length == 2) {
                    Repository.checkoutBranch(args[1]);
                }
                else if (args[1].equals("--")) {
                    validateNumArgs("checkout", args, 3);
                    Repository.checkout(args[2]);
                }
                else if (args[2].equals("--")) {
                    validateNumArgs("checkout", args, 4);
                    Repository.checkout(args[1], args[3]);
                }
                else {
                    throw Utils.error("Incorrect arguments for: %s.", args[0]);
                }
            }
            case "branch" -> {
                validateNumArgs("branch", args, 2);
                Repository.branch(args[1]);
            }
            case "rm-branch" -> {
                validateNumArgs("rm-branch", args, 2);
                Repository.rm_branch(args[1]);
            }
        }
    }

    public static void validateNumArgs(String cmd, String[] args, int n) {
        if (args.length != n) {
            throw new RuntimeException(
                    String.format("Invalid number of arguments for: %s.", cmd));
        }
    }
}
