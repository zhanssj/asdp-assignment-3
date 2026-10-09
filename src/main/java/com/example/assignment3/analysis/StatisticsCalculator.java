package com.example.assignment3.analysis;

import org.springframework.stereotype.Component;
import java.util.Arrays;
import static com.example.assignment3.analysis.AnalysisModels.Statistics;

@Component
public class StatisticsCalculator {
    public Statistics calculate(double[] values) {
        if (values.length == 0) {
            throw new CsvValidationException("The selected column contains no numeric measurements.");
        }
        if (Arrays.stream(values).anyMatch(v -> !Double.isFinite(v))) {
            throw new CsvValidationException("Measurements must be finite numbers.");
        }
        double[] sorted = values.clone();
        Arrays.sort(sorted);
        double origin = values[0];
        for (double value : values) {
            if (!Double.isFinite(value - origin)) {
                origin = 0;
                break;
            }
        }
        double scale = 0;
        for (double value : values) {
            scale = Math.max(scale, Math.abs(value - origin));
        }
        double mean = origin;
        Double standardDeviation = values.length == 1 ? null : 0.0;
        if (scale != 0) {
            CompensatedSum sum = new CompensatedSum();
            for (double value : values) {
                sum.add((value - origin) / scale);
            }
            double normalizedMean = sum.value() / values.length;
            mean = origin + normalizedMean * scale;
            if (values.length > 1) {
                CompensatedSum squaredDeviations = new CompensatedSum();
                for (double value : values) {
                    double deviation = (value - origin) / scale - normalizedMean;
                    squaredDeviations.add(deviation * deviation);
                }
                standardDeviation = Math.sqrt(squaredDeviations.value() / (values.length - 1)) * scale;
            }
        }
        if (!Double.isFinite(mean) || (standardDeviation != null && !Double.isFinite(standardDeviation))) {
            throw new CsvValidationException("The measurements exceed the supported numeric range for these statistics.");
        }
        int middle = sorted.length / 2;
        double median = sorted[middle];
        if (sorted.length % 2 == 0) {
            double lower = sorted[middle - 1];
            double difference = median - lower;
            median = Double.isFinite(difference) ? lower + difference / 2 : lower / 2 + median / 2;
        }
        return new Statistics(values.length, sorted[0], sorted[sorted.length - 1], mean, median, standardDeviation);
    }

    /** Kahan summation reduces rounding loss when many normalized values are added. */
    private static final class CompensatedSum {
        private double sum;
        private double correction;
        void add(double value) {
            double corrected = value - correction;
            double next = sum + corrected;
            correction = (next - sum) - corrected;
            sum = next;
        }
        double value() { return sum; }
    }
}
