package deque;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ArrayDeque<T> implements Iterable<T>, Deque<T> {
    T[] items;
    int size;
    int nextFirst;
    int nextLast;
    int array_size;

    public ArrayDeque() {
        array_size = 8;
        items = (T[]) new Object[array_size];
        nextFirst = 0;
        nextLast = 1;
        size = 0;
    }

    @Override
    public void addFirst(T item) {
        items[nextFirst] = item;
        nextFirst = (nextFirst - 1 + array_size) % array_size;
        size += 1;
        // when only 1 space left
        if (nextFirst == nextLast) {
            resize(2);
        }
    }

    @Override
    public void addLast(T item) {
        items[nextLast] = item;
        nextLast = (nextLast + 1) % array_size;
        size += 1;
        // when only 1 space left
        if (nextFirst == nextLast) {
            resize(2);
        }
    }

    private void resize(int factor) {
        int last_seg_len = array_size - nextFirst;
        array_size *= factor;
        T[] temp_ad = (T[]) new Object[array_size];
        System.arraycopy(items, 0, temp_ad, 0, nextLast);
        System.arraycopy(items, nextFirst, temp_ad, array_size - last_seg_len, last_seg_len);
        items = temp_ad;
        nextFirst = array_size - last_seg_len;
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
        if (!isEmpty()) {
            if (array_size >= 16 && usage_factor() < 0.25) {
                downsize(2);
            }
            nextFirst = (nextFirst + 1) % array_size;
            T temp = items[nextFirst];
            items[nextFirst] = null;
            size -= 1;
            return temp;
        }
        return null;
    }

    @Override
    public T removeLast() {
        if (array_size >= 16 && usage_factor() < 0.25) {
            downsize(2);
        }
        if (!isEmpty()) {
            nextLast = (nextLast - 1 + array_size) % array_size;
            T temp = items[nextLast];
            items[nextLast] = null;
            size -= 1;
            return temp;
        }
        return null;
    }

    private float usage_factor() {
        return (float) (size - 1) / array_size;
    }

    private void downsize(int factor) {
        int temp_array_size = array_size;
        array_size /= factor;
        T[] temp_ad = (T[]) new Object[array_size];
        int src_idx = (nextFirst + 1) % temp_array_size;
        int des_idx = Math.max(0, src_idx - temp_array_size / factor);

        if (nextLast < nextFirst && (nextFirst + 1) != temp_array_size) {
            int l = array_size - des_idx;
            System.arraycopy(items, src_idx, temp_ad, des_idx, l);
            System.arraycopy(items, 0, temp_ad, 0, size - l);
        }
        else {
            System.arraycopy(items, src_idx, temp_ad, des_idx, size);
        }

        items = temp_ad;
        nextFirst = (des_idx - 1 + array_size) % array_size;
        nextLast = (nextFirst + size + 1) % array_size;
    }

    @Override
    public T get(int index) {
        if (!isEmpty() && index < size) {
            return items[(nextFirst + 1 + index) % array_size];
        }
        return null;
    }

    public Iterator<T> iterator() {
        return new ArrayDequeIterator();
    }

    private class ArrayDequeIterator implements Iterator<T> {
        private int pos;

        public ArrayDequeIterator() {
            pos = 0;
        }

        public boolean hasNext() {
            return pos < size;
        }

        public T next() {
            T item = ArrayDeque.this.get(pos);
            pos += 1;
            return item;
        }
    }

    @Override
    public boolean equals (Object o) {
        if (this == o) {
            return true;
        }
        if (o.getClass() != this.getClass()) {
            return false;
        }
        if (o == null) {
            return false;
        }
        ArrayDeque<T> tmp_o = (ArrayDeque<T>) o;
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
