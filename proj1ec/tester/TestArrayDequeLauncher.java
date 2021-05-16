package tester;

import static org.junit.Assert.*;

import edu.princeton.cs.introcs.StdRandom;
import org.junit.Test;
import student.StudentArrayDeque;

public class TestArrayDequeLauncher {

    @Test
    public void randomizedTest1() {
        // 0 -> addFirst
        // 1 -> addLast
        // 2 -> removeFirst (only when size() > 0)
        // 3 -> removeLast (only when size() > 0)
        StudentArrayDeque<Integer> sad1 = new StudentArrayDeque<>();
        ArrayDequeSolution<Integer> solu = new ArrayDequeSolution<>();
        String err_msg = "";

        for (int i = 0; i < 200; i += 1) {
            int action = StdRandom.uniform(0, 4);
            Integer randVal = StdRandom.uniform(0, 100);

            if (action == 0) {
                sad1.addLast(randVal);
                solu.addLast(randVal);
                Integer tmp_a = solu.get(solu.size() - 1);
                Integer tmp_b = sad1.get(sad1.size() - 1);
                err_msg = "addLast(" + randVal + ")\n" + err_msg;
                assertEquals(err_msg, tmp_a, tmp_b);
            } else if (action == 1) {
                sad1.addFirst(randVal);
                solu.addFirst(randVal);
                err_msg = "addFirst(" + randVal + ")\n" + err_msg;
                assertEquals(err_msg, solu.get(0), sad1.get(0));
            } else if (action == 2) {
                if (solu.size() > 0) {
                    Integer a = solu.removeLast();
                    Integer b = sad1.removeLast();
                    err_msg = "removeLast()\n" + err_msg;
                    assertEquals(err_msg, a, b);
                }
            } else if (solu.size() > 0) {
                Integer a = sad1.removeFirst();
                Integer b = solu.removeFirst();
                err_msg = "removeFirst()\n" + err_msg;
                assertEquals(err_msg, a, b);
            }
        }
    }
}
