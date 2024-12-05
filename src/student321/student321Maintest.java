package student321;

import org.junit.Test;
import static org.junit.Assert.*;

public class student321Maintest {
    @Test
    public void testAdd() {
        assertEquals(5, Mainstudent321.add(2, 3));
        assertEquals(0, Mainstudent321.add(0, 0));
    }

    @Test
    public void testNegativeAdd() {
        assertEquals(-1, Mainstudent321.add(-2, 1));
    }
}
