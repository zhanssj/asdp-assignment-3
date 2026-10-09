# Project task record

This local record is not a claim that GitHub issues have been created. Publish pending issues after owner review and push. Reusable issue templates are included.

| Task | Acceptance condition | Local status |
| --- | --- | --- |
| CSV validation | Quoted UTF-8 CSV works; wrong width, duplicate headers and limits fail clearly | Implemented and tested |
| Scientific calculation | Fixture yields mean/median 24 and sample deviation √10; single-value deviation is undefined | Implemented and tested |
| Browser workflow | Example/upload, column/unit selection, preview, statistics, chart and both downloads work | Implemented and verified |
| Reproducibility | Exclusions, original record positions, input hash and version accompany results | Implemented and tested |
| Automated verification | Clean build tests the scientific module and API and produces an executable JAR | Verified locally |
| CI and delivery | Push/PR builds and tests; version tag delivers a tested JAR | Configured locally; remote run pending |
| Public submission | Code, README, license, issues and Actions evidence are accessible | Visibility verified public; owner commit/push and issues pending |

Suggested remaining GitHub issues:

1. **Publish and verify the Assignment 3 implementation.** Acceptance: commit/push reviewed changes, inspect the first Actions run, record its URL and inspect the test artifact.
2. **Deliver version 0.1.0.** Acceptance: successful CI, matching build/application versions, owner-pushed `v0.1.0`, and released JAR tested against the documented fixture.

Histograms, multiple-column comparison and a command-line entry point are future ideas outside the completed first-version scope.
