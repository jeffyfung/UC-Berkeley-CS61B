package timingtest;
import edu.princeton.cs.algs4.Stopwatch;

/**
 * Created by hug.
 */
public class TimeSLList {
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
        timeGetLast();
    }

    public static void timeGetLast() {
        // TODO: YOUR CODE HERE
        int N = 128000;
        int M = 10000;
        AList<Integer> n_lst = new AList<> ();
        AList<Double> t_lst = new AList<> ();
        AList<Integer> m_lst = new AList<> ();

        int n = 1000;
        while (n <= N) {
            SLList<Integer> lst = new SLList<> ();
            for (int i = 0; i < n; i += 1) {
                lst.addLast(10);
            }
            Stopwatch sw = new Stopwatch();
            for (int m = 0; m < M; m += 1) {
                lst.addLast(10);
            }
            t_lst.addLast(sw.elapsedTime());
            m_lst.addLast(M);
            n_lst.addLast(n);
            n *= 2;
        }

        System.out.println("Timing table for SLList.addLast");
        printTimingTable(n_lst, t_lst, m_lst);
    }

}
