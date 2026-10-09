package com.example.assignment3.web;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import java.nio.charset.StandardCharsets;
import static org.hamcrest.Matchers.closeTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AnalysisApiTests {
    @Autowired MockMvc mvc;

    private MockMultipartFile csv(String content) {
        return new MockMultipartFile("file", "measurements.csv", "text/csv", content.getBytes(StandardCharsets.UTF_8));
    }

    @Test void analyzesUploadedFixtureAndReportsMetadata() throws Exception {
        mvc.perform(multipart("/api/analysis").file(csv("sample,temperature_C\n1,20\n2,22\n3,24\n4,26\n5,28\n"))
                        .param("column", "temperature_C").param("unit", "°C"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statistics.count").value(5))
                .andExpect(jsonPath("$.statistics.mean").value(24.0))
                .andExpect(jsonPath("$.statistics.sampleStandardDeviation").value(closeTo(Math.sqrt(10), 1e-12)))
                .andExpect(jsonPath("$.unit").value("°C"))
                .andExpect(jsonPath("$.applicationVersion").value("0.1.0"))
                .andExpect(jsonPath("$.inputSha256").isNotEmpty())
                .andExpect(jsonPath("$.measurements[0].recordNumber").value(2));
    }

    @Test void excludesBlanksAndPreservesLogicalRecordPositions() throws Exception {
        mvc.perform(multipart("/api/analysis").file(csv("label,x\na,1\nb,\nc,3\n")).param("column", "x"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRows").value(3))
                .andExpect(jsonPath("$.excludedBlankValues").value(1))
                .andExpect(jsonPath("$.statistics.mean").value(2.0))
                .andExpect(jsonPath("$.measurements[1].recordNumber").value(4));
    }

    @Test void singleValueSerializesDeviationAsNull() throws Exception {
        mvc.perform(multipart("/api/analysis").file(csv("x\n2\n")).param("column", "x"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.statistics.sampleStandardDeviation").value(org.hamcrest.Matchers.nullValue()));
    }

    @Test void previewIdentifiesNumericColumnsAndLimitsVisibleRows() throws Exception {
        mvc.perform(multipart("/api/datasets/preview").file(csv("label,x,invalid\n" + "lab,1,NaN\n".repeat(25))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rowCount").value(25))
                .andExpect(jsonPath("$.numericColumns.length()").value(1))
                .andExpect(jsonPath("$.numericColumns[0]").value("x"))
                .andExpect(jsonPath("$.previewRows.length()").value(20));
    }

    @Test void acceptsDecimalScientificNotation() throws Exception {
        mvc.perform(multipart("/api/analysis").file(csv("x\n+1e2\n.5\n")).param("column", "x"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.statistics.mean").value(50.25));
    }

    @ParameterizedTest @ValueSource(strings = {"NaN", "Infinity", "-Infinity", "1e309", "0x1.0p2", "2d", "word", "1,5"})
    void invalidMeasurementsReturnActionableErrors(String value) throws Exception {
        mvc.perform(multipart("/api/analysis").file(csv("x\n\"" + value + "\"\n")).param("column", "x"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("record 2")));
    }

    @Test void unknownColumnIsRejected() throws Exception {
        mvc.perform(multipart("/api/analysis").file(csv("x\n1\n")).param("column", "missing"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").exists());
    }

    @Test void blankOnlyColumnIsRejected() throws Exception {
        mvc.perform(multipart("/api/analysis").file(csv("x,label\n,a\n,b\n")).param("column", "x"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("no numeric")));
    }

    @Test void missingFileAndColumnAreRejected() throws Exception {
        mvc.perform(multipart("/api/analysis").param("column", "x")).andExpect(status().isBadRequest());
        mvc.perform(multipart("/api/analysis").file(csv("x\n1\n"))).andExpect(status().isBadRequest());
    }

    @Test void emptyFileAndIncorrectExtensionAreRejected() throws Exception {
        mvc.perform(multipart("/api/datasets/preview").file(csv(""))).andExpect(status().isBadRequest());
        mvc.perform(multipart("/api/datasets/preview").file(new MockMultipartFile("file", "data.txt", "text/plain", "x\n1".getBytes())))
                .andExpect(status().isBadRequest());
    }

    @Test void excessiveUnitLabelIsRejected() throws Exception {
        mvc.perform(multipart("/api/analysis").file(csv("x\n1\n")).param("column", "x").param("unit", "u".repeat(33)))
                .andExpect(status().isBadRequest());
    }

    @Test void uploadedPathsAreReducedToBasename() throws Exception {
        var file = new MockMultipartFile("file", "C:\\data\\lab.csv", "text/csv", "x\n1".getBytes());
        mvc.perform(multipart("/api/datasets/preview").file(file)).andExpect(status().isOk()).andExpect(jsonPath("$.filename").value("lab.csv"));
    }

    @Test void browserAssetsAndSampleAreServed() throws Exception {
        mvc.perform(get("/index.html")).andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("Scientific CSV Data Analyzer")));
        mvc.perform(get("/app.js")).andExpect(status().isOk());
        mvc.perform(get("/example-measurements.csv")).andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("5,28")));
    }
}
