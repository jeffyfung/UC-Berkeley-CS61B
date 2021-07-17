package bstmap;

import java.util.Iterator;
import java.util.Set;

public class BSTMap<K extends Comparable, V> implements Map61B<K, V> {

    BSTNode root;
    int size = 0;

    private class BSTNode {
        K key;
        V value;
        BSTNode left;
        BSTNode right;

        BSTNode(K key, V value){
            this.key = key;
            this.value = value;
        }
    }

    public BSTMap(){
    }

    @Override
    public void clear(){
        root = null;
        size = 0;
    }

    @Override
    public boolean containsKey(K key){
        return keyFinder(root, key);
    }

    private boolean keyFinder(BSTNode n, K key){
        if (n == null){
            return false;
        } else if (n.key.equals(key)){
            return true;
        } else if (key.compareTo(n.key) > 0) {
            return keyFinder(n.right, key);
        } else {
            return keyFinder(n.left, key);
        }
    }

    @Override
    public V get(K key){
        return getHelper(root, key);
    }

    private V getHelper(BSTNode n, K key){
        if (n == null){
            return null;
        } else if (n.key.equals(key)){
            return n.value;
        } else if (key.compareTo(n.key) > 0) {
            return getHelper(n.right, key);
        } else {
            return getHelper(n.left, key);
        }
    }

    @Override
    public int size(){
        return size;
    }

    @Override
    public void put(K key, V value){
        root = putHelper(root, key, value);
        size += 1;
    }

    private BSTNode putHelper(BSTNode n, K key, V value){
        if (n == null){
            n = new BSTNode(key, value);
        } else if (key.compareTo(n.key) > 0) {
            n.right = putHelper(n.right, key, value);
        } else {
            n.left = putHelper(n.left, key, value);
        }
        return n;
    }

    @Override
    public V remove(K key){
        throw unsupportedOperationError("command: remove not supported");
    }

    @Override
    public V remove(K key, V value){
        throw unsupportedOperationError("command: remove not supported");
    }

    @Override
    public Iterator<K> iterator(){
        throw unsupportedOperationError("command: iterator not supported");
    }

    @Override
    public Set<K> keySet(){
        throw unsupportedOperationError("command: keySet not supported");
    }

    /** Prints out BSTMap in order of increasing Key. */
    void printInOrder(){
        // in-order traversal of BST
        printInOrder(root);
    }

    private void printInOrder(BSTNode n){
        if (n == null){
            return;
        }
        printInOrder(n.left);
        System.out.println(n.key);
        printInOrder(n.right);
    }

    static UnsupportedOperationException unsupportedOperationError(String msg, Object... args){
        return new UnsupportedOperationException(String.format(msg, args));
    }
}
