import java.util.Iterator;
// import java.util.*; -- to test import mismatch

public class ArrayDeque<T> implements Deque<T>, Iterable<T> {
    private T[] data;
    private int size;
    private int iFront;
    private int iEnd;

    public ArrayDeque() {
        data = (T[]) new Object[8];
        size = 0;
        iFront = 0;
        iEnd = 0;
    }

    private void dynamicSize() {
        if (size == data.length) {
            resize(data.length * 2);
        } else if (size >= 16 && size <= data.length / 4) {
            resize(data.length / 2);
        }
    }

    private void resize(int newSize) {
        T[] newData = (T[]) new Object[newSize];
        // for (int i = 0; i < size - 1; i++) { -- to test failed JUnit test
        for (int i = 0; i < size; i++) {
            newData[i] = get(i);
        }
        data = newData;
        iFront = 0;
        iEnd = size;
    }

    private int loop(int index) {
        return (index + data.length) % data.length;
    }

    private void push(int index, T value) {
        data[loop(index)] = value;
    }

    private T pull(int index) {
        return data[loop(index)];
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void addFirst(T item) {
        dynamicSize();
        iFront--;
        iFront = loop(iFront);
        push(iFront, item);
        size++;
    }

    @Override
    public void addLast(T item) {
        dynamicSize();
        push(iEnd, item);
        iEnd++;
        iEnd = loop(iEnd);
        size++;
    }

    @Override
    public T removeFirst() {
        if (isEmpty()) {
            return null;
        }
        T removed = pull(iFront);
        push(iFront, null);
        iFront++;
        iFront = loop(iFront);
        size--;
        dynamicSize();
        return removed;
    }

    @Override
    public T removeLast() {
        if (isEmpty()) {
            return null;
        }
        iEnd--;
        iEnd = loop(iEnd);
        T removed = pull(iEnd);
        push(iEnd, null);
        size--;
        dynamicSize();
        return removed;
    }

    @Override
    public T get(int index) {
        if (index >= size || index < 0) {
            return null;
        }
        return data[loop(iFront + index)];
    }

    @Override
    public void printDeque() {
        for (int i = 0; i < size; i++) {
            System.out.print(get(i) + " ");
        }
        System.out.println();
    }

    @Override
    public Iterator<T> iterator() {
        return new ArrayDequeIterator();
    }

    private class ArrayDequeIterator implements Iterator<T> {
        private int index = 0;

        @Override
        public boolean hasNext() {
            return index < size;
        }

        @Override
        public T next() {
            T item = get(index);
            index++;
            return item;
        }

        /**
        @Override
        public boolean equals(Object o) {
            return false;
        }
        */
    }
}
