package com.example.assignment3.web;

import com.example.assignment3.analysis.AnalysisModels.AnalysisResult;
import com.example.assignment3.analysis.AnalysisModels.DatasetPreview;
import com.example.assignment3.analysis.AnalysisService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
public class AnalysisController {
    private final AnalysisService service;
    public AnalysisController(AnalysisService service) { this.service = service; }

    @PostMapping(value = "/datasets/preview", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public DatasetPreview preview(@RequestParam("file") MultipartFile file) {
        return service.preview(file);
    }

    @PostMapping(value = "/analysis", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AnalysisResult analyze(@RequestParam("file") MultipartFile file,
                                  @RequestParam("column") String column,
                                  @RequestParam(value = "unit", defaultValue = "") String unit) {
        return service.analyze(file, column, unit);
    }
}
