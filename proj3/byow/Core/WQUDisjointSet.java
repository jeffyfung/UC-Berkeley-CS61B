package byow.Core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.NoSuchElementException;

public class WQUDisjointSet {
    /** Track parents of elements in disjoint set. */
    private int[] parentArray;
    /** Actual elements in disjoint set. */
    private Object[] objectArray;
    /** Size of disjoint set. */
    private int size;

    WQUDisjointSet(ArrayList<?> objects) {
        this.objectArray = objects.toArray();
        this.size = this.objectArray.length;
        this.parentArray = new int[size];
        Arrays.fill(this.parentArray, -1);
    }

    WQUDisjointSet(Object[] objects) {
        this.objectArray = objects;
        this.parentArray = new int[this.objectArray.length];
        Arrays.fill(this.parentArray, -1);
    }

    /** Return true if objects located at objIdx1 and objIdx2 of objectsArray are connected, i.e.
     * part of the same set, and vice versa. */
    boolean isConnected(int objIdx1, int objIdx2) {
        return this.parent(objIdx1) == this.parent(objIdx2);
    }

    /** Connect objects located at objIdx1 and objIdx2 by pointing the parent of the smaller
     * sized set among the 2 sets to that of the larger sized set. Point the parent of second set
     * to that of first set if both sets have the same size. */
    void connect(int objIdx1, int objIdx2) {
        int obj1ParentIdx = parent(objIdx1);
        int obj2ParentIdx = parent(objIdx2);
        if (parentArray[obj1ParentIdx] <= parentArray[obj2ParentIdx]) {
            parentArray[obj1ParentIdx] += parentArray[obj2ParentIdx];
            parentArray[obj2ParentIdx] = obj1ParentIdx;
        } else {
            parentArray[obj2ParentIdx] += parentArray[obj1ParentIdx];
            parentArray[obj1ParentIdx] = obj2ParentIdx;
        }
    }

    /** Return index of the least-connected element. */
    int getLoneliestElement() {
        int out = 0;
        for (int i = 1; i < this.size(); i += 1) {
            if (parentArray[out] >= 0 || (parentArray[i] < 0
                    && parentArray[i] > parentArray[out])) {
                out = i;
            }
        }
        return out;
    }

    /** Return index of the least-connected element that is equally or more connected than
     * given prevIdx. */
    int getNextLoneliestElement(int prevIdx) {
        int out = -1;
        for (int i = 0; i < this.size(); i += 1) {
            if (parentArray[i] <= parentArray[prevIdx] && i != prevIdx) {
                if (out == -1 || parentArray[i] > parentArray[out]) {
                    out = i;
                }
            }
        }
        if (out == -1) {
            throw new NoSuchElementException("The rooms cannot be fully connected");
        }
        return out;
    }

    /** Return the next sibling, i.e. next element in array sharing the parent as objIdx. If
     * exhausted, return objIdx's first child, as determined by the order of elements in array. */
    Integer nextOfKin(int objIdx) {
        // find next sibling
        for (int idx = objIdx; idx < objectArray.length; idx += 1) {
            if (parentArray[objIdx] == parentArray[idx] && objIdx != idx) {
                return idx;
            }
        }
        // if exhausted, find first child
        for (int idx = 0; idx < objectArray.length; idx += 1) {
            if (objIdx == parentArray[idx]) {
                return idx;
            }
        }
        return null;
    }


    /** Return index of root parent of the object located at objIdx. */
    int parent(int objIdx) {
        if (objIdx >= this.parentArray.length) {
            throw new IndexOutOfBoundsException();
        }
        int outputIdx = parentArray[objIdx];
        if (outputIdx >= 0) {
            return parent(outputIdx);
        } else {
            return objIdx;
        }
    }

    /** Check if the object at given idx position is connected to all other objects in the set.
     * If true, it is implied that the disjoint set has been connected. */
    boolean connectedToAllObjects(int idx) {
        return parentArray[parent(idx)] == -1 * size();
    }

    /** Return number of objects in the disjoint set. */
    int size() {
        return this.size;
    }

    /** Return objects in the disjoint set. */
    Object[] getObjects() {
        return this.objectArray;
    }

    /** Return object at given idx of objectArray. */
    Object getElement(int idx) {
        return this.objectArray[idx];
    }
}
