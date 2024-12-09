package org.uturano.tests;

import org.junit.Test;
import org.uturano.code.Addition;
import org.uturano.code.Subtraction;

import static org.junit.Assert.assertEquals;

public class JUnitFile {
    @Test
    public void testAddition() {
        Addition addition = new Addition();
        assertEquals(5, addition.add(2, 3)); // 測試應該通過
    }

    @Test
    public void testSubtraction() {
        Subtraction subtraction = new Subtraction();
        assertEquals(1, subtraction.subtract(3, 2)); // 測試應該失敗
    }
}

