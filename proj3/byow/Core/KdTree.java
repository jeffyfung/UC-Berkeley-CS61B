package byow.Core;

import java.util.LinkedList;

import static byow.Core.Position.dist;

/** A KdTree structure that partitions a 2D space composed of Position nodes with alternating
 * dominant axes. */
public class KdTree {
    /** Max tree layers of a kdTree, including the root node. E.g. MAX_TREE_LAYERS = 3 for a
     * KdTree with max 7 nodes. */
    static final int MAX_TREE_LAYERS = 5;
    /** Space partitioned by extensions of empty leaf nodes. See private class
     * EmptyLeafExtensionSpace for details. */
    static LinkedList<EmptyLeafExtensionSpace> spacePartitions = new LinkedList<>();
    /** Position (coordinates) rooted at the KdTree. */
    Position pos;
    /** "Upper" branch of the KdTree. */
    KdTree upper;
    /** "Lower" branch of the KdTree. */
    KdTree lower;
    /** Dominant axis that depends on depth of the current node which is used to partition the 2D
     * space. Axis = 0 and axis = 1 refers to x-axis and y-axis respectively. Dominant axis at root
     * node is 0. */
    int axis;
    /** Size of the kdTree rooted at the Position. */
    int size;

    KdTree(Position pos, int axis) {
        this.pos = pos;
        this.axis = axis;
        this.size = 1;
    }

    /** Insert the given Position into the given KdTree. Always insert at a leaf node. Return the
     *  KdTree with the inserted element. Return null if the insertion fails. An Position
     *  will not be inserted if the given Position is too close (<sqrt(18)) to its parent node, or
     *  if inserting the Position will lead to a KdTree that exceeds TREE_DEPTH. */
    static KdTree insert(KdTree kdt, Position pos) {
        return insertHelper(kdt, pos, null, 0, MAX_TREE_LAYERS).kdt;
    }

    /** Return a list of EmptyLeafExtensionSpace, which delineates non-overlapping spaces in the
     * world partitioned by the KdTree. */
    LinkedList<EmptyLeafExtensionSpace> getPartitionedSpace() {
        EmptyLeafExtensionSpace space = new EmptyLeafExtensionSpace();
        // pre-order traversal of KdTree
        preOrderTraversal(this, space);
        return spacePartitions;
    }

    /** Reset static variable spacePartitions to empty.*/
    static void resetPartitionedSpace() {
        spacePartitions = new LinkedList<>();
    }

    /** Visit empty children of each leaf node in pre-order manner. For each visit, adds
     * partitioned space delineated by the parent nodes of the corresponding empty children of
     * leaf nodes. */
    static void preOrderTraversal(KdTree kdt, EmptyLeafExtensionSpace space) {
        if (kdt == null) {
            spacePartitions.add(space);
            return;
        }
        preOrderTraversal(kdt.upper, space.changeCoor(kdt.pos, kdt.axis,true));
        preOrderTraversal(kdt.lower, space.changeCoor(kdt.pos, kdt.axis,false));
    }

    /** Recursive helper function to insert to a KdTree. */
    private static InsertHelperObj insertHelper(KdTree kdt, Position pos, Position parentPos,
                                              int axis, int treeLayersAllowed) {
        // skip if treeDepth limit is reached or a node is too close to its parent
        if (treeLayersAllowed <= 0 ) {
            return new InsertHelperObj(null, 0);
        }
        if (kdt == null) {
            Double dist_ = dist(pos, parentPos);
            if (dist_ != null && dist_ < Math.sqrt(18)) {
                return new InsertHelperObj(null, 0);
            }
            return new InsertHelperObj(new KdTree(pos, axis), 1);
        }
        InsertHelperObj insertObj;
        if (Position.compare(pos, kdt.pos, kdt.axis)) {
            insertObj = insertHelper(kdt.upper, pos, kdt.pos, 1 - axis
                    , treeLayersAllowed - 1);
            kdt.upper = insertObj.kdt;
        } else {
            insertObj = insertHelper(kdt.lower, pos, kdt.pos, 1 - axis
                    , treeLayersAllowed - 1);
            kdt.lower = insertObj.kdt;
        }
        kdt.size += insertObj.inserted;
        return new InsertHelperObj(kdt, insertObj.inserted);
    }

    /** Helper class to insert function. */
    private static class InsertHelperObj {
        /** A KdTree. */
        private KdTree kdt;
        /** 1 if a Position is successfully inserted to KdTree. 0 if failed. */
        private int inserted;

        public InsertHelperObj(KdTree kdt, int inserted) {
            this.kdt = kdt;
            this.inserted = inserted;
        }
    }

    /** Private class that represents an empty pace partitioned by leaf nodes of KdTree.
     *  E.g. for a perfectly bushy KdTree with 7 nodes in which 4 of them are leaf nodes, there
     *  are 4*2 such empty extension space, 2 extending from every leaf node. */
    class EmptyLeafExtensionSpace {
        Position lowerLeft;
        Position upperRight;

        EmptyLeafExtensionSpace() {
            this.lowerLeft = new Position(0, 0);
            this.upperRight = new Position(Engine.WORLD_WIDTH - 1, Engine.WORLD_HEIGHT - 1);
        }

        EmptyLeafExtensionSpace(Position lowerLeft, Position upperRight) {
            this.lowerLeft = lowerLeft;
            this.upperRight = upperRight;
        }

        /** Return a new EmptyLeafExtensionSpace bounded by new coordinates of corners as specified
         *  by axis and greatThanOrEqual. */
        EmptyLeafExtensionSpace changeCoor(Position pos, int axis, Boolean greaterThanOrEqual) {
            if (greaterThanOrEqual) {
                Position newLowerLeft = new Position(lowerLeft.xy[0], lowerLeft.xy[1]);
                newLowerLeft.xy[axis] = pos.xy[axis];
                return new EmptyLeafExtensionSpace(newLowerLeft, upperRight);
            } else {
                Position newUpperRight = new Position(upperRight.xy[0], upperRight.xy[1]);
                newUpperRight.xy[axis] = pos.xy[axis];
                // clarify : no need to -1; just exclude upper bound when sampling from space
                return new EmptyLeafExtensionSpace(lowerLeft, newUpperRight);
            }
        }
    }
}