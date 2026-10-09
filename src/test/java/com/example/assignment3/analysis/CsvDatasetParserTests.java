package com.example.assignment3.analysis;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.*;

class CsvDatasetParserTests {
    private final CsvDatasetParser parser = new CsvDatasetParser();

    private AnalysisModels.CsvDataset parse(String text) {
        return parser.parse(new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8)));
    }

    @Test void supportsBomQuotedCommasUnicodeAndMultilineFields() {
        var data = parse("\uFEFFlabel,value\r\n\"Астана, lab\",2.5\r\n\"two\nlines\",4\r\n");
        assertEquals("label", data.columns().getFirst());
        assertEquals("Астана, lab", data.rows().getFirst().values().getFirst());
        assertEquals("two\nlines", data.rows().get(1).values().getFirst());
        assertEquals(3, data.rows().get(1).recordNumber());
        assertEquals(64, data.sha256().length());
    }

    @Test void trimsValuesAndSkipsEmptyPhysicalLines() {
        var data = parse("label, value\n\n A , 2.5 \n");
        assertEquals("value", data.columns().get(1));
        assertEquals("2.5", data.rows().getFirst().values().get(1));
        assertEquals(1, data.rows().size());
    }

    @Test void hashIdentifiesExactBytesRatherThanParsedContent() {
        assertNotEquals(parse("x\n1\n").sha256(), parse("x\r\n1\r\n").sha256());
        assertEquals(parse("x\n1\n").sha256(), parse("x\n1\n").sha256());
    }

    @ParameterizedTest @ValueSource(strings = {"", "x\n", "x,x\n1,2", "x,\n1,2", "x,y\n1", "x\n1,2", "x\n\"unterminated"})
    void rejectsMalformedOrUnusableDatasets(String text) {
        assertThrows(CsvValidationException.class, () -> parse(text));
    }

    @Test void rejectsInvalidUtf8() {
        assertThrows(CsvValidationException.class, () -> parser.parse(new ByteArrayInputStream(new byte[]{(byte) 0xC3, 0x28})));
    }

    @Test void rejectsOversizedFile() {
        assertThrows(CsvValidationException.class, () -> parser.parse(new ByteArrayInputStream(new byte[CsvDatasetParser.MAX_BYTES + 1])));
    }

    @Test void permitsRowLimitAndRejectsNextRow() {
        String withinLimit = "x\n" + "1\n".repeat(CsvDatasetParser.MAX_ROWS);
        assertEquals(CsvDatasetParser.MAX_ROWS, parse(withinLimit).rows().size());
        assertThrows(CsvValidationException.class, () -> parse(withinLimit + "1\n"));
    }

    @Test void rejectsTooManyColumns() {
        String header = java.util.stream.IntStream.range(0, 51).mapToObj(i -> "x" + i).collect(java.util.stream.Collectors.joining(","));
        assertThrows(CsvValidationException.class, () -> parse(header + "\n" + "1,".repeat(50) + "1\n"));
    }

    @Test void rejectsOversizedField() {
        assertThrows(CsvValidationException.class, () -> parse("label,x\n" + "a".repeat(2001) + ",1\n"));
    }
}
