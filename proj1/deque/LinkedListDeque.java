package deque;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class LinkedListDeque<T> implements Iterable<T>, Deque<T> {


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

    @Override
    public void addFirst(T item) {
        sentinel.next.prev = new IntNode(item, sentinel.next, sentinel);
        sentinel.next = sentinel.next.prev;
        size += 1;
    }

    @Override
    public void addLast(T item) {
        // new item's next point to sentinel
        // sentinel's prev point to new item
        // new item's prev point to original last item
        IntNode temp_node = sentinel.prev;
        sentinel.prev = new IntNode(item, sentinel, temp_node);
        temp_node.next = sentinel.prev;
        size += 1;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void printDeque() {
        List<String> tmp_list = new ArrayList<>();
        for (T x : this) {
            tmp_list.add(x.toString());
        }
        String.join(", ", tmp_list);
    }

    @Override
    public T removeFirst() {
        IntNode temp = sentinel.next;
        if (temp.item != null) {
            sentinel.next = temp.next;
            sentinel.next.prev = sentinel;
            size -= 1;
            return temp.item;
        } else {
            return null;
        }
    }

    @Override
    public T removeLast() {
        IntNode temp = sentinel.prev;
        if (temp.item != null) {
            sentinel.prev = temp.prev;
            sentinel.prev.next = sentinel;
            size -= 1;
            return temp.item;
        } else {
            return null;
        }
    }

    @Override
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
        } else {
            return recurHelper(index - 1, node.next);
        }
    }

    public Iterator<T> iterator() {
        return new DequeIterator();
    }

    private class DequeIterator implements Iterator<T> {
        private IntNode node_pos;

        public DequeIterator() {
            node_pos = sentinel.next;
        }

        public boolean hasNext() {
            return node_pos.item != null;
        }

        public T next() {
            T tmp = (T) node_pos.item;
            node_pos = node_pos.next;
            return tmp;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o.getClass() != this.getClass()) {
            return false;
        }
        if (o == null) {
            return false;
        }
        LinkedListDeque<T> tmp_o = (LinkedListDeque<T>) o;
        if (size() != tmp_o.size()) {
            return false;
        }
        for (int i = 0; i < size(); i++) {
            if (!this.get(i).equals(tmp_o.get(i))) {
                return false;
            }
        }
        return true;
    }
}
