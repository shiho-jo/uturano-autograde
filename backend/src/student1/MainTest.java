package student1;

import org.junit.Test;
import static org.junit.Assert.*;

public class MainTest {
    @Test
    public void testAdd() {
        assertEquals(5, Main.add(2, 3));
        assertEquals(0, Main.add(0, 0));
    }

    @Test
    public void testNegativeAdd() {
        assertEquals(-1, Main.add(-2, 1));
    }
}
