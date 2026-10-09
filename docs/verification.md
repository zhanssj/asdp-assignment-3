# Recorded local verification

Date: **9 October 2026**. Environment: Windows, Eclipse Temurin **25.0.4**, Spring Boot **4.1.1**, Gradle Wrapper **9.7.1**, application **0.1.0**.

Command: `.\gradlew.bat --no-daemon clean build` with `JAVA_HOME` set to JDK 25.

| Suite | Executed cases | Failures | Errors | Skipped |
| --- | ---: | ---: | ---: | ---: |
| CsvDatasetParserTests | 15 | 0 | 0 | 0 |
| StatisticsCalculatorTests | 12 | 0 | 0 | 0 |
| AnalysisApiTests | 20 | 0 | 0 | 0 |
| AsdpAssignment3ApplicationTests | 1 | 0 | 0 | 0 |
| **Total** | **48** | **0** | **0** | **0** |

Gradle reported **BUILD SUCCESSFUL** and produced `build/libs/scientific-csv-analyzer.jar`. These counts include parameterized test invocations; they are not a coverage percentage. The original context test is retained alongside the new tests.

The packaged JAR was started on port **18083** for independent live-server checks using headless Microsoft Edge through Playwright. Ten checks passed:

1. Built-in example selected the temperature column and returned six expected statistics.
2. JSON downloaded with mean 24, five measurements and a 64-character input SHA-256.
3. Summary CSV downloaded with the same mean.
4. Changing the unit invalidated old results and exports.
5. A dataset with a blank measurement reported the exclusion.
6. Uploaded HTML-like text appeared literally in the preview.
7. Duplicate headers displayed a useful error and disabled analysis.
8. A 390-pixel-wide viewport had no page-level horizontal overflow.
9. A real multipart upload above 5 MiB returned HTTP 413 with a clear error.
10. The exercised browser workflow raised no JavaScript exceptions.

Desktop/mobile screenshots and the actual downloaded result were used to verify the interface and update the report/presentation. The fixture `20,22,24,26,28` returned mean/median 24 and sample deviation `3.1622776601683795` within the numerical test tolerance.

**Remote status:** repository visibility was verified public. CI/release YAML and issue templates exist locally; the assistant made no commits, pushes, remote issue posts or releases. A successful GitHub Actions run, source publication, issue records and a release remain owner-controlled submission steps. Windows local verification does not establish that a Linux-hosted workflow has run. No numerical coverage percentage, experimental validity or production deployment is claimed.
