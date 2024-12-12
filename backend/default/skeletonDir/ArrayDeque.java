import java.util.Iterator;

public class ArrayDeque<T> implements Deque<T>, Iterable<T> {
    private T[] data;
    private int size;
    private int iFront;
    private int iEnd;

    public ArrayDeque() {
    }

    private void resize(int newCapacity) {
    }


    @Override
    public void addFirst(T item) {
    }

    @Override
    public void addLast(T item) {
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public void printDeque() {
    }

    @Override
    public T removeFirst() {
        return null;
    }

    @Override
    public T removeLast() {
        return null;
    }

    @Override
    public T get(int index) {
        return null;
    }

    @Override
    public Iterator<T> iterator() {
        return new ArrayDequeIterator();
    }

    private class ArrayDequeIterator implements Iterator<T> {
        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public T next() {
            return null;
        }
    }

    /** to test method mismatch
    @Override
    public boolean equals(Object o, Object p) {
        return false;
    }
    */


}
