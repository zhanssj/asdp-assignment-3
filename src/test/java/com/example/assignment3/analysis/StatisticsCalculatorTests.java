package com.example.assignment3.analysis;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

class StatisticsCalculatorTests {
    private final StatisticsCalculator calculator = new StatisticsCalculator();

    @Test void referenceFixtureMatchesHandCalculation() {
        var result = calculator.calculate(new double[]{20, 22, 24, 26, 28});
        assertEquals(5, result.count());
        assertEquals(20, result.minimum());
        assertEquals(28, result.maximum());
        assertEquals(24, result.mean());
        assertEquals(24, result.median());
        assertEquals(Math.sqrt(10), result.sampleStandardDeviation(), 1e-12);
    }

    @Test void evenMedianWorksForUnsortedNegativeValuesWithoutChangingInput() {
        double[] input = {-1, -7, -3, -5};
        var result = calculator.calculate(input);
        assertEquals(-4, result.mean());
        assertEquals(-4, result.median());
        assertArrayEquals(new double[]{-1, -7, -3, -5}, input);
    }

    @Test void oneValueHasUndefinedSampleDeviation() {
        var result = calculator.calculate(new double[]{3.5});
        assertEquals(3.5, result.mean());
        assertEquals(3.5, result.median());
        assertNull(result.sampleStandardDeviation());
    }

    @Test void repeatedMeasurementsHaveZeroDeviation() {
        assertEquals(0.0, calculator.calculate(new double[]{7, 7, 7}).sampleStandardDeviation());
    }

    @Test void addingLargeOffsetPreservesDeviation() {
        double offset = 1e12;
        var result = calculator.calculate(new double[]{offset + 20, offset + 22, offset + 24, offset + 26, offset + 28});
        assertEquals(offset + 24, result.mean());
        assertEquals(Math.sqrt(10), result.sampleStandardDeviation(), 1e-12);
    }

    @Test void scalingAvoidsOverflowInSquaresAndMedian() {
        var result = calculator.calculate(new double[]{1e200, 2e200});
        assertEquals(1.5e200, result.mean(), 1e186);
        assertEquals(1.5e200, result.median(), 1e186);
        assertEquals(1e200 / Math.sqrt(2), result.sampleStandardDeviation(), 1e186);
    }

    @Test void oppositeLargeSignsProduceFiniteStatistics() {
        var result = calculator.calculate(new double[]{-1e308, 1e308});
        assertEquals(0, result.mean());
        assertEquals(0, result.median());
        assertEquals(Math.sqrt(2) * 1e308, result.sampleStandardDeviation(), 1e294);
    }

    @Test void unavailableNumericRangeIsRejected() {
        assertThrows(CsvValidationException.class, () -> calculator.calculate(new double[]{-Double.MAX_VALUE, Double.MAX_VALUE}));
    }

    @Test void emptyMeasurementsAreRejected() {
        assertThrows(CsvValidationException.class, () -> calculator.calculate(new double[]{}));
    }

    @ParameterizedTest @ValueSource(doubles = {Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY})
    void nonFiniteValuesAreRejected(double value) {
        assertThrows(CsvValidationException.class, () -> calculator.calculate(new double[]{value}));
    }
}
