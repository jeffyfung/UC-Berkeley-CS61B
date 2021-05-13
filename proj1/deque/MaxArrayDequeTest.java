package deque;
import java.util.Comparator;
import org.junit.Test;
import static org.junit.Assert.*;

public class MaxArrayDequeTest {

    private class IntComparator implements Comparator<Integer> {
        @Override
        public int compare(Integer a, Integer b) {
            return a.compareTo(b);
        }
    }

    private class InvIntComparator implements Comparator<Integer> {
        @Override
        public int compare(Integer a, Integer b) {
            return -1 * a.compareTo(b);
        }
    }

    private class StrComparator implements Comparator<String> {
        @Override
        public int compare(String a, String b) {
            return a.compareTo(b);
        }
    }

    // add get object method
    // @Override
    // comparator that access class attributes

    @Test
    public void nullArrayTest() {

        MaxArrayDeque<Integer> mad1 = new MaxArrayDeque<>(new IntComparator()); //empty list

        assertNull(mad1.max());
        assertNull(mad1.max(new InvIntComparator()));
    }

    @Test
    public void arrayTest1() {

        MaxArrayDeque<Integer> mad2 = new MaxArrayDeque<>(new IntComparator()); //{0,1,2,10,9,8}

        for (int i = 0; i < 3; i++) {
            mad2.addLast(i);
        }
        for (int i = 10; i > 7; i--) {
            mad2.addLast(i);
        }

        assertEquals(10, (long) mad2.max());
        assertEquals(0, (long) mad2.max(new InvIntComparator()));
    }

    @Test
    public void strArrayTest1() {

        MaxArrayDeque<String> mad3 = new MaxArrayDeque<>(new StrComparator()); //{"d","c","b","a"}
        assertNull(mad3.max());

        mad3.addFirst("a");
        mad3.addFirst("b");
        mad3.addFirst("c");
        mad3.addFirst("d");

        assertEquals("d", mad3.max());
    }
}
