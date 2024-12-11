package org.uturano.api.utils.v2.tests;

import org.junit.Test;
import org.uturano.api.utils.v2.student.Mainstudent111;

import static org.junit.Assert.*;
public class student111Maintest{
    @Test
    public void testAdd() {
        assertEquals(5, Mainstudent111.add(2, 3));
        assertEquals(0, Mainstudent111.add(-2, 3)); // 此测试会失败
    }
    @Test
    public void testDivide() {
        assertEquals(2, Mainstudent111.divide(4, 2));
        try {
            Mainstudent111.divide(4, 0); // 此测试会失败
            fail("Should have thrown ArithmeticException for divide by zero");
        } catch (ArithmeticException e) {
            // Expected exception
        }
    }
    @Test
    public void testIsEven() {
        assertTrue(Mainstudent111.isEven(4)); // 此测试会失败
        assertFalse(Mainstudent111.isEven(3));
    }
    @Test
    public void testMultiply() {
        assertEquals(6, Mainstudent111.multiply(2, 3));
        assertEquals(0, Mainstudent111.multiply(0, 5));
    }
    @Test
    public void testSubtract() {
        assertEquals(1, Mainstudent111.subtract(5, 4));
        assertEquals(-1, Mainstudent111.subtract(4, 5));
    }
    @Test
    public void testSquare() {
        assertEquals(16, Mainstudent111.square(4));
        assertEquals(0, Mainstudent111.square(0));
    }
}
