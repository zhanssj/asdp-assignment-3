package com.example.assignment3.analysis;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import static com.example.assignment3.analysis.AnalysisModels.*;

@Service
public class AnalysisService {
    private final CsvDatasetParser parser;
    private final StatisticsCalculator calculator;
    private final String version;

    public AnalysisService(CsvDatasetParser parser, StatisticsCalculator calculator,
                           @Value("${app.version}") String version) {
        this.parser = parser;
        this.calculator = calculator;
        this.version = version;
    }

    public DatasetPreview preview(MultipartFile file) {
        CsvDataset dataset = dataset(file);
        List<String> numericColumns = new ArrayList<>();
        for (int index = 0; index < dataset.columns().size(); index++) {
            boolean numeric = true;
            boolean hasMeasurement = false;
            for (CsvRow row : dataset.rows()) {
                String value = row.values().get(index);
                if (!value.isBlank()) {
                    hasMeasurement = true;
                    if (NumericValues.finiteDecimal(value) == null) {
                        numeric = false;
                        break;
                    }
                }
            }
            if (numeric && hasMeasurement) {
                numericColumns.add(dataset.columns().get(index));
            }
        }
        return new DatasetPreview(filename(file), dataset.columns(), List.copyOf(numericColumns),
                dataset.rows().size(), dataset.rows().stream().limit(20).map(CsvRow::values).toList(), dataset.sha256());
    }

    public AnalysisResult analyze(MultipartFile file, String column, String suppliedUnit) {
        CsvDataset dataset = dataset(file);
        int index = dataset.columns().indexOf(column);
        if (index < 0) {
            throw new CsvValidationException("Select a column that exists in the CSV header.");
        }
        String unit = suppliedUnit == null ? "" : suppliedUnit.trim();
        if (unit.length() > 32 || unit.chars().anyMatch(Character::isISOControl)) {
            throw new CsvValidationException("The unit must be at most 32 characters and contain no control characters.");
        }
        List<Measurement> measurements = new ArrayList<>();
        int blanks = 0;
        for (CsvRow row : dataset.rows()) {
            String value = row.values().get(index);
            if (value.isBlank()) {
                blanks++;
            } else {
                Double number = NumericValues.finiteDecimal(value);
                if (number == null) {
                    throw new CsvValidationException("CSV record " + row.recordNumber()
                            + " in column '" + column + "' is not a finite decimal number.");
                }
                measurements.add(new Measurement(row.recordNumber(), number));
            }
        }
        Statistics statistics = calculator.calculate(measurements.stream().mapToDouble(Measurement::value).toArray());
        return new AnalysisResult(filename(file), column, unit, dataset.rows().size(), blanks, statistics,
                List.copyOf(measurements), dataset.sha256(), version, "sample (n - 1)", "exclude blank values; reject invalid nonblank values");
    }

    private CsvDataset dataset(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new CsvValidationException("Choose a non-empty CSV file.");
        }
        if (!filename(file).toLowerCase(java.util.Locale.ROOT).endsWith(".csv")) {
            throw new CsvValidationException("Choose a file with the .csv extension.");
        }
        try (var input = file.getInputStream()) {
            return parser.parse(input);
        } catch (IOException e) {
            throw new CsvValidationException("The uploaded file could not be read.");
        }
    }

    private String filename(MultipartFile file) {
        String name = file.getOriginalFilename();
        if (name == null) {
            return "dataset.csv";
        }
        name = name.replace('\\', '/');
        name = name.substring(name.lastIndexOf('/') + 1).replaceAll("[\\p{Cntrl}]", "");
        return name.length() > 200 ? name.substring(name.length() - 200) : name;
    }
}
