# Scientific CSV Data Analyzer

A working scientific data tool for ASDP Assignment 3. Upload a CSV, select a numeric column, view descriptive statistics and a measurement profile, and download JSON or a CSV summary. One Spring Boot process serves the browser interface and API.

## Run locally

Prerequisite: **JDK 25**. Gradle 9.7.1 is supplied by the wrapper; a separate Gradle installation is unnecessary. The first build downloads dependencies.

Windows PowerShell (adjust the JDK path if needed):

```powershell
$env:JAVA_HOME = 'C:\Program Files\Eclipse Adoptium\jdk-25.0.4.7-hotspot'
.\gradlew.bat bootRun
```

Linux/macOS: set `JAVA_HOME` to JDK 25, then run `bash ./gradlew bootRun`.

Open **http://localhost:8080**. Click **Load example measurements**, then **Calculate statistics**. The example selects `temperature_C` with the unit label `°C`.

Build and run the executable JAR:

```powershell
.\gradlew.bat --no-daemon clean build
& "$env:JAVA_HOME\bin\java.exe" -jar build\libs\scientific-csv-analyzer.jar
```

Linux/macOS: `bash ./gradlew --no-daemon clean build`, then `java -jar build/libs/scientific-csv-analyzer.jar`. Add `--server.port=8081` if port 8080 is occupied.

## Input contract

- UTF-8 CSV, optionally with a BOM; comma delimiter and RFC 4180 quoted fields. Quoted commas and multiline fields are supported. Empty physical lines are ignored.
- The first logical record contains unique, case-sensitive, nonblank headers. Whitespace around unquoted fields is trimmed. Every data record must have the same number of fields as the header.
- Maximum **5 MiB** (5,242,880 bytes), **10,000** data records, **50** columns and **2,000** characters per field.
- Numbers use a decimal point or scientific notation: `-2.5`, `.5`, `1.2e3`. Decimal commas, hexadecimal numbers, Java suffixes, NaN and infinity are rejected. Arithmetic uses IEEE 754 double precision.
- Blank measurements in the selected column are excluded and counted. Invalid nonblank values cause an error identifying their logical CSV record and column. The header is record 1; the first data record is record 2. A multiline quoted field remains one logical record.
- A column containing only blanks cannot be analyzed. Other columns may contain text. The optional unit is a label of up to 32 characters, not a conversion or an automatic unit check.

Uploads are processed for the current request without a persistent dataset archive. The browser retains its selected file until replaced or the page is closed.

## Scientific method and reference result

Mean: `sum(x)/n`. Median: middle sorted value, or the average of the two middle values for even `n`. **Sample** standard deviation: `sqrt(sum((x - mean)^2)/(n - 1))`. For one observation it is undefined, returned as JSON `null` and displayed as `N/A`. A constant sample of at least two values has deviation zero.

The calculator shifts and scales values and uses compensated summation to reduce rounding loss and intermediate overflow. It rejects statistics outside the finite double range. It does not estimate measurement uncertainty, remove outliers or perform hypothesis tests.

The illustrative fixture [examples/temperature.csv](examples/temperature.csv) contains `20, 22, 24, 26, 28`:

| Count | Minimum | Maximum | Mean | Median | Sample standard deviation |
| --- | --- | --- | --- | --- | --- |
| 5 | 20 | 28 | 24 | 24 | √10 ≈ 3.1622776601683795 |

For richer demonstrations, see the [sample dataset guide](examples/README.md): seven synthetic datasets cover weather cycles, reaction decay, damped oscillation, sensor drift and outliers, missing greenhouse measurements, water quality at multiple sites and two particle populations.

Cards show six significant digits. Downloads retain API numerical values, selected column, unit, version, exclusions and SHA-256 of the exact uploaded bytes. JSON/chart positions preserve original record order, including gaps for blanks. The horizontal axis is data row index, not time. The CSV summary prefixes textual spreadsheet formulas with an apostrophe; JSON preserves original text.

## API

`POST /api/datasets/preview`, multipart `file`: headers, numeric column candidates, row count, first 20 rows and SHA-256.

`POST /api/analysis`, multipart `file`, `column`, optional `unit`: statistics, measurements and metadata.

```powershell
curl.exe -s -F "file=@examples/temperature.csv" -F "column=temperature_C" -F "unit=degC" http://localhost:8080/api/analysis
```

Validation failures return HTTP 400 with JSON `{"message":"..."}`. Multipart uploads above the configured size limit return HTTP 413. Invalid measurements are not silently replaced.

## Tests and architecture

Run `.\gradlew.bat --no-daemon clean build` (Linux/macOS: `bash ./gradlew --no-daemon clean build`). Tests cover independently calculated statistics, single/constant/negative samples, large offsets and magnitudes, malformed CSV, encoding/resource limits and multipart API integration. Read `build/reports/tests/test/index.html` and JUnit XML in `build/test-results/test/`. [docs/verification.md](docs/verification.md) records actual local verification.

| Area | Technology | Reason |
| --- | --- | --- |
| Runtime/API | Java 25, Spring Boot 4.1.1 | Reuse the existing starter and multipart/JSON support |
| Build | Gradle Wrapper 9.7.1 | Reproduce the build tool on developer/CI machines |
| CSV | Apache Commons CSV 1.14.1 | Handle quoted records correctly |
| Scientific module | Independent calculator and immutable records | Fast formula tests without an HTTP server |
| Interface | HTML/CSS/JavaScript and canvas | No separate frontend build or external CDN |
| Tests | JUnit Jupiter and Spring MockMvc | Numerical reference and integration cases |
| Delivery | GitHub Actions, executable JAR | Automated verification and downloadable releases |

`analysis/` separates parsing, validation, calculation and orchestration; `web/` handles API routing and errors. Static files are in `src/main/resources/static/`. See [docs/architecture.md](docs/architecture.md).

## GitHub, CI and delivery

Public repository: https://github.com/zhanssj/asdp-assignment-3

- [CI](.github/workflows/ci.yml): pushes to `main`, pull requests and manual runs install JDK 25, run `clean build`, retain test reports and upload the tested JAR.
- [Release](.github/workflows/release.yml): an owner-pushed semantic version tag such as `v0.1.0` builds/tests again and publishes the JAR as a GitHub Release. This delivers a locally runnable package; it does not deploy a server.
- [Issue templates](.github/ISSUE_TEMPLATE/) collect reproducible bug reports and feature criteria. [docs/project-issues.md](docs/project-issues.md) records implemented tasks and remaining publication steps.

The implementation and workflows are present **locally**. The assistant has not committed or pushed them, and no successful remote workflow or release is claimed. The owner must review, commit and push, inspect Actions, and publish issue records before submission. Update application/build versions before each release tag. Use focused feature branches, tests, diff review and owner-created commits/pull requests.

Original code uses the [MIT license](LICENSE); dependencies retain their own licenses. Keep private experimental datasets out of the public repository.

## Sources

- [Spring Boot requirements](https://docs.spring.io/spring-boot/system-requirements.html)
- [Apache Commons CSV](https://commons.apache.org/proper/commons-csv/)
- [Gradle Wrapper](https://docs.gradle.org/current/userguide/gradle_wrapper.html)
- [GitHub Actions: Java with Gradle](https://docs.github.com/en/actions/tutorials/build-and-test-code/java-with-gradle)
- [Good enough practices in scientific computing](https://doi.org/10.1371/journal.pcbi.1005510)
