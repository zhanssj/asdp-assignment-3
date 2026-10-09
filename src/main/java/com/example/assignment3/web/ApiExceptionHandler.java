package com.example.assignment3.web;

import com.example.assignment3.analysis.CsvValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

@RestControllerAdvice
public class ApiExceptionHandler {
    public record ApiError(String message) { }

    @ExceptionHandler(CsvValidationException.class)
    public ResponseEntity<ApiError> validation(CsvValidationException exception) {
        return ResponseEntity.badRequest().body(new ApiError(exception.getMessage()));
    }

    @ExceptionHandler({MissingServletRequestParameterException.class, MissingServletRequestPartException.class})
    public ResponseEntity<ApiError> missingInput(Exception exception) {
        return ResponseEntity.badRequest().body(new ApiError("Provide a CSV file and, for analysis, a column name."));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiError> tooLarge(MaxUploadSizeExceededException exception) {
        return ResponseEntity.status(HttpStatus.CONTENT_TOO_LARGE).body(new ApiError("The CSV file must be no larger than 5 MiB."));
    }
}
