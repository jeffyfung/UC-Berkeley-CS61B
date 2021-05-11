package deque;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;

import static org.junit.Assert.*;


/** Performs some basic linked list tests. */
public class ArrayDequeTest {

    @Test
    /** Adds a few things to the list, checking isEmpty() and size() are correct,
     * finally printing the results.
     *
     * && is the "and" operation. */
    public void addIsEmptySizeTest() {

        ArrayDeque<String> ad1 = new ArrayDeque<>();

		assertTrue("A newly initialized arraydeque should be empty", ad1.isEmpty());
		ad1.addFirst("front");

		// The && operator is the same as "and" in Python.
		// It's a binary operator that returns true if both arguments true, and false otherwise.
        assertEquals(1, ad1.size());
        assertFalse("ad1 should now contain 1 item", ad1.isEmpty());

		ad1.addLast("middle");
		assertEquals(2, ad1.size());

		ad1.addLast("back");
		assertEquals(3, ad1.size());

		System.out.println("Printing out deque: ");
		ad1.printDeque();
    }

    @Test
    /** Adds an item, then removes an item, and ensures that dll is empty afterwards. */
    public void addRemoveTest() {

        ArrayDeque<Integer> ad1 = new ArrayDeque<Integer>();
		// should be empty
		assertTrue("ad1 should be empty upon initialization", ad1.isEmpty());

		ad1.addFirst(10);
		// should not be empty
		assertFalse("ad1 should contain 1 item", ad1.isEmpty());

		ad1.removeFirst();
		// should be empty
		assertTrue("ad1 should be empty after removal", ad1.isEmpty());
    }

    @Test
    /* Tests removing from an empty deque */
    public void removeEmptyTest() {

        ArrayDeque<Integer> ad1 = new ArrayDeque<>();
        ad1.addFirst(3);

        ad1.removeLast();
        ad1.removeFirst();
        ad1.removeLast();
        ad1.removeFirst();

        int size = ad1.size();
        String errorMsg = "  Bad size returned when removing from empty deque.\n";
        errorMsg += "  student size() returned " + size + "\n";
        errorMsg += "  actual size() returned 0\n";

        assertEquals(errorMsg, 0, size);
    }

    @Test
    /* Check if you can create ArrayDeques with different parameterized types*/
    public void multipleParamTest() {

        ArrayDeque<String>  ad1 = new ArrayDeque<String>();
        ArrayDeque<Double>  ad2 = new ArrayDeque<Double>();
        ArrayDeque<Boolean> ad3 = new ArrayDeque<Boolean>();

        ad1.addFirst("string");
        ad2.addFirst(3.14159);
        ad3.addFirst(true);

        String s = ad1.removeFirst();
        double d = ad2.removeFirst();
        boolean b = ad3.removeFirst();
    }

    @Test
    /* check if null is return when removing from an empty ArrayDeque. */
    public void emptyNullReturnTest() {

        ArrayDeque<Integer> ad1 = new ArrayDeque<Integer>();

        boolean passed1 = false;
        boolean passed2 = false;
        assertEquals("Should return null when removeFirst is called on an empty Deque,", null, ad1.removeFirst());
        assertEquals("Should return null when removeLast is called on an empty Deque,", null, ad1.removeLast());
    }

    @Test
    /* Add large number of elements to deque; check if order is correct. */
    public void bigadequeTest() {

        ArrayDeque<Integer> ad1 = new ArrayDeque<Integer>();
        for (int i = 0; i < 1000000; i++) { //1000000
            ad1.addLast(i);
        }

        for (double i = 0; i < 500000; i++) { //500000
            assertEquals("Should have the same value", i, (double) ad1.removeFirst(), 0.0);
        }

        for (double i = 999999; i > 500000; i--) { // i = 999999; i > 500
            assertEquals("Should have the same value", i, ad1.removeLast(), 0.0);
        }
        assertEquals(1, ad1.size());
    }

    @Test
    public void downsizingTest1() {

        ArrayDeque<Integer> ad1 = new ArrayDeque<Integer>();
        for (int i = 0; i < 35; i++) {
            ad1.addLast(i);
        }

        for (int i = 0; i < 35; i++) {
            ad1.removeLast();
            if (ad1.size == 16) {
                assertEquals(64 , ad1.array_size);
            }
            else if (ad1.size == 15) {
                assertEquals(32, ad1.array_size);
            }
            else if (ad1.size == 10) {
                assertEquals(32, ad1.array_size);
            }
            else if (ad1.size == 7) {
                assertEquals(16, ad1.array_size);
            }
        }
        assertEquals(0, ad1.size);
        assertEquals(8, ad1.array_size);
    }

    @Test
    public void downsizingTest2() {

        ArrayDeque<Integer> ad1 = new ArrayDeque<Integer>();
        for (int i = 0; i < 35; i++) {
            ad1.addFirst(i);
        }

        for (int i = 0; i < 35; i++) {
            ad1.removeFirst();
            if (ad1.size == 16) {
                assertEquals(64 , ad1.array_size);
            }
            else if (ad1.size == 15) {
                assertEquals(32, ad1.array_size);
            }
            else if (ad1.size == 10) {
                assertEquals(32, ad1.array_size);
            }
            else if (ad1.size == 7) {
                assertEquals(16, ad1.array_size);
            }
        }
        assertEquals(0, ad1.size);
        assertEquals(8, ad1.array_size);
    }

//    @Test
//    /* Check if some lists are equal */
//    public void compareDequeTest() {
//
//        ArrayDeque<Integer> l1 = new ArrayDeque<>();
//        l1.addLast(2);
//        l1.addLast(3);
//        l1.addLast(4);
//        l1.removeFirst();
//        l1.removeLast();
//
//        ArrayDeque<Integer> l2 = new ArrayDeque<>();
//        l2.addFirst(3);
//
//        assertTrue("Should have the same content", l1.equals(l2));
//
//        ArrayDeque<Integer> l3 = new ArrayDeque<>();
//        assertFalse("Should have different contents", l1.equals(l3));
//
//        for (int i = 0; i < 5; i++) {
//            l3.addFirst(100);
//        }
//        assertFalse("Should have different contents", l1.equals(l3));
//        assertFalse("Should have different contents", l2.equals(l3));
//        assertFalse("Should have different contents", l3.equals(l2));
//
//        /* 2 empty lists */
//        ArrayDeque<Integer> l4 = new ArrayDeque<>();
//        ArrayDeque<Integer> l5 = new ArrayDeque<>();
//        assertTrue("Should have the same content (null)", l4.equals(l5));
//
//        for (int i = 0; i < 10; i++) {
//            l4.addFirst(2);
//            l5.addLast(2);
//        }
//        assertTrue("Should have the same content (null)", l4.equals(l5));
//    }

    @Test
    /* Test get & getRecursive */
    public void getIndex() {

        ArrayDeque<Integer> l1 = new ArrayDeque<>();
        for (int i = 0; i < 5; i++) {
            l1.addLast(i);
        }
        for (int i = 0; i < 5; i++) {
            assertEquals((Integer) i, l1.get(i));
        }
        // get out-of-range index
        assertNull(l1.get(10));

        // get index from empty list
        ArrayDeque<Integer> l2 = new ArrayDeque<>();
        assertNull(l2.get(0));
        assertNull(l2.get(2));
    }

    @Test
    public void randomizedTest() {
        ArrayDeque<Integer> ad1 = new ArrayDeque<>();
        LinkedListDeque<Integer> lld1 = new LinkedListDeque<Integer>();

        int N = 50000;
        for (int i = 0; i < N; i++) {
            int operationNumber = StdRandom.uniform(0, 6);
            // 0: addLast
            // 1: addFirst
            // 2: size
            // 3: get() <- a random number within range
            // 4: removeLast
            // 5: removeFirst

            if (operationNumber == 0) {
                int randVal = StdRandom.uniform(0, 100);
                ad1.addLast(randVal);
                lld1.addLast(randVal);
            }
            if (operationNumber == 1) {
                int randVal = StdRandom.uniform(0, 100);
                ad1.addFirst(randVal);
                lld1.addFirst(randVal);
            }
            else if (operationNumber == 2) {
                assertEquals(ad1.size(), lld1.size());
            }
            else if (operationNumber == 3) {
                int ad_size = ad1.size();
                int randIndex = StdRandom.uniform(0, Math.max(1, ad_size));
                assertEquals(ad1.get(randIndex), lld1.get(randIndex));
            }
            else if (operationNumber == 4) {
                if (ad1.size() > 0) {
                    assertEquals(ad1.removeLast(), lld1.removeLast());
                }
            }
            else {
                if (ad1.size() > 0) {
                    assertEquals(ad1.removeFirst(), lld1.removeFirst());
                }
            }
        }
    }

    @Test
    public void addMultipleItems() {
        ArrayDeque<Integer> ad1 = new ArrayDeque<>();
        for (int i = 0; i < 1000; i++) {
            ad1.addFirst(i);
        }
        assertEquals(1000, ad1.size());
        assertEquals(0, (int) ad1.get(999));
        assertEquals(999, (int) ad1.get(0));
        assertEquals(997, (int) ad1.get(2));

        ArrayDeque<Integer> ad2 = new ArrayDeque<>();
        for (int i = 0; i < 1000; i++) {
            ad2.addLast(i);
        }
        assertEquals(1000, ad2.size());
        assertEquals(0, (int) ad2.get(0));
        assertEquals(999, (int) ad2.get(999));
    }
}
