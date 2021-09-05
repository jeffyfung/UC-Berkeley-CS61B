package byow.Core;

import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class Room {
    Position lowerLeft;
    Position upperRight;
//    Position center;

    public Room(Position lowerLeft, Position upperRight) {
        this.lowerLeft = lowerLeft;
        this.upperRight = upperRight;
    }

    /** Return a list of Room objects. ???.*/
    public static LinkedList<Room> buildRooms(WorldMap world) {
        // generate a random sequence of Position objects, limited by range of world map
        List<Position> posSeq = generateRandomPos(WorldMap.worldWidth, WorldMap.worldHeight,
                WorldMap.RANDOM);
        // insert elements from sequence into kdTree
        KdTree kdt = null;
        for (Position pos : posSeq) {
            kdt = KdTree.insert(kdt, pos);
            if (kdt.size >= Math.pow(2, KdTree.MAX_TREE_LAYERS) - 1) {
                break;
            }
        }
        if (kdt == null) {
            throw new RuntimeException("Empty KdTree");
        }
        LinkedList<Room> rooms = new LinkedList<>();
//        LinkedList<Position> partitionLL = new LinkedList<>();
//        LinkedList<Position> partitionUR = new LinkedList<>();
        for (KdTree.EmptyLeafExtensionSpace partition : kdt.getPartitionedSpace()) {
            Room room = sampleRoomFromPartition(partition, false, 0
                    , WorldMap.RANDOM);
//            partitionLL.add(partition.lowerLeft);
//            partitionUR.add(partition.upperRight);
            if (room != null) {
                drawRoom(world.world, room, Tileset.WALL, Tileset.FLOOR);
                rooms.add(room);
            }
        }
        KdTree.resetPartitionedSpace();
//        for (Position pt : partitionLL) {
//            world.world[pt.xy[0]][pt.xy[1]] = Tileset.GRASS;
//        }
//        for (Position pt : partitionUR) {
//            world.world[pt.xy[0]][pt.xy[1]] = Tileset.FLOWER;
//        }
        return rooms;
    }

    /** Return a sequence of randomly generated Position. Length of the sequence is limited by
     * lesser among width and height of the world. */
    private static LinkedList<Position> generateRandomPos(int width, int height, Random rand) {
        LinkedList<Integer> xList = new LinkedList<>();
        LinkedList<Integer> yList = new LinkedList<>();
        for (int i = 0; i < width; i++) {
            xList.add(i);
        }
        for (int i = 0; i < height; i++) {
            yList.add(i);
        }
        Collections.shuffle(xList, rand);
        Collections.shuffle(yList, rand);
        LinkedList<Position> out = new LinkedList<>();
        for (int i = 0; i < Math.min(width, height); i++) {
            out.add(new Position(xList.remove(), yList.remove()));
        }
        return out;
    }

    /** Return a Room object that is within the given EmptyLeafExtensionSpace object. Return null
     *  if the EmptyLeafExtensionSpace is <3*3 or no eligible. Resample up to 4 times if the
     *  EmptyLeafExtensionSpace is >=3*3 but the area sampled is <3*3. */
    static Room sampleRoomFromPartition(KdTree.EmptyLeafExtensionSpace partition,
                                                boolean checked, int counter, Random rand) {

        if (!checked) {
            int partitionWidth = partition.upperRight.xy[0] - partition.lowerLeft.xy[0];
            int partitionHeight = partition.upperRight.xy[1] - partition.lowerLeft.xy[1];
            if (partitionWidth < 3 || partitionHeight < 3) {
                return null;
            }
        }

        // sample room from partition
        int roomLowerLeftX = partition.lowerLeft.xy[0]
                + rand.nextInt((partition.upperRight.xy[0] - partition.lowerLeft.xy[0]) / 2);
        int roomLowerLeftY = partition.lowerLeft.xy[1]
                + rand.nextInt((partition.upperRight.xy[1] - partition.lowerLeft.xy[1]) / 2);
        Position roomLowerLeft = new Position(roomLowerLeftX, roomLowerLeftY);
        int roomUpperRightX = roomLowerLeft.xy[0]
                + rand.nextInt(partition.upperRight.xy[0] - roomLowerLeft.xy[0]);
        int roomUpperRightY = roomLowerLeft.xy[1]
                + rand.nextInt(partition.upperRight.xy[1] - roomLowerLeft.xy[1]);
        Position roomUpperRight = new Position(roomUpperRightX, roomUpperRightY);

        int roomWidth = roomUpperRight.xy[0] - roomLowerLeft.xy[0];
        int roomHeight = roomUpperRight.xy[1] - roomLowerLeft.xy[1];
        if (roomWidth < 3 || roomHeight < 3) {
            if (counter < 10) {
                return sampleRoomFromPartition(partition, true, counter + 1, rand);
            } else {
                // contingency for marginal partition size -> resulting in slender rooms
                return new Room(partition.lowerLeft, new Position(partition.upperRight.xy[0] - 1,
                 partition.upperRight.xy[1] - 1));
            }
        }
        return new Room(roomLowerLeft, roomUpperRight);
    }

    /** Change tile patterns located inside given room on the 2D tile array that represents the
     * world. Return the altered 2D tile array. */
    static TETile[][] drawRoom(TETile[][] tiles, Room room, TETile wallPattern,
                                 TETile floorPattern) {
        int[] lowerLeftCoor = room.lowerLeft.xy;
        int[] upperRightCoor = room.upperRight.xy;
        // draw interior - floor
        for (int i = lowerLeftCoor[0]; i <= upperRightCoor[0]; i += 1) {
            for (int j = lowerLeftCoor[1]; j <= upperRightCoor[1]; j += 1) {
                if (i == lowerLeftCoor[0] || i == upperRightCoor[0]
                        || j == lowerLeftCoor[1] || j == upperRightCoor[1]) {
                    tiles[i][j] = wallPattern;
                } else {
                    tiles[i][j] = floorPattern;
                }
            }
        }
        return tiles;
    }
}
