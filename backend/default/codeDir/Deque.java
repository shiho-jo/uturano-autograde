
public interface Deque<T> {
    void addFirst(T item);

    void addLast(T item);

    // boolean isEmpty();

    int size();

    void printDeque();

    T removeFirst();

    T removeLast();

    T get(int index);

    /** check whether a list is empty */
    default boolean isEmpty() {
        return (this.size() == 0);
    }
}
