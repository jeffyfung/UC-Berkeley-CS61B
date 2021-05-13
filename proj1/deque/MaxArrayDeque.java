package deque;

import java.util.Comparator;

public class MaxArrayDeque<T> extends ArrayDeque<T> {
    private Comparator<T> comp;

    public MaxArrayDeque(Comparator<T> c) {
        // super();
        comp = c;
    }

    public T max() {

        if (isEmpty()) {
            return null;
        }

        int max_idx = 0;
        for (int i = 0; i < size; i++) {
            int cmp = comp.compare(get(i), get(max_idx));
            if (cmp > 0) {
                max_idx = i;
            }
        }
        return get(max_idx);
    }

    public T max(Comparator<T> c) {

        if (isEmpty()) {
            return null;
        }

        int max_idx = 0;
        for (int i = 0; i < size; i++) {
            int cmp = c.compare(get(i), get(max_idx));
            if (cmp > 0) {
                max_idx = i;
            }
        }
        return get(max_idx);
    }

    public Comparator<T> getComparator() {
        return comp;
    }
}
