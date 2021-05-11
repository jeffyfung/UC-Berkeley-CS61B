package deque;

public class LinkedListDeque<T> {

    private class IntNode {
        public T item;
        public IntNode prev;
        public IntNode next;

        public IntNode(T i, IntNode n, IntNode p) {
            item = i;
            next = n;
            prev = p;
        }
    }

    private IntNode sentinel; // front and end
    private int size;

    public LinkedListDeque() {
        sentinel = new IntNode(null, null, null);
        sentinel.next = sentinel;
        sentinel.prev = sentinel;
        size = 0;
    }

    public void addFirst(T item) {
        sentinel.next.prev = new IntNode(item, sentinel.next, sentinel);
        sentinel.next = sentinel.next.prev;
        size += 1;
    }

    public void addLast(T item) {
        // new item's next point to sentinel
        // sentinel's prev point to new item
        // new item's prev point to original last item
        IntNode temp_node = sentinel.prev;
        sentinel.prev = new IntNode(item, sentinel, temp_node);
        temp_node.next = sentinel.prev;
        size += 1;
    }

    public boolean isEmpty() {
        return (sentinel.next.item == null);
    }

    public int size() {
        return size;
    }

    public void printDeque() {
        IntNode i = sentinel.next;
        while (i.item != null) {
            System.out.print(i.item + " ");
            i = i.next;
        }
        System.out.println();
    }

    public T removeFirst() {
        IntNode temp = sentinel.next;
        if (temp.item != null) {
            sentinel.next = temp.next;
            sentinel.next.prev = sentinel;
            size -= 1;
            return temp.item;
        }
        else {
            return null;
        }
    }

    public T removeLast() {
        IntNode temp = sentinel.prev;
        if (temp.item != null) {
            sentinel.prev = temp.prev;
            sentinel.prev.next = sentinel;
            size -= 1;
            return temp.item;
        }
        else {
            return null;
        }
    }

    public T get(int index) {
        IntNode temp_node = sentinel.next;
        while (index != 0) {
            if (temp_node.item == null) {
                return null;
            }
            index -= 1;
            temp_node = temp_node.next;
        }
        return temp_node.item;
    }

    public T getRecursive(int index) {
        return recurHelper(index, sentinel.next);
    }

    private T recurHelper(int index, IntNode node) {
        if (index == 0 || node.item == null) {
            return node.item;
        }
        else {
            return recurHelper(index - 1, node.next);
        }
    }

//    @Override
//    public boolean equals(Object o) {
//        // check if instanceof LinkedListDeque
//        if (o instanceof LinkedListDeque) {
//            LinkedListDeque<T> temp_deq = (LinkedListDeque<T>) o;
//            return equalDequeHelper(this.sentinel.next, temp_deq.sentinel.next);
//        }
//        else {
//            return false;
//        }
//    }


    private boolean equalDequeHelper(IntNode n1, IntNode n2) {
        if (n1.item == null && n2.item == null) {
            return true;
        }
        else if (n1.item == null || n2.item == null || !(n1.item.equals(n2.item))) {
            return false;
        }
        else {
            return equalDequeHelper(n1.next, n2.next);
        }
    }
}
