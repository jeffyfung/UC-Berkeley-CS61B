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
        // TODO: what if args is empty?
        if (args.length == 0) {
            throw Utils.error("Must have at least one argument");
        }
        String firstArg = args[0];
        switch (firstArg) {
            case "init" -> {
                // TODO: handle the `init` command
                validateNumArgs("init", args, 1);
                Repository.initRepo();
            }
            case "add" -> {
                // TODO: handle the `add [filename]` command
                validateNumArgs("add", args, 2);
                Repository.add(args[1]);
            }
            case "commit" -> {
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
        }
    }

    public static void validateNumArgs(String cmd, String[] args, int n) {
        if (args.length != n) {
            if (cmd.equals("commit") && args.length == 1) {
                throw Utils.error("Please enter a commit message.");
            }
            throw new RuntimeException(
                    String.format("Invalid number of arguments for: %s.", cmd));
        }
    }
}
