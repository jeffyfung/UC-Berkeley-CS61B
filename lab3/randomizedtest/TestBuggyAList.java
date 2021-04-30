package randomizedtest;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Created by hug.
 */
public class TestBuggyAList {
  // YOUR TESTS HERE
    @Test
    public void testThreeAddThreeRemove() {
        BuggyAList<Integer> buggy_lst = new BuggyAList<> ();
        AListNoResizing<Integer> lst = new AListNoResizing<> ();

//        buggy_lst.addLast(0);
//        lst.addLast(0);
        buggy_lst.addLast(4);
        lst.addLast(4);
        buggy_lst.addLast(5);
        lst.addLast(5);
        buggy_lst.addLast(6);
        lst.addLast(6);
        assertEquals(lst.getLast(), buggy_lst.getLast());

        for (int i = 0; i < 3; i += 1) {
            assertEquals(lst.removeLast(), buggy_lst.removeLast());
        }
    }

    @Test
    public void randomizedTest() {
        AListNoResizing<Integer> L = new AListNoResizing<>();
        BuggyAList<Integer> buggy_L = new BuggyAList<>();

        int N = 50000;
        for (int i = 0; i < N; i += 1) {
            int operationNumber = StdRandom.uniform(0, 4);
            if (operationNumber == 0) {
                // addLast
                int randVal = StdRandom.uniform(0, 100);
                L.addLast(randVal);
                buggy_L.addLast(randVal);
            }
            else if (operationNumber == 1) {
                // size
                assertEquals(L.size(), buggy_L.size());
            }
            else if (operationNumber == 2) {
                // get last
                if (L.size() > 0) {
                    assertEquals(L.getLast(), buggy_L.getLast());
                }
            }
            else if (operationNumber == 3) {
                // remove last
                if (L.size() > 0) {
                    assertEquals(L.removeLast(), buggy_L.removeLast());
                }
            }
        }
    }
}
