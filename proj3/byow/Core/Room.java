package byow.Core;

import byow.TileEngine.TETile;

import java.util.*;

import static byow.Core.Engine.*;

public class Room {
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
    public static ArrayList<Room> buildRooms(Engine engine) {
        // generate a random sequence of Position objects, limited by range of world map
        List<Position> posSeq = generateRandomPos(Engine.WORLD_WIDTH, Engine.WORLD_HEIGHT,
                engine.random);
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
        for (KdTree.EmptyLeafExtensionSpace partition : kdt.getPartitionedSpace()) {
            Room room = sampleRoomFromPartition(partition, false, 0
                    , engine.random);
            if (room != null) {
                drawRoom(engine, room, patternWall, patternRoomFloor);
                rooms.add(room);
            }
        }
        KdTree.resetPartitionedSpace();
        return rooms;
    }

    /** Connect all rooms by repeatedly finding the least connected room and connecting it to the
     *  approximately closest, second closest or third closest room until all rooms are connected.
     *  Skip a candidate pair of connection if a connection cannot be formed between the pair of
     *  rooms. Draw the resulted hallway during each successful connection. */
    public static void connectRooms(Engine engine, ArrayList<Room> rooms) { ;
        WQUDisjointSet roomsDS = new WQUDisjointSet(rooms);
        TileGraph g = new TileGraph(rooms);
        int srcRoomIdx = 0;
        while (!roomsDS.connectedToAllObjects(srcRoomIdx)) {
            // TODO: alternative method for calculating tgtRoom e.g. use Dijkstra's ? (inaccessible)
            // TODO: append every hallways for quick retrieval? (apart from existingHallways)
            int tgtRoomIdx = getApproxAdjacUnconnectedRoom(roomsDS, rooms, srcRoomIdx);
            Hallway h = g.connect(srcRoomIdx, tgtRoomIdx);
            if (h != null) {
                roomsDS.connect(srcRoomIdx, tgtRoomIdx);
                drawSequence(engine, h.getPath(), patternHallwayFloor);
                drawSequence(engine, h.getWalls(), patternWall);
                srcRoomIdx = roomsDS.getLoneliestElement();
            } else {
                Integer nok = roomsDS.nextOfKin(srcRoomIdx);
                srcRoomIdx = (nok == null)? roomsDS.getNextLoneliestElement(srcRoomIdx) : nok;
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
    static void drawRoom(Engine engine, Room room, TETile wallPattern,
                                 TETile floorPattern) {
        Position lowerLeft = room.lowerLeft;
        Position upperRight = room.upperRight;
        for (int i = lowerLeft.getX(); i <= upperRight.getX(); i += 1) {
            for (int j = lowerLeft.getY(); j <= upperRight.getY(); j += 1) {
                if (i == lowerLeft.getX() || i == upperRight.getX()
                        || j == lowerLeft.getY()|| j == upperRight.getY()) {
                    engine.changeTilePattern(i, j, wallPattern);
                } else {
                    engine.changeTilePattern(i, j, floorPattern);
                }
            }
        }
    }

    /** Change tile patterns along given sequence of positions on tiles. */
    static void drawSequence(Engine engine, Set<Position> sequence
            , TETile hallwayPattern) {
        for (Position pos : sequence) {
            engine.changeTilePattern(pos, hallwayPattern);
        }
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

    /** Return a list of indices of the n approximately closest rooms to the source room
     * (srcRoomIdx). */
    static List<Integer> getNApproxAdjacentUnconnectedRoom(int n, WQUDisjointSet ds,
                                                     ArrayList<Room> rooms, int srcRoomIdx) {
        Position srcRoomCenterCoor = rooms.get(srcRoomIdx).center;
        List<Integer> out = new LinkedList<>();
        for (int i = 0; i < n; i += 1) {
            int tgtRoomIdx = 0;
            double minDist = Double.POSITIVE_INFINITY;
            for (int r = 0; r < rooms.size(); r += 1) {
                if (r != srcRoomIdx && !out.contains(r) && !ds.isConnected(srcRoomIdx, r)) {
                    Room tgtRoom = rooms.get(r);
                    double candidateDist = Position.dist(srcRoomCenterCoor, tgtRoom.center);
                    tgtRoomIdx = (candidateDist < minDist) ? r : tgtRoomIdx;
                    minDist = Math.min(candidateDist, minDist);
                }
            }
            out.add(tgtRoomIdx);
        }
        return out;
    }

    /** Check whether the given position is located on or inside the room. */
    boolean isPosWithinRoom(Position pos) {
        return pos.getX() >= lowerLeft.getX() && pos.getY() >= lowerLeft.getY()
                && pos.getX() <= upperRight.getX() && pos.getY() <= upperRight.getY();
    }
}
