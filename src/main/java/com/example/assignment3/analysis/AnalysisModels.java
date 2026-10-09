package com.example.assignment3.analysis;

import java.util.List;

/** Immutable values shared by the parser, calculation layer and HTTP boundary. */
public final class AnalysisModels {
    private AnalysisModels() { }

    public record CsvRow(long recordNumber, List<String> values) { }
    public record CsvDataset(List<String> columns, List<CsvRow> rows, String sha256) { }
    public record DatasetPreview(String filename, List<String> columns,
                                 List<String> numericColumns, int rowCount,
                                 List<List<String>> previewRows, String sha256) { }
    /** recordNumber counts logical CSV records, with the header as record 1. */
    public record Measurement(long recordNumber, double value) { }
    public record Statistics(int count, double minimum, double maximum, double mean,
                             double median, Double sampleStandardDeviation) { }
    public record AnalysisResult(String filename, String column, String unit,
                                 int totalRows, int excludedBlankValues,
                                 Statistics statistics, List<Measurement> measurements,
                                 String inputSha256, String applicationVersion,
                                 String standardDeviationMethod, String missingValuePolicy) { }
}
