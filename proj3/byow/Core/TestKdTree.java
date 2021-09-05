package byow.Core;

import org.junit.Test;
import java.util.LinkedList;
import static org.junit.Assert.*;

public class TestKdTree {

    @Test
    public void testCompare() {
        Position pos1 = new Position(1, 4);
        Position pos2 = new Position(3, 3);
        assertTrue(Position.compare(pos1, pos2, 1));
        assertFalse(Position.compare(pos1, pos2, 0));
    }

    @Test
    public void testSimpleInsert() {
        KdTree testKdTree = null;
        for (int i = 0; i < 2; i += 1) {
            testKdTree = KdTree.insert(testKdTree, new Position(i * 4, i * 4));
        }
        assertEquals(2, testKdTree.size);;
    }

    @Test
    public void testUnsuccessfulInsert() {
        KdTree testKdTree = null;
        for (int i = 0; i < 3; i += 1) { // Position too close to each other
            testKdTree = KdTree.insert(testKdTree, new Position(i, i));
        }
        assertEquals(1, testKdTree.size);;
    }

    @Test
    public void testInsertTreeDepthExceeded() {
        KdTree testKdTree = null;
        for (int i = 0; i < 10; i += 1) {
            testKdTree = KdTree.insert(testKdTree, new Position(i * 4, i * 4));
            testKdTree = KdTree.insert(testKdTree, new Position(i * -4, i * -4));
        }
        assertEquals(5, testKdTree.size);;
    }

    @Test
    public void testSuccessfulMultipleInsert() {
        // TODO: add more?
    }

    @Test
    public void testPartitionCreationFromKdTree() {
        KdTree testKdTree = KdTree.insert(null, new Position(5, 5));
        testKdTree = KdTree.insert(testKdTree, new Position(2, 2));
        testKdTree = KdTree.insert(testKdTree, new Position(10, 3));
        LinkedList<KdTree.EmptyLeafExtensionSpace> ls = testKdTree.getPartitionedSpace();
        assertEquals(new Position(5, 3), ls.get(0).lowerLeft);
        assertEquals(new Position(49, 49), ls.get(0).upperRight);
        assertEquals(new Position(5, 0), ls.get(1).lowerLeft);
        assertEquals(new Position(49, 3), ls.get(1).upperRight);
        assertEquals(new Position(0, 2), ls.get(2).lowerLeft);
        assertEquals(new Position(5, 49), ls.get(2).upperRight);
        assertEquals(new Position(0, 0), ls.get(3).lowerLeft);
        assertEquals(new Position(5, 2), ls.get(3).upperRight);
        KdTree.resetPartitionedSpace();
    }

    @Test
    public void testPartitionCreationFromKdTreeMultipleNodes() {
        KdTree testKdTree = KdTree.insert(null, new Position(5, 5));
        testKdTree = KdTree.insert(testKdTree, new Position(2, 2));
        testKdTree = KdTree.insert(testKdTree, new Position(10, 3));
        testKdTree = KdTree.insert(testKdTree, new Position(4, 7));
        LinkedList<KdTree.EmptyLeafExtensionSpace> ls = testKdTree.getPartitionedSpace();
        assertEquals(new Position(4, 2), ls.get(2).lowerLeft);
        assertEquals(new Position(5, 49), ls.get(2).upperRight);
        assertEquals(new Position(0, 2), ls.get(3).lowerLeft);
        assertEquals(new Position(4, 49), ls.get(3).upperRight);
        KdTree.resetPartitionedSpace();
    }
}
