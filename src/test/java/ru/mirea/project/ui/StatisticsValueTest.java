package ru.mirea.project.ui;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class StatisticsValueTest {
    @Test
    void formatsCountsAndAverageForRussianUi() {
        assertEquals("12", StatisticsController.formatValue(12L));
        assertEquals("0", StatisticsController.formatValue(0L));
        assertEquals("4,50", StatisticsController.formatValue(4.5));
        assertEquals("0,00", StatisticsController.formatValue(0.0));
        assertEquals("—", StatisticsController.formatValue(null));
    }
}
