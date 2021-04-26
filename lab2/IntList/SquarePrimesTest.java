package IntList;

import static org.junit.Assert.*;

import jh61b.junit.In;
import org.junit.Test;

public class SquarePrimesTest {

    /**
     * Here is a test for isPrime method. Try running it.
     * It passes, but the starter code implementation of isPrime
     * is broken. Write your own JUnit Test to try to uncover the bug!
     */
    @Test
    public void testSquarePrimesSimple() {
        IntList lst = IntList.of(14, 15, 16, 17, 18);
        boolean changed = IntListExercises.squarePrimes(lst);
        assertEquals("14 -> 15 -> 16 -> 289 -> 18", lst.toString());
        assertTrue(changed);
    }

    @Test
    public void testSquarePrimesZero() {
        IntList lst = IntList.of(0, 14, 15, 16);
        boolean changed = IntListExercises.squarePrimes(lst);
        assertEquals("0 -> 14 -> 15 -> 16", lst.toString());
        assertFalse(changed);
    }

    @Test
    public void testSquarePrimesMultiplePrime() {
        IntList lst = IntList.of(37, 14, 17, 23);
        boolean changed = IntListExercises.squarePrimes(lst);
        assertEquals("1369 -> 14 -> 289 -> 529", lst.toString());
        assertTrue(changed);
    }

    @Test
    public void testSquarePrimesNegative() {
        IntList lst = IntList.of(4, 8, -5);
        boolean changed = IntListExercises.squarePrimes(lst);
        assertEquals("4 -> 8 -> -5", lst.toString());
        assertFalse(changed);
    }

    @Test
    public void testSquarePrimesEmpty() {
        IntList lst = IntList.of();
        boolean changed = IntListExercises.squarePrimes(lst);
        assertNull(lst);
        assertFalse(changed);
    }
}
