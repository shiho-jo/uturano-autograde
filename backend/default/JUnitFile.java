import org.junit.Test;
import static org.junit.Assert.*;

/** Performs some basic linked list tests. */
public class JUnitFile {

    @Test
    public void onlyAddAndPrint() {
        ArrayDeque<Integer> lld0 = new ArrayDeque<Integer>();
        lld0.addLast(3);
        lld0.addFirst(5);
        lld0.addLast(9);
        lld0.printDeque(); // Should be: 5 3 9
    }

    @Test
    public void resizeDown() {
        ArrayDeque<Integer> ArrayDeque = new ArrayDeque<Integer>();
        ArrayDeque.addFirst(0);
        ArrayDeque.addFirst(1);
        ArrayDeque.removeLast();
        ArrayDeque.addFirst(3);
        ArrayDeque.addFirst(4);
        ArrayDeque.isEmpty();
        ArrayDeque.addFirst(6);
        ArrayDeque.removeLast();
        ArrayDeque.addFirst(8);
        ArrayDeque.addFirst(9);
        ArrayDeque.addFirst(10);
        ArrayDeque.addFirst(11);
    }

    @Test
    public void addIsEmptySizeTest() {
        ArrayDeque<String> lld1 = new ArrayDeque<String>();
        assertTrue("A newly initialized LLDeque should be empty", lld1.isEmpty());
        lld1.addFirst("front");
        assertEquals(1, lld1.size());
        assertFalse("lld1 should now contain 1 item", lld1.isEmpty());
        lld1.addLast("middle");
        assertEquals(2, lld1.size());
        lld1.addLast("back");
        assertEquals(3, lld1.size());
        lld1.printDeque();
    }

    @Test
    public void addRemoveTest() {
        ArrayDeque<Integer> lld1 = new ArrayDeque<Integer>();
        assertTrue("lld1 should be empty upon initialization", lld1.isEmpty());
        lld1.addFirst(10);
        assertFalse("lld1 should contain 1 item", lld1.isEmpty());
        lld1.removeFirst();
        assertTrue("lld1 should be empty after removal", lld1.isEmpty());
    }

    @Test
    public void removeEmptyTest() {
        ArrayDeque<Integer> lld1 = new ArrayDeque<>();
        lld1.addFirst(3);
        lld1.removeLast();
        lld1.removeFirst();
        lld1.removeLast();
        lld1.removeFirst();
        int size = lld1.size();
        String errorMsg = "  Bad size returned when removing from empty deque.\n";
        errorMsg += "  student size() returned " + size + "\n";
        errorMsg += "  actual size() returned 0\n";
        assertEquals(errorMsg, 0, size);
    }

    @Test
    public void multipleParamTest() {
        ArrayDeque<String>  lld1 = new ArrayDeque<String>();
        ArrayDeque<Double>  lld2 = new ArrayDeque<Double>();
        ArrayDeque<Boolean> lld3 = new ArrayDeque<Boolean>();
        lld1.addFirst("string");
        lld2.addFirst(3.14159);
        lld3.addFirst(true);
        String s = lld1.removeFirst();
        double d = lld2.removeFirst();
        boolean b = lld3.removeFirst();
    }

    @Test
    public void emptyNullReturnTest() {
        ArrayDeque<Integer> lld1 = new ArrayDeque<Integer>();
        assertEquals("Should return null when removeFirst is called on an empty Deque,", null, lld1.removeFirst());
        assertEquals("Should return null when removeLast is called on an empty Deque,", null, lld1.removeLast());
    }

    @Test
    public void bigLLDequeTest() {
        ArrayDeque<Integer> lld1 = new ArrayDeque<Integer>();
        for (int i = 0; i < 1000000; i++) {
            lld1.addLast(i);
        }
        for (double i = 0; i < 500000; i++) {
            assertEquals("Should have the same value", i, (double) lld1.removeFirst(), 0.0);
        }
        for (double i = 999999; i > 500000; i--) {
            assertEquals("Should have the same value", i, (double) lld1.removeLast(), 0.0);
        }
    }
}
