package byow.Core;

import byow.TileEngine.TERenderer;
import byow.TileEngine.Tileset;
import org.junit.Test;
import static org.junit.Assert.*;

public class TestRoom {

    @Test
    public void TestDrawRoomsSimple() {
        int nullCounter = 0;
        WorldMap testWorld = new WorldMap();

        KdTree testKdTree = KdTree.insert(null, new Position(5, 5));
        testKdTree = KdTree.insert(testKdTree, new Position(2, 2));
        testKdTree = KdTree.insert(testKdTree, new Position(10, 3));
        for (KdTree.EmptyLeafExtensionSpace partition : testKdTree.getPartitionedSpace()) {
            Room room = Room.sampleRoomFromPartition(partition, false, 0
                    , WorldMap.RANDOM);
            if (room == null) {
                nullCounter += 1;
                continue;
            }
            Room.drawRoom(testWorld.world, room, Tileset.WALL, Tileset.FLOOR);
        }
        KdTree.resetPartitionedSpace();
        assertEquals(1, nullCounter);

        // visualize altered tiles
        TERenderer ter = new TERenderer();
        ter.initialize(WorldMap.worldWidth, WorldMap.worldHeight);
        ter.renderFrame(testWorld.world);
    }

}
