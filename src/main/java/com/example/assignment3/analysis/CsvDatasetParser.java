package com.example.assignment3.analysis;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;

import static com.example.assignment3.analysis.AnalysisModels.*;

@Component
public class CsvDatasetParser {
    public static final int MAX_BYTES = 5 * 1024 * 1024;
    public static final int MAX_ROWS = 10_000;
    public static final int MAX_COLUMNS = 50;
    public static final int MAX_FIELD_LENGTH = 2_000;
    private static final CSVFormat FORMAT = CSVFormat.RFC4180.builder()
            .setIgnoreEmptyLines(true).setTrim(true).get();

    public CsvDataset parse(InputStream input) {
        try {
            byte[] bytes = input.readNBytes(MAX_BYTES + 1);
            if (bytes.length > MAX_BYTES) {
                throw new CsvValidationException("The CSV file must be no larger than 5 MiB.");
            }
            String text = decode(bytes);
            if (text.startsWith("\uFEFF")) {
                text = text.substring(1);
            }
            try (CSVParser parser = CSVParser.parse(text, FORMAT)) {
                var records = parser.iterator();
                if (!records.hasNext()) {
                    throw new CsvValidationException("The CSV file is empty. Include a header and data rows.");
                }
                List<String> columns = values(records.next());
                if (columns.size() > MAX_COLUMNS) {
                    throw new CsvValidationException("A CSV file may contain at most 50 columns.");
                }
                if (columns.stream().anyMatch(String::isBlank)) {
                    throw new CsvValidationException("Every column must have a non-empty header.");
                }
                if (new HashSet<>(columns).size() != columns.size()) {
                    throw new CsvValidationException("Column headers must be unique (case-sensitive).");
                }
                List<CsvRow> rows = new ArrayList<>();
                while (records.hasNext()) {
                    CSVRecord record = records.next();
                    if (rows.size() == MAX_ROWS) {
                        throw new CsvValidationException("A CSV file may contain at most 10,000 data rows.");
                    }
                    if (record.size() != columns.size()) {
                        throw new CsvValidationException("CSV record " + record.getRecordNumber()
                                + " has " + record.size() + " fields; expected " + columns.size() + ".");
                    }
                    rows.add(new CsvRow(record.getRecordNumber(), values(record)));
                }
                if (rows.isEmpty()) {
                    throw new CsvValidationException("The CSV file contains a header but no data rows.");
                }
                return new CsvDataset(columns, List.copyOf(rows), sha256(bytes));
            }
        } catch (CharacterCodingException e) {
            throw new CsvValidationException("Use a UTF-8 encoded CSV file.");
        } catch (IOException | UncheckedIOException | IllegalArgumentException e) {
            throw new CsvValidationException("The file could not be read as valid comma-separated CSV. Check its quotes and delimiters.");
        }
    }

    private List<String> values(CSVRecord record) {
        List<String> values = new ArrayList<>();
        for (String value : record) {
            if (value.length() > MAX_FIELD_LENGTH) {
                throw new CsvValidationException("CSV fields may contain at most 2,000 characters.");
            }
            values.add(value);
        }
        return List.copyOf(values);
    }

    private String decode(byte[] bytes) throws CharacterCodingException {
        return StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)
                .decode(ByteBuffer.wrap(bytes)).toString();
    }

    private String sha256(byte[] bytes) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is required by the Java runtime.", e);
        }
    }
}
