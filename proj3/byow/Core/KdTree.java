package byow.Core;

import static byow.Core.Position.dist;

public class KdTree {
    /** Depth of the kdTree, excluding the root layer **/
    static final int TREE_DEPTH = 2;
    Position pos;
    KdTree upper;
    KdTree lower;
    int axis;
    int size;

    KdTree() {}

    KdTree(Position pos, int axis) {
        this.pos = pos;
        this.axis = axis;
    }

    static KdTree insert(KdTree kdt, Position pos) {
        return insertHelper(kdt, pos, null, 0, TREE_DEPTH).kdt;
    }

    private static InsertHelperObj insertHelper(KdTree kdt, Position pos, Position parentPos,
                                              int axis,
                                 int treeDepth) {
        if (kdt == null) {
            Double dist_ = dist(pos, parentPos);
            if (dist_ != null && dist_ < Math.sqrt(18)) {
                return new InsertHelperObj(null, 0);
            }
            return new InsertHelperObj(new KdTree(pos, axis), 1);
        }
        // skip if treeDepth limit is reached or a node is too close to its parent
        if (treeDepth <= 0 ) {
            return new InsertHelperObj(null, 0);
        }
        InsertHelperObj insertObj;
        if (compare(pos, kdt.pos, kdt.axis)) {
            insertObj = insertHelper(kdt.upper, pos, kdt.pos, 1 - axis
                    , treeDepth - 1);
            kdt.upper = insertObj.kdt;
        }
        else {
            insertObj = insertHelper(kdt.lower, pos, kdt.pos, 1 - axis
                    , treeDepth - 1);
            kdt.lower = insertObj.kdt;
        }
        kdt.size += insertObj.inserted;
        return new InsertHelperObj(kdt, insertObj.inserted);
    }

    private static Boolean compare(Position cmp, Position pos, int axis) {
        // axis: 0 -> x; 1 -> y
        if (axis == 0) {
            return cmp.x >= pos.x;
        }
        else {
            return cmp.y >= pos.y;
        }
    }

    private static class InsertHelperObj {
        private KdTree kdt;
        private int inserted;

        public InsertHelperObj(KdTree kdt, int inserted) {}
    }
}