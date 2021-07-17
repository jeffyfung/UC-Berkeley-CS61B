package hashmap;

import java.util.*;

/**
 *  A hash table-backed Map implementation. Provides amortized constant time
 *  access to elements via get(), remove(), and put() in the best case.
 *
 *  Assumes null keys will never be inserted, and does not resize down upon remove().
 *  @author YOUR NAME HERE
 */
public class MyHashMap<K, V> implements Map61B<K, V> {

    /**
     * Protected helper class to store key/value pairs
     * The protected qualifier allows subclass access
     */
    protected class Node {
        K key;
        V value;

        Node(K k, V v) {
            key = k;
            value = v;
        }
    }

    /* Instance Variables */
    private Collection<Node>[] buckets;
    private int bucketNum = 16;
    private double maxLoad = 0.75;
    private Set<K> keySet = new HashSet<>();
    // You should probably define some more!

    /**
     * Constructors
     */
    public MyHashMap() {
        this.buckets = createTable(bucketNum);
    }

    public MyHashMap(int initialSize) {
        this.bucketNum = initialSize;
        this.buckets = createTable(initialSize);
    }

    /**
     * MyHashMap constructor that creates a backing array of initialSize.
     * The load factor (# items / # buckets) should always be <= loadFactor
     *
     * @param initialSize initial size of backing array
     * @param maxLoad     maximum load factor
     */
    public MyHashMap(int initialSize, double maxLoad) {
        this.maxLoad = maxLoad;
        this.bucketNum = initialSize;
        this.buckets = createTable(initialSize);
    }

    /**
     * Returns a new node to be placed in a hash table bucket
     */
    private Node createNode(K key, V value) {
        return new Node(key, value);
    }

    /**
     * Returns a data structure to be a hash table bucket
     * <p>
     * The only requirements of a hash table bucket are that we can:
     * 1. Insert items (`add` method)
     * 2. Remove items (`remove` method)
     * 3. Iterate through items (`iterator` method)
     * <p>
     * Each of these methods is supported by java.util.Collection,
     * Most data structures in Java inherit from Collection, so we
     * can use almost any data structure as our buckets.
     * <p>
     * Override this method to use different data structures as
     * the underlying bucket type
     * <p>
     * BE SURE TO CALL THIS FACTORY METHOD INSTEAD OF CREATING YOUR
     * OWN BUCKET DATA STRUCTURES WITH THE NEW OPERATOR!
     */
    protected Collection<Node> createBucket() {
        return new HashSet<>();
    }

    /**
     * Returns a table to back our hash table. As per the comment
     * above, this table can be an array of Collection objects
     * <p>
     * BE SURE TO CALL THIS FACTORY METHOD WHEN CREATING A TABLE SO
     * THAT ALL BUCKET TYPES ARE OF JAVA.UTIL.COLLECTION
     *
     * @param tableSize the size of the table to create
     */
    private Collection<Node>[] createTable(int tableSize) {
        return new Collection[tableSize];
    }

    // TODO: Implement the methods of the Map61B Interface below
    // Your code won't compile until you do so!

    @Override
    public void clear() {
        buckets = createTable(bucketNum);
        keySet.clear();
    }

    @Override
    public boolean containsKey(K key) {
        return keySet.contains(key);
    }

    @Override
    public V get(K key) {
        if (keySet.contains(key)) {
            int idx = Math.floorMod(key.hashCode(), (int) Math.ceil(bucketNum));
            for (Node i : buckets[idx]) {
                if (i.key.equals(key)) {
                    return i.value;
                }
            }
        }
        return null;
    }

    @Override
    public int size(){
        return keySet.size();
    }

    @Override
    public void put(K key, V value){
        int idx = Math.floorMod(key.hashCode(), (int) Math.ceil(bucketNum));
        if (buckets[idx] == null) {
            buckets[idx] = createBucket();
        } else {
            for (Node n : buckets[idx]) {
                if (n.key.equals(key)) {
                    n.value = value;
                    return;
                }
            }
        }
        buckets[idx].add(new Node(key, value));
        keySet.add(key);
        if (keySet.size() * 1.0/ bucketNum > maxLoad){
            resize(2);
        }
    }

    private void resize(int factor){
        bucketNum *= factor;
        Collection<Node>[] tmp = createTable(bucketNum);
        for (Collection<Node> bucket : buckets){
            if (bucket == null){
                continue;
            }
            for (Node n : bucket){
                int idx = Math.floorMod(n.key.hashCode(), (int) Math.ceil(bucketNum));
                if (tmp[idx] == null) {
                    tmp[idx] = createBucket();
                }
                tmp[idx].add(new Node(n.key, n.value));
            }
        }
        buckets = tmp;
    }

    @Override
    public Set<K> keySet(){
        return keySet;
    }

    @Override
    public Iterator<K> iterator(){
        return new MyHashMapIterator();
    }

    @Override
    public V remove(K key){
        throw unsupportedOperationError("command: keySet not supported");
    }

    @Override
    public V remove(K key, V value) {
        throw unsupportedOperationError("command: keySet not supported");
    }

    static UnsupportedOperationException unsupportedOperationError(String msg, Object... args){
        return new UnsupportedOperationException(String.format(msg, args));
    }

    private class MyHashMapIterator implements Iterator<K>{
        Iterator<K> refIterator;

        MyHashMapIterator(){
            refIterator = keySet.iterator();
        }

        @Override
        public boolean hasNext(){
            return refIterator.hasNext();
        }

        @Override
        public K next(){
            return refIterator.next();
        }
    }

}
