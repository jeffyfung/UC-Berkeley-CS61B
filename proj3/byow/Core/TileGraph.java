package byow.Core;

import byow.TileEngine.TETile;
import edu.princeton.cs.algs4.Edge;
import edu.princeton.cs.algs4.EdgeWeightedGraph;

import java.util.*;

public class TileGraph extends EdgeWeightedGraph {
    private int mapWidth;
    private ArrayList<Room> rooms;
    private ArrayList<HashSet<Integer>> roomsVertices;
    private boolean[] vInstantiated;
    private HashSet<Integer> existingHallways = new HashSet<>();

    TileGraph(TETile[][] tiles, ArrayList<Room> rooms) {
        super(tiles[0].length * tiles.length);
        this.mapWidth = tiles[0].length;
        this.rooms = rooms;
        this.roomsVertices = new ArrayList<>();
        this.vInstantiated = new boolean[V()];

        addEdges(0, 1, V()); // TODO: confirm weight = 1?
        for (int i = 0; i < rooms.size(); i += 1) {
            Room rm = rooms.get(i);
            Position lowerLeft = rm.lowerLeft;
            Position upperRight = rm.upperRight;
            HashSet<Integer> rv = new HashSet<>();
            for (int p = lowerLeft.getY(); p <= upperRight.getY(); p += 1) {
                int v1 = convertArrayPosToV(lowerLeft.getX(), p);
                int v2 = convertArrayPosToV(upperRight.getX(), p);
                rv.add(v1);
                rv.add(v2);
            }
            for (int q = lowerLeft.getX() + 1; q < upperRight.getX(); q += 1) {
                int v3 = convertArrayPosToV(q, lowerLeft.getY());
                int v4 = convertArrayPosToV(q, upperRight.getY());
                rv.add(v3);
                rv.add(v4);
            }
            roomsVertices.add(rv);
        }
        System.out.println("TileGraph initiated");
    }

    /**
     * Dijkstra / A*
     */
    public Hallway connect(int srcRoomIdx, int tgtRoomIdx) {
        Room srcRoom = rooms.get(srcRoomIdx);
        int srcV = convertArrayPosToV(srcRoom.center.getX(), srcRoom.center.getY());
        Room tgtRoom = rooms.get(tgtRoomIdx);
        int tgtV = convertArrayPosToV(tgtRoom.center.getX(), tgtRoom.center.getY());
        System.out.println("srcRoomCenter: " + srcRoom.center.getX() + " , " + srcRoom.center.getY());
        System.out.println("tgtRoomCenter: " + tgtRoom.center.getX() + " , " + tgtRoom.center.getY());
        System.out.println("srcV: " + srcV);
        System.out.println("tgtV: " + tgtV);

        ArrayList<HashSet<Integer>> inaccessibleAreas = new ArrayList<>();
        inaccessibleAreas.addAll(roomsVertices);
        HashSet<Integer> srcRoomVertices = inaccessibleAreas.remove(srcRoomIdx);
        if (srcRoomIdx < tgtRoomIdx) {
            tgtRoomIdx -= 1;
        }
        HashSet<Integer> tgtRoomVertices = inaccessibleAreas.remove(tgtRoomIdx);

        DijkstraUndirMaskedSP dusp = new DijkstraUndirMaskedSP(this, srcV, inaccessibleAreas,
                existingHallways);
        ArrayList<Integer> path = new ArrayList<>();
        for (Integer v : dusp.pathTo(tgtV)) {
            path.add(v);
        }
        if (path.size() == 0) {
            return null;
        }
        // TODO: need to account for twisting path
        return buildHallway(path, srcRoomVertices, tgtRoomVertices, srcRoom, tgtRoom);
    }

    /**
     * Recursively add undirected weighted edges to TileGraph from bottom left to top right.
     */
    public void addEdges(int v, int weight, int totalTiles) {
        if (vInstantiated[v]) {
            return;
        }
        if ((v + 1) / mapWidth == v / mapWidth) {
            addEdge(new Edge(v, v + 1, weight));
            addEdges(v + 1, weight, totalTiles);
        }
        if (v + mapWidth < totalTiles) {
            addEdge(new Edge(v, v + mapWidth, weight));
            addEdges(v + mapWidth, weight, totalTiles);
        }
        vInstantiated[v] = true;
    }

    static public Set<Integer> getVPeriphery(TileGraph g, int v) {
        return Set.of(v, v - 1, v + 1, v - g.mapWidth, v + g.mapWidth);
    }

    private Hallway buildHallway(List<Integer> path, Set<Integer> srcRoomVertices,
                                        Set<Integer> tgtRoomVertices, Room srcRoom, Room tgtRoom) {
        int[] indices = truncatePath(path, srcRoomVertices, tgtRoomVertices);
        int startVIdx = indices[0];
        int lastVIdx = indices[1];

        List<Position> truncatedPath = new LinkedList<>();
        List<Position> walls = new LinkedList<>();
        int translator = getTranslator(srcRoom.center, tgtRoom.center);
        for (int j = startVIdx; j <= lastVIdx; j += 1) {
            int v = path.get(j);
            if (j != startVIdx && j != lastVIdx) {
                // exclude vertices at the ends of path from inaccessibleAreas
                existingHallways.add(v);
            }
            Position pos = convertVToArrayPos(v);
            truncatedPath.add(pos);

            Position rTranslatedPos;
            Position lTranslatedPos;
            if (translator == 2) {
                rTranslatedPos = new Position(pos.getX(), pos.getY() + 1);
                lTranslatedPos = new Position(pos.getX(), pos.getY() - 1);
            } else {
                rTranslatedPos = new Position(pos.getX() + 1, pos.getY() + translator);
                lTranslatedPos = new Position(pos.getX() - 1, pos.getY() - translator);
            }

            if (!srcRoom.isPosWithinRoom(rTranslatedPos) && !tgtRoom.isPosWithinRoom(rTranslatedPos)) {
                walls.add(rTranslatedPos);
                existingHallways.add(convertArrayPosToV(rTranslatedPos));
            }
            if (!srcRoom.isPosWithinRoom(lTranslatedPos) && !tgtRoom.isPosWithinRoom(lTranslatedPos)) {
                walls.add(lTranslatedPos);
                existingHallways.add(convertArrayPosToV(lTranslatedPos));
            }
        }
        return new Hallway(truncatedPath, walls);
    }

    private int[] truncatePath(List<Integer> path, Set<Integer> srcRoomVertices,
                               Set<Integer> tgtRoomVertices) {
        int startVIdx = 0;
        int lastVIdx = 0;
        for (int i = 0; i < path.size() - 1; i += 1) {
            int v = path.get(i);
            int nextV = path.get(i + 1);
            if (srcRoomVertices.contains(v) && !srcRoomVertices.contains(nextV)) {
                if (isCornerVertex(v, srcRoomVertices)) {
                    startVIdx = i - 1;
                } else {
                    startVIdx = i;
                }
            }
            if (tgtRoomVertices.contains(nextV) && !tgtRoomVertices.contains(v)) {
                if (isCornerVertex(nextV, tgtRoomVertices)) {
                    lastVIdx = i + 2;
                } else {
                    lastVIdx = i + 1;
                }
                break;
            }
        }
        return new int[]{startVIdx, lastVIdx};
    }

    private boolean isCornerVertex(int v, Set<Integer> roomVertices) {
        if (!roomVertices.contains(v)) {
            throw new NoSuchElementException(String.format("vertex %d is not located on " +
                    "boundaries of the room", v));
        }
        return (!(roomVertices.contains(v - 1) && roomVertices.contains(v + 1))
                && !(roomVertices.contains(v + mapWidth) && roomVertices.contains(v - mapWidth)));
    }

    /** Determine the relative locations of pos1 and pos2. Return 1 if the higher position is
     * located more to the left, -1 if the higher position is located more to the right. Return 0
     * if the positions are collinear. */
    private int getTranslator(Position pos1, Position pos2) {
        int pos1x = pos1.getX();
        int pos1y = pos1.getY();
        int pos2x = pos2.getX();
        int pos2y = pos2.getY();
        if (pos1y == pos2y) {
            return 2;
        } else if (pos1x == pos2x) {
            return 0;
        } else {
            return ((pos1y > pos2y && pos1x < pos2x) || (pos2y > pos1y && pos2x < pos1x)) ? 1 : -1;
        }
    }

    private int convertArrayPosToV(int x, int y) {
        return x + mapWidth * y;
    }

    private int convertArrayPosToV(Position pos) {
        return pos.getX() + mapWidth * pos.getY();
    }

    private Position convertVToArrayPos(int v) {
        return new Position(v % mapWidth, v / mapWidth);
    }
}
