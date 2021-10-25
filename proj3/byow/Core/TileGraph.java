package byow.Core;

import edu.princeton.cs.algs4.Edge;
import edu.princeton.cs.algs4.EdgeWeightedGraph;

import java.util.*;

public class TileGraph extends EdgeWeightedGraph {
    /** List of Room objects. */
    private ArrayList<Room> rooms;
    /** List of vertices on perimeters of each room. */
    private ArrayList<HashSet<Integer>> roomsVertices;
    /** Track if edges have been added to a vertex during initialization. */
    private boolean[] vInstantiated;
    /** Set of vertices locating on hallways, including paths and walls. */
    private HashSet<Integer> existingHallways = new HashSet<>();

    TileGraph(ArrayList<Room> rooms) {
        super(Engine.WORLD_WIDTH * Engine.WORLD_HEIGHT);
        this.rooms = rooms;
        this.roomsVertices = new ArrayList<>();
        this.vInstantiated = new boolean[V()];

        addEdges(0, 1, V());
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
    }

    /** Connect the a room (at srcRoomIdx) to another room (at tgtRoomIdx) by running a
     * modified Dijkstra's algorithm on the vertex representing center of the source room. Return
     * a hallway which consists of a 1 unit wide path and surrounding walls after processing the
     * path by truncating and building walls around it. */
    public Hallway connect(int srcRoomIdx, int tgtRoomIdx) {
        Room srcRoom = rooms.get(srcRoomIdx);
        int srcV = convertArrayPosToV(srcRoom.center);
        Room tgtRoom = rooms.get(tgtRoomIdx);
        int tgtV = convertArrayPosToV(tgtRoom.center);

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
        return buildHallway(path, srcRoomVertices, tgtRoomVertices, srcRoom, tgtRoom);
    }

    /** Recursively add undirected weighted edges to TileGraph from bottom left to top right.
     * Default weights are 1 for all edges. */
    public void addEdges(int v, int weight, int totalTiles) {
        if (vInstantiated[v]) {
            return;
        }
        if ((v + 1) / Engine.WORLD_WIDTH == v / Engine.WORLD_WIDTH) {
            addEdge(new Edge(v, v + 1, weight));
            addEdges(v + 1, weight, totalTiles);
        }
        if (v + Engine.WORLD_WIDTH < totalTiles) {
            addEdge(new Edge(v, v + Engine.WORLD_WIDTH, weight));
            addEdges(v + Engine.WORLD_WIDTH, weight, totalTiles);
        }
        vInstantiated[v] = true;
    }

    /** Return the given vertex and its immediately adjacent vertices in all directions all
     * together in a set. */
    static public Set<Integer> getVPeriphery(TileGraph g, int v) {
        return Set.of(v, v - 1, v + 1, v - Engine.WORLD_WIDTH, v + Engine.WORLD_WIDTH);
    }

    /** Process the given path of vertices, which connects source room to target room, in 2 steps:
     * 1) truncate the path such that the hallway stops at the boundary of the rooms;
     * 2) build walls along the path.
     * Return the resulted hallway. */
    private Hallway buildHallway(List<Integer> path, Set<Integer> srcRoomVertices,
                                        Set<Integer> tgtRoomVertices, Room srcRoom, Room tgtRoom) {

        Map<Integer, int[]> directions = new HashMap<>();
        directions.put(1, new int[] {Engine.WORLD_WIDTH , -Engine.WORLD_WIDTH});
        directions.put(Engine.WORLD_WIDTH, new int[] {-1 , 1});
        directions.put(-1, new int[] {-Engine.WORLD_WIDTH, Engine.WORLD_WIDTH});
        directions.put(-Engine.WORLD_WIDTH, new int[] {1, -1});

        int[] indices = truncatePath(path, srcRoomVertices, tgtRoomVertices);
        int startVIdx = indices[0];
        int lastVIdx = indices[1];
        Hallway h = new Hallway(new HashSet<>(), new HashSet<>());

        for (int j = startVIdx + 1; j <= lastVIdx + 1; j +=1 ) {
            int v = j == lastVIdx + 1 ? -1 : path.get(j);
            int prevV = path.get(j - 1);
            int prevPrevV = j == startVIdx + 1 ? -1 : path.get(j - 2);
            h = buildHallwayHelper(v, prevV, prevPrevV, directions, srcRoom, tgtRoom, h);
        }
        return h;
    }

    /** Return an array of indices (of size 2) that indicates the starting and ending indices of
     * the truncated path such that the path will stop at the boundaries of the rooms. */
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

    /** Check whether the given vertex is located on the corner of a room. Throws exception if
     * the vertex is not located on the boundary of the given room. */
    private boolean isCornerVertex(int v, Set<Integer> roomVertices) {
        if (!roomVertices.contains(v)) {
            throw new NoSuchElementException(String.format("vertex %d is not located on " +
                    "boundaries of the room", v));
        }
        return (!(roomVertices.contains(v - 1) && roomVertices.contains(v + 1))
                && !(roomVertices.contains(v + Engine.WORLD_WIDTH)
                && roomVertices.contains(v - Engine.WORLD_WIDTH)));
    }

    /** Return a hallway with 2 updated sequences of tiles representing a path from source room
     * to target room and walls along the path respectively. Any tile immediately or diagonally
     * adjacent to a tile on the path is either part of a path or a wall surrounding a path. */
    private Hallway buildHallwayHelper(int v, int prevV, int prevPrevV,
                                              Map<Integer, int[]> directions, Room srcRoom,
                                              Room tgtRoom, Hallway h) {
        Set<Position> path = h.getPath();
        Set<Position> walls = h.getWalls();
        List<Integer> wallsInScope = new LinkedList<>();

        int deltaV = v - prevV;
        int prevDeltaV = prevV - prevPrevV;
        if (v == -1) {
            wallsInScope.add(prevV + directions.get(prevDeltaV)[0]);
            wallsInScope.add(prevV + directions.get(prevDeltaV)[1]);
            wallsInScope.add(prevV + prevDeltaV + directions.get(prevDeltaV)[0]);
            wallsInScope.add(prevV + prevDeltaV + directions.get(prevDeltaV)[1]);
        } else if (prevPrevV == -1 || deltaV == prevDeltaV) {
            wallsInScope.add(prevV + directions.get(deltaV)[0]);
            wallsInScope.add(prevV + directions.get(deltaV)[1]);
            if (prevPrevV == -1) {
                wallsInScope.add(prevV - deltaV + directions.get(deltaV)[0]);
                wallsInScope.add(prevV - deltaV + directions.get(deltaV)[1]);
            }
        } else {
                wallsInScope.add(prevV - deltaV);
                wallsInScope.add(prevV + prevDeltaV);
                wallsInScope.add(prevV + prevDeltaV - deltaV);
        }
        for (int wall : wallsInScope) {
            Position wallPos = convertVToArrayPos(wall);
            if (!srcRoom.isPosWithinRoom(wallPos) && !tgtRoom.isPosWithinRoom(wallPos)) {
                walls.add(wallPos);
                existingHallways.add(wall);
            }
        }
        Position pathPos = convertVToArrayPos(prevV);
        if (!srcRoom.isPosWithinRoom(pathPos) && !tgtRoom.isPosWithinRoom(pathPos)) {
            existingHallways.add(prevV);
        }
        path.add(pathPos);
        return new Hallway(path, walls);
    }

    /** Check if the given vertex is located on the boundary of the graph. */
    boolean isVertexOnGraphBoundary(int v) {
        Position pos = convertVToArrayPos(v);
        return pos.getX() == 0 || pos.getX() == Engine.WORLD_WIDTH - 1
                || pos.getY() == 0 || pos.getY() == Engine.WORLD_HEIGHT - 1;
    }

    /** Convert a Position object to corresponding vertex. */
    private int convertArrayPosToV(int x, int y) {
        return x + Engine.WORLD_WIDTH * y;
    }

    /** Convert a Position object to corresponding vertex. */
    private int convertArrayPosToV(Position pos) {
        return pos.getX() + Engine.WORLD_WIDTH * pos.getY();
    }

    /** Convert a corresponding vertex to Position object. */
    private Position convertVToArrayPos(int v) {
        return new Position(v % Engine.WORLD_WIDTH, v / Engine.WORLD_WIDTH);
    }
}
