package deque;

import org.junit.Test;
import static org.junit.Assert.*;
import edu.princeton.cs.algs4.StdRandom;


/** Performs some basic linked list tests. */
public class LinkedListDequeTest {

    @Test
    /** Adds a few things to the list, checking isEmpty() and size() are correct,
     * finally printing the results.
     *
     * && is the "and" operation. */
    public void addIsEmptySizeTest() {

        LinkedListDeque<String> lld1 = new LinkedListDeque<>();

		assertTrue("A newly initialized LLDeque should be empty", lld1.isEmpty());
		lld1.addFirst("front");

		// The && operator is the same as "and" in Python.
		// It's a binary operator that returns true if both arguments true, and false otherwise.
        assertEquals(1, lld1.size());
        assertFalse("lld1 should now contain 1 item", lld1.isEmpty());

		lld1.addLast("middle");
		assertEquals(2, lld1.size());

		lld1.addLast("back");
		assertEquals(3, lld1.size());

		System.out.println("Printing out deque: ");
		lld1.printDeque();
    }

    @Test
    /** Adds an item, then removes an item, and ensures that dll is empty afterwards. */
    public void addRemoveTest() {

        LinkedListDeque<Integer> lld1 = new LinkedListDeque<Integer>();
		// should be empty
		assertTrue("lld1 should be empty upon initialization", lld1.isEmpty());

		lld1.addFirst(10);
		// should not be empty
		assertFalse("lld1 should contain 1 item", lld1.isEmpty());

		lld1.removeFirst();
		// should be empty
		assertTrue("lld1 should be empty after removal", lld1.isEmpty());
    }

    @Test
    /* Tests removing from an empty deque */
    public void removeEmptyTest() {

        LinkedListDeque<Integer> lld1 = new LinkedListDeque<>();
        lld1.addFirst(3);

        lld1.removeLast();
        lld1.removeFirst();
        lld1.removeLast();
        lld1.removeFirst();

        int size = lld1.size();
        String errorMsg = "  Bad size returned when removing from empty deque.\n";
        errorMsg += "  student size() returned " + size + "\n";
        errorMsg += "  actual size() returned 0\n";

        assertEquals(errorMsg, 0, size);
    }

    @Test
    /* Check if you can create LinkedListDeques with different parameterized types*/
    public void multipleParamTest() {

        LinkedListDeque<String>  lld1 = new LinkedListDeque<String>();
        LinkedListDeque<Double>  lld2 = new LinkedListDeque<Double>();
        LinkedListDeque<Boolean> lld3 = new LinkedListDeque<Boolean>();

        lld1.addFirst("string");
        lld2.addFirst(3.14159);
        lld3.addFirst(true);

        String s = lld1.removeFirst();
        double d = lld2.removeFirst();
        boolean b = lld3.removeFirst();
    }

    @Test
    /* check if null is return when removing from an empty LinkedListDeque. */
    public void emptyNullReturnTest() {

        LinkedListDeque<Integer> lld1 = new LinkedListDeque<Integer>();

        boolean passed1 = false;
        boolean passed2 = false;
        assertEquals("Should return null when removeFirst is called on an empty Deque,", null, lld1.removeFirst());
        assertEquals("Should return null when removeLast is called on an empty Deque,", null, lld1.removeLast());
    }

    @Test
    /* Add large number of elements to deque; check if order is correct. */
    public void bigLLDequeTest() {

        LinkedListDeque<Integer> lld1 = new LinkedListDeque<Integer>();
        for (int i = 0; i < 1000000; i++) {
            lld1.addLast(i);
        }

        for (double i = 0; i < 500000; i++) {
            assertEquals("Should have the same value", i, (double) lld1.removeFirst(), 0.0);
        }

        for (double i = 999999; i > 500000; i--) {
            assertEquals("Should have the same value", i, (double) lld1.removeLast(), 0.0);
        }
    }

    @Test
    /* Check if some lists are equal */
    public void compareDequeTest() {

        LinkedListDeque<Integer> l1 = new LinkedListDeque<>();
        l1.addLast(2);
        l1.addLast(3);
        l1.addLast(4);
        l1.removeFirst();
        l1.removeLast();

        LinkedListDeque<Integer> l2 = new LinkedListDeque<>();
        l2.addFirst(3);

        boolean tmp = l1.equals(l2);
        assertTrue("Should have the same content", l1.equals(l2));

        LinkedListDeque<Integer> l3 = new LinkedListDeque<>();
        assertFalse("Should have different contents", l1.equals(l3));

        for (int i = 0; i < 5; i++) {
            l3.addFirst(100);
        }
        assertFalse("Should have different contents", l1.equals(l3));
        assertFalse("Should have different contents", l2.equals(l3));
        assertFalse("Should have different contents", l3.equals(l2));

        /* 2 empty lists */
        LinkedListDeque<Integer> l4 = new LinkedListDeque<>();
        LinkedListDeque<Integer> l5 = new LinkedListDeque<>();
        assertTrue("Should have the same content (null)", l4.equals(l5));

        for (int i = 0; i < 10; i++) {
            l4.addFirst(2);
            l5.addLast(2);
        }
        assertTrue("Should have the same content (null)", l4.equals(l5));
    }

    @Test
    /* Test get & getRecursive */
    public void getIndex() {

        LinkedListDeque<Integer> l1 = new LinkedListDeque<>();
        for (int i = 0; i < 5; i++) {
            l1.addLast(i);
        }
        for (int i = 0; i < 5; i++) {
            assertEquals((Integer) i, l1.get(i));
            assertEquals((Integer) i, l1.getRecursive(i));
        }
        // get out-of-range index
        assertNull(l1.get(10));

        // get index from empty list
        LinkedListDeque<Integer> l2 = new LinkedListDeque<>();
        assertNull(l2.get(0));
        assertNull(l2.get(2));
    }

    @Test
    public void randomizedTest() {
        LinkedListDeque<Integer> lld1 = new LinkedListDeque<>();
        AListNoResizing<Integer> al1 = new AListNoResizing<Integer>();

        int N = 50000;
        for (int i = 0; i < N; i++) {
            int operationNumber = StdRandom.uniform(0, 4);
            // 0: addLast
            // 1: size
            // 2: get() <- a random number within range
            // 3: removeLast

            if (operationNumber == 0) {
                int randVal = StdRandom.uniform(0, 100);
                lld1.addLast(randVal);
                al1.addLast(randVal);
            }
            else if (operationNumber == 1) {
                assertEquals(lld1.size(), al1.size());
            }
            else if (operationNumber == 2) {
                int lld_size = lld1.size();
                int randIndex = StdRandom.uniform(0, Math.max(1, lld_size));
                assertEquals(lld1.get(randIndex), al1.get(randIndex));
            }
            else {
                if (lld1.size() > 0) {
                    assertEquals(lld1.removeLast(), al1.removeLast());
                }
            }
        }
    }

    @Test
    public void forEachLoopTest() {
        LinkedListDeque<Integer> lld1 = new LinkedListDeque<>();
        for (int i = 0; i < 10; i++) {
            lld1.addLast(i);
        }

        int j = 0;
        for (int i : lld1) {
            int tmp = i;
            assertEquals(j, i);
            j += 1;
        }
    }
}
