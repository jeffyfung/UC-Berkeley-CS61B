package timingtest;
import edu.princeton.cs.algs4.Stopwatch;

/**
 * Created by hug.
 */
public class TimeAList {
    private static void printTimingTable(AList<Integer> Ns, AList<Double> times, AList<Integer> opCounts) {
        System.out.printf("%12s %12s %12s %12s\n", "N", "time (s)", "# ops", "microsec/op");
        System.out.printf("------------------------------------------------------------\n");
        for (int i = 0; i < Ns.size(); i += 1) {
            int N = Ns.get(i);
            double time = times.get(i);
            int opCount = opCounts.get(i);
            double timePerOp = time / opCount * 1e6;
            System.out.printf("%12d %12.2f %12d %12.2f\n", N, time, opCount, timePerOp);
        }
    }

    public static void main(String[] args) {
        timeAListConstruction();
    }

    public static void timeAListConstruction() {
        // TODO: YOUR CODE HERE

        int N = 20480000;
        AList<Integer> n_lst = new AList<> ();
        AList<Double> t_lst = new AList<> ();

        int n = 1000;
        while (n <= N) {
            Stopwatch sw = new Stopwatch();
            AList<Integer> lst = new AList<> ();
            for (int i = 0; i < n; i += 1) {
                lst.addLast(10);
            }
            n_lst.addLast(n);
            t_lst.addLast(sw.elapsedTime());
            n *= 2;
        }

        // call printTimingTable(n_lst, t_lst, op_list)
        System.out.println("Timing table for addLast");
        printTimingTable(n_lst, t_lst, n_lst);
    }
}
