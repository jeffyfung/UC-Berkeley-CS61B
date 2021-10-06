package byow.Core;

import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.util.*;

public class Room {
    static final TETile patternRoomWalls = Tileset.WALL;
    static final TETile patternRoomFloor = Tileset.FLOOR;
    static final TETile patternHallwayWalls = Tileset.WALL;
    static final TETile patternHallwayFloor = Tileset.FLOOR;
    Position lowerLeft;
    Position upperRight;
    Position center;

    public Room(Position lowerLeft, Position upperRight) {
        this.lowerLeft = lowerLeft;
        this.upperRight = upperRight;
        this.center = new Position((lowerLeft.getX() + upperRight.getX()) / 2,
                (lowerLeft.getY() + upperRight.getY()) / 2);
    }

    /** Return a list of Room objects. Each room is randomly sampled from a space partitioned
     * by leaf nodes of a random modified KdTree. */
    public static ArrayList<Room> buildRooms(Engine world) {
        // generate a random sequence of Position objects, limited by range of world map
        List<Position> posSeq = generateRandomPos(Engine.WORLD_WIDTH, Engine.WORLD_HEIGHT,
                world.random);
        // build KdTree from the random sequence
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
        // partition space using the KdTree and sample room from partitioned space
        ArrayList<Room> rooms = new ArrayList<>();
//        LinkedList<Position> partitionLL = new LinkedList<>();
//        LinkedList<Position> partitionUR = new LinkedList<>();
        for (KdTree.EmptyLeafExtensionSpace partition : kdt.getPartitionedSpace()) {
            Room room = sampleRoomFromPartition(partition, false, 0
                    , world.random);
//            partitionLL.add(partition.lowerLeft);
//            partitionUR.add(partition.upperRight);
            if (room != null) {
                drawRoom(world.tiles, room, patternRoomWalls, patternRoomFloor);
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

    /** Connect all rooms by repeatedly finding the least connected room and connecting it to the
     *  approximately closest room until all rooms are connected. Skip a candidate pair of
     *  connection if a connection cannot be formed between the pair of rooms. Draw the resulted
     *  hallway during each successful connection. */
    public static void connectRooms(Engine world, ArrayList<Room> rooms) { ;
        WQUDisjointSet roomsDS = new WQUDisjointSet(rooms);
        TileGraph g = new TileGraph(rooms);
        int srcRoomIdx = 0;
        while (!roomsDS.connectedToAllObjects(srcRoomIdx)) {
            int tgtRoomIdx = getApproxAdjacUnconnectedRoom(roomsDS, rooms, srcRoomIdx);
            // for test purpose only
            System.out.println("source room: " + srcRoomIdx);
            System.out.println("adjacent unconnected room: " + tgtRoomIdx);
            // TODO: alternative method for calculating tgtRoom e.g. use Dijkstra's ? (inaccessible)

            Hallway h = g.connect(srcRoomIdx, tgtRoomIdx);
            //             for testing purpose
//            if (srcRoomIdx == 9 && tgtRoomIdx == 6) {
//                drawRoom(world.tiles, rooms.get(srcRoomIdx), Tileset.WATER, Tileset.FLOOR);
//                drawRoom(world.tiles, rooms.get(tgtRoomIdx), Tileset.FLOWER, Tileset.FLOOR);
//                return;
//            }
            if (h != null) {
                roomsDS.connect(srcRoomIdx, tgtRoomIdx);
                drawSequence(world.tiles, h.getPath(), patternHallwayFloor);
                drawSequence(world.tiles, h.getWalls(), patternHallwayWalls);
                srcRoomIdx = roomsDS.getLoneliestElement();
            } else {
                srcRoomIdx = roomsDS.getNextLoneliestElement(srcRoomIdx);
            }
        }
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
            int partitionWidth = partition.upperRight.getX() - partition.lowerLeft.getX();
            int partitionHeight = partition.upperRight.getY() - partition.lowerLeft.getY();
            if (partitionWidth < 3 || partitionHeight < 3) {
                return null;
            }
        }

        int roomLowerLeftX = partition.lowerLeft.getX()
                + rand.nextInt((partition.upperRight.getX() - partition.lowerLeft.getX()) / 2);
        int roomLowerLeftY = partition.lowerLeft.getY()
                + rand.nextInt((partition.upperRight.getY() - partition.lowerLeft.getY()) / 2);
        Position roomLowerLeft = new Position(roomLowerLeftX, roomLowerLeftY);
        int roomUpperRightX = roomLowerLeft.getX()
                + rand.nextInt(partition.upperRight.getX() - roomLowerLeft.getX());
        int roomUpperRightY = roomLowerLeft.getY()
                + rand.nextInt(partition.upperRight.getY() - roomLowerLeft.getY());
        Position roomUpperRight = new Position(roomUpperRightX, roomUpperRightY);

        int roomWidth = roomUpperRight.getX() - roomLowerLeft.getX();
        int roomHeight = roomUpperRight.getY() - roomLowerLeft.getY();
        if (roomWidth < 3 || roomHeight < 3) {
            if (counter < 10) {
                return sampleRoomFromPartition(partition, true, counter + 1, rand);
            } else {
                // resulting in slender rooms
                return new Room(partition.lowerLeft, new Position(partition.upperRight.getX() - 1,
                 partition.upperRight.getY() - 1));
            }
        }
        return new Room(roomLowerLeft, roomUpperRight);
    }

    /** Change tile patterns located inside given room on the 2D tile array that represents the
     * world. Return the altered 2D tile array. */
    static TETile[][] drawRoom(TETile[][] tiles, Room room, TETile wallPattern,
                                 TETile floorPattern) {
        Position lowerLeft = room.lowerLeft;
        Position upperRight = room.upperRight;
        for (int i = lowerLeft.getX(); i <= upperRight.getX(); i += 1) {
            for (int j = lowerLeft.getY(); j <= upperRight.getY(); j += 1) {
                if (i == lowerLeft.getX() || i == upperRight.getX()
                        || j == lowerLeft.getY()|| j == upperRight.getY()) {
                    tiles[i][j] = wallPattern;
                } else {
                    tiles[i][j] = floorPattern;
                }
            }
        }
        return tiles;
    }

    /** Change tile patterns along given sequence of positions on tiles. */
    static TETile[][] drawSequence(TETile[][] tiles, Set<Position> sequence
            , TETile hallwayPattern) {
        for (Position pos : sequence) {
            tiles[pos.getX()][pos.getY()] = hallwayPattern;
        }
        return tiles;
    }

    /** Return index (of Rooms) that represents a Room that is closest to and
     * not connected to source room (srcRoomIdx). */
    static int getApproxAdjacUnconnectedRoom(WQUDisjointSet ds, ArrayList<Room> rooms,
                                          int srcRoomIdx) {
        Position srcRoomCenterCoor = rooms.get(srcRoomIdx).center;
        int tgtRoomIdx = 0;
        double minDist = Double.POSITIVE_INFINITY;
        for (int r = 0; r < rooms.size(); r += 1) {
            if (r != srcRoomIdx && !ds.isConnected(srcRoomIdx, r)) {
                Room tgtRoom = rooms.get(r);
                double candidateDist = Position.dist(srcRoomCenterCoor, tgtRoom.center);
                tgtRoomIdx = (candidateDist < minDist) ? r : tgtRoomIdx;
                minDist = Math.min(candidateDist, minDist);
            }
        }
        return tgtRoomIdx;
    }

    /** Check whether the given position is located on or inside the room. */
    boolean isPosWithinRoom(Position pos) {
        return pos.getX() >= lowerLeft.getX() && pos.getY() >= lowerLeft.getY()
                && pos.getX() <= upperRight.getX() && pos.getY() <= upperRight.getY();
    }
}
