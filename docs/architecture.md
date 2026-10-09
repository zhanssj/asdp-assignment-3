# Architecture and scientific boundaries

1. The browser sends a multipart CSV to `POST /api/datasets/preview`.
2. `AnalysisController` delegates to `AnalysisService`, which validates the filename and opens a bounded stream.
3. `CsvDatasetParser` checks UTF-8, structure and limits, returns immutable records and hashes the original bytes.
4. The browser presents numeric column candidates and 20 preview rows. It resubmits the selected file, column and unit to `POST /api/analysis`.
5. The service excludes blanks, rejects invalid values and calls the independent `StatisticsCalculator`.
6. Immutable results serialize to JSON. The browser displays six statistics and a canvas profile, and creates JSON/summary-CSV downloads from the response.

Every request is self-contained. Repeated preview/analysis parsing avoids persistent dataset sessions. Byte, record, column and field limits bound input. Uploaded text is displayed through `textContent`. Controls have labels, status is announced, keyboard file selection works and layout adapts to narrow screens. Changing file/column/unit invalidates old results; generation checks and cancellation prevent older responses from replacing newer selections.

The scientific layer uses sample deviation and reports missingness without imputation. Units are descriptive metadata. The chart preserves data-row order and discontinuities for blanks; its axis is not time. Results include exact input SHA-256 and application version; tests provide independent numerical references.

Double precision limits range and precision. Shifted/scaled calculation with compensated summation reduces intermediate overflow and loss of variation on a large offset. Final numeric overflow is rejected. Values below the representable range may round to zero. Advanced methods require additional algorithms, reference datasets and validation.

This assignment version is intended for local use. A public multi-user service would additionally require operational access control, concurrency limits and deployment configuration outside this scope.
