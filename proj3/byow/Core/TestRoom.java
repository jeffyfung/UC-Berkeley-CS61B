package byow.Core;

import byow.TileEngine.Tileset;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestRoom {

    @Test
    public void TestDrawRoomsSimple() {
        int nullCounter = 0;
        Engine test = new Engine();

        KdTree testKdTree = KdTree.insert(null, new Position(5, 5));
        testKdTree = KdTree.insert(testKdTree, new Position(2, 2));
        testKdTree = KdTree.insert(testKdTree, new Position(10, 3));
        for (KdTree.EmptyLeafExtensionSpace partition : testKdTree.getPartitionedSpace()) {
            Room room = Room.sampleRoomFromPartition(partition, false, 0
                    , test.random);
            if (room == null) {
                nullCounter += 1;
                continue;
            }
            Room.drawRoom(test.tiles, room, Tileset.WALL, Tileset.FLOOR);
        }
        KdTree.resetPartitionedSpace();
        assertEquals(1, nullCounter);
    }

}
