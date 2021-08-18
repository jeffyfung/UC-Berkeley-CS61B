package byow.Core;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class Room {
    Position topLeftCorner;
    Position bottomLeftCorner;
    Position bottomRightCorner;
    Position topRightCorner;
    Position center;

    public Room(){}

    public static LinkedList<Room> buildRooms(WorldMap map){
        // generate a random sequence of Position objects, limited by range of world map
        List<Position> posSeq = generateRandomPos(WorldMap.RANDOM, WorldMap.worldWidth
                , WorldMap.worldHeight);
        // insert elements from sequence into kdTree
        KdTree kdt = new KdTree();
        for (Position pos : posSeq) {
            kdt = KdTree.insert(kdt, pos);
            if (kdt.size == Math.pow(2, KdTree.TREE_DEPTH)) {
                break;
            }
//            if (tree != null) {
//                kdt = tree;
//            }
        }
        // traverse the kdTree to delineate corners of each room and calculate centers
        LinkedList<Room> rooms = new LinkedList<>();
        for (Room partition : getPartitions(kdt)) {
            Room room = sampleRoomFromPartition(partition);
            drawRoom(map, room);
            rooms.add(room);
        }
        return rooms;
    }

    private static LinkedList<Position> generateRandomPos(Random rand, int width, int height) {
        LinkedList<Integer> xList = new LinkedList<>();
        LinkedList<Integer> yList = new LinkedList<>();
        for (int i = 0; i < width; i++) {
            xList.add(i);
        }
        for (int i = 0; i < height; i++) {
            yList.add(i);
        }
        Collections.shuffle(xList);
        Collections.shuffle(yList);
        LinkedList<Position> out = new LinkedList<>();
        for (int i = 0; i < Math.min(width, height); i++) {
            out.add(new Position(xList.remove(), yList.remove()));
        }
        return out;
    }

    private static LinkedList<Room> getPartitions(KdTree kdt) {
        return kdt.scanLeavesByDFS();
    }

    private static Room sampleRoomFromPartition(Room partition) {
        return null;
    }

    private static void drawRoom(WorldMap map, Room room) {
        return; // TODO: add more
    }
}
