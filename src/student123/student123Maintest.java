package student123;

import org.junit.Test;
import static org.junit.Assert.*;

public class student123Maintest {
    @Test
    public void testAdd() {
        assertEquals(5, Mainstudent123.add(2, 3));
        assertEquals(0, Mainstudent123.add(0, 0));
    }

    @Test
    public void testNegativeAdd() {
        assertEquals(-1, Mainstudent123.add(-2, 1));
    }
}
