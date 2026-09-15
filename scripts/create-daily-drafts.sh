#!/usr/bin/env bash
# Creates Daily 25-47 as draft issues on sem4pi2526_2da1-board (project #2203)
set -euo pipefail

OWNER="Departamento-de-Engenharia-Informatica"
PROJECT=2203

create_daily() {
  local num="$1"
  local date="$2"
  local body="$3"
  echo "Creating Daily ${num} - ${date}..."
  gh project item-create "$PROJECT" \
    --owner "$OWNER" \
    --title "Daily ${num} - ${date}" \
    --body "$body"
}

# Daily 25 - 12/05/2026
create_daily 25 "12/05/2026" "$(cat <<'EOF'
What will I do today:
@vvitorr - Review Sprint 2 MVP checklist and prepare JaCoCo coverage improvements for airport and engine-model packages.
@BKTales - Implement US102 safety violations in C: fuel depletion, low altitude and crash detection (#47), including segfault hotfix.
@alexandrehenrique0 - Continue US101 movement processing and physics improvements in flight_simulator.c.
@joaofigueiredo123 - Finalize US031-US033 user management tests to reach 90% coverage before Sprint 2 deadline.
@Henrique-1211487 - Continue US070-US072 fleet management bootstrap and integration tests.

What did I do yesterday:
@vvitorr - Reviewed US057 engine certification workflow and Sprint 2 backlog items for final delivery.
@BKTales - Worked on US057 certified engine model workflow improvements and Sprint 2 wrap-up tasks.
@alexandrehenrique0 - Started US101 physics and individual process information upgrade in the C simulator.
@joaofigueiredo123 - Finished US031 tests and updated US032/US033 tests for user management package coverage.
@Henrique-1211487 - Finished US072 implementation and tests for fleet management.

Difficulties:
@vvitorr - None
@BKTales - None
@alexandrehenrique0 - None
@joaofigueiredo123 - None
@Henrique-1211487 - None
EOF
)"

# Daily 26 - 13/05/2026
create_daily 26 "13/05/2026" "$(cat <<'EOF'
What will I do today:
@vvitorr - Raise JaCoCo coverage above 90% for airport (#18), air-transport-company (#23) and engine-model (#20) packages.
@BKTales - Continue US102 safety violation detection and finalize C report generation for simulation runs.
@alexandrehenrique0 - Integrate US102 violation signals with US101 movement processing pipeline.
@joaofigueiredo123 - Run mvn verify and fix remaining user-management test gaps for Sprint 2 DoD.
@Henrique-1211487 - Strengthen fleet management service tests and bootstrap seed data.

What did I do yesterday:
@vvitorr - Reviewed Sprint 2 MVP deliverables and identified coverage gaps in domain packages.
@BKTales - Implemented US102 safety violations: fuel, low altitude and crash detection with proper error handling (#47).
@alexandrehenrique0 - Continued US101 physics refactor and process information tracking in flight_simulator.c.
@joaofigueiredo123 - Updated US031-US033 test suites toward 90% JaCoCo threshold.
@Henrique-1211487 - Continued US070-US072 fleet feature implementation and test coverage.

Difficulties:
@vvitorr - None
@BKTales - None
@alexandrehenrique0 - None
@joaofigueiredo123 - None
@Henrique-1211487 - None
EOF
)"

# Daily 27 - 14/05/2026
create_daily 27 "14/05/2026" "$(cat <<'EOF'
What will I do today:
@vvitorr - Implement Unified Console Launcher with role-based routing, quit and logout features (close #157).
@BKTales - Add C simulation report export to CSV and plain text files for US109 groundwork.
@alexandrehenrique0 - Close remaining US101 tasks and align C simulator with Sprint 2 acceptance criteria.
@joaofigueiredo123 - Support console launcher integration and verify user-management flows across roles.
@Henrique-1211487 - Finish fleet management implementation, testing and interface infrastructure (close #28).

What did I do yesterday:
@vvitorr - Achieved +90% JaCoCo coverage for airport (#18), air-transport-company (#23) and engine-model (#20); refactored VO constructors to private with valueOf pattern (#12).
@BKTales - Completed US102 safety violation detection and hotfixed segfault pointer assignment in C simulator.
@alexandrehenrique0 - Continued US101 movement processing and physics integration with violation detection.
@joaofigueiredo123 - Ran mvn verify and addressed remaining test failures in user management package.
@Henrique-1211487 - Expanded fleet bootstrap data and strengthened service-level tests.

Difficulties:
@vvitorr - None
@BKTales - None
@alexandrehenrique0 - None
@joaofigueiredo123 - None
@Henrique-1211487 - None
EOF
)"

# Daily 28 - 15/05/2026
create_daily 28 "15/05/2026" "$(cat <<'EOF'
What will I do today:
@vvitorr - Add test runner and environment flight plans for simulation validation (close #53).
@BKTales - Close backlog of daily meeting issues (#59-#153) and finalize C report export formats.
@alexandrehenrique0 - Complete US documentation closes (#158, #159, #160) for Sprint 2 deliverables.
@joaofigueiredo123 - Finish US031 and US032 documentation updates (#95, #96) for Sprint 2 DoD.
@Henrique-1211487 - Continue fleet feature documentation and interface infrastructure tasks.

What did I do yesterday:
@vvitorr - Delivered Unified Console Launcher with role-based routing and quit/logout (close #157); reviewed US052 documentation (#80).
@BKTales - Started C simulation report export to CSV and text file formats.
@alexandrehenrique0 - Closed remaining US101 integration tasks (close #146).
@joaofigueiredo123 - Verified console launcher flows across backoffice and operator roles.
@Henrique-1211487 - Completed fleet management implementation, testing and interface infrastructure (close #28).

Difficulties:
@vvitorr - None
@BKTales - None
@alexandrehenrique0 - None
@joaofigueiredo123 - None
@Henrique-1211487 - None
EOF
)"

# Daily 29 - 16/05/2026
create_daily 29 "16/05/2026" "$(cat <<'EOF'
What will I do today:
@vvitorr - Expand bootstrap seed data and strengthen service invariant tests (#81, #86, #129, close #164).
@BKTales - Standardize US041/US050/US057 documentation structure for Sprint 2 final submission.
@alexandrehenrique0 - Close remaining Sprint 2 documentation and test issues (#168, #169).
@joaofigueiredo123 - Finalize US031-US033 documentation (#95, #96, #97) for Sprint 2 MVP deadline.
@Henrique-1211487 - Complete fleet feature implementation closes (#154-#167) and documentation (#93, #30, #91).

What did I do yesterday:
@vvitorr - Added test runner and environment flight plans for simulation runs (close #53).
@BKTales - Closed backlog of daily meeting issues (#59-#153) and finalized text-based C report export.
@alexandrehenrique0 - Closed Sprint 2 documentation tasks (#158, #159, #160).
@joaofigueiredo123 - Updated US031 documentation (#95) and US032 documentation (#96).
@Henrique-1211487 - Continued fleet interface and infrastructure documentation.

Difficulties:
@vvitorr - None
@BKTales - None
@alexandrehenrique0 - None
@joaofigueiredo123 - None
@Henrique-1211487 - None
EOF
)"

# Daily 30 - 17/05/2026
create_daily 30 "17/05/2026" "$(cat <<'EOF'
What will I do today:
@vvitorr - Update collision test cases and simulation reports (#53); verify Sprint 2 MVP acceptance criteria.
@BKTales - Finalize US041/US050/US057 docs with standardized 5-file structure and SVG diagrams for Sprint 2 submission.
@alexandrehenrique0 - Complete final domain model documentation changes and Sprint 2 test closes (#118, #169).
@joaofigueiredo123 - Verify all Sprint 2 user stories pass mvn verify and manual testing before MVP freeze.
@Henrique-1211487 - Close fleet interface and infrastructure tasks (#155) and verify Sprint 2 deliverables.

What did I do yesterday:
@vvitorr - Expanded bootstrap seed data and tests (close #164); strengthened service invariant tests.
@BKTales - Standardized US041/US050/US057 documentation artifacts and fixed minor test issues.
@alexandrehenrique0 - Closed Sprint 2 documentation (#168) and final test suite updates.
@joaofigueiredo123 - Finalized US031-US033 documentation (#95, #96, #97).
@Henrique-1211487 - Completed fleet implementation closes (#161-#167, #154) and documentation (#93, #30, #28, #91).

Difficulties:
@vvitorr - None
@BKTales - None
@alexandrehenrique0 - None
@joaofigueiredo123 - None
@Henrique-1211487 - None
EOF
)"

# Daily 31 - 18/05/2026
create_daily 31 "18/05/2026" "$(cat <<'EOF'
What will I do today:
@vvitorr - Create Sprint 3 documentation folder structure under docs/sprint3/ for all user stories.
@BKTales - Publish Sprint 3 planning with responsibility distribution and backlog priorities (close #174).
@alexandrehenrique0 - Begin flight_simulator.c refactor for Sprint 3 validation requirements (#175).
@joaofigueiredo123 - Plan US105 shared-memory simulation architecture and US080 flight plan domain design.
@Henrique-1211487 - Start US073 flight route creation analysis and design per Sprint 3 backlog.

What did I do yesterday:
@vvitorr - Updated collision test cases and simulation reports (#53); confirmed Sprint 2 MVP readiness.
@BKTales - Delivered Sprint 2 MVP: standardized US041/US050/US057 docs, weather role updates and diagram fixes.
@alexandrehenrique0 - Completed final domain model documentation (#118) and Sprint 2 test closes.
@joaofigueiredo123 - Verified Sprint 2 user stories meet DoD (coverage, docs, manual tests).
@Henrique-1211487 - Closed fleet interface and infrastructure (#155) for Sprint 2 delivery.

Difficulties:
@vvitorr - None
@BKTales - None
@alexandrehenrique0 - None
@joaofigueiredo123 - None
@Henrique-1211487 - None
EOF
)"

# Daily 32 - 19/05/2026
create_daily 32 "19/05/2026" "$(cat <<'EOF'
What will I do today:
@vvitorr - Continue Sprint 3 docs structure and begin NFR08 Postgres configuration planning (#177).
@BKTales - Review Sprint 3 backlog priorities and prepare TCP/UDP infrastructure design (#180).
@alexandrehenrique0 - Refactor flight_simulator.c time management and modularize C components (#175).
@joaofigueiredo123 - Draft US105 POC design for parent threads, child processes and shared memory.
@Henrique-1211487 - Begin US073 flight route domain model and persistence layer implementation.

What did I do yesterday:
@vvitorr - Created Sprint 3 documentation folder structure under docs/sprint3/.
@BKTales - Published Sprint 3 planning with team responsibilities and mandatory scope (close #174).
@alexandrehenrique0 - Reviewed Sprint 3 SCOMP requirements and planned flight_simulator.c refactor approach.
@joaofigueiredo123 - Planned US105 SHM architecture and US080 flight plan domain for Sprint 3.
@Henrique-1211487 - Started US073 flight route analysis and design documentation.

Difficulties:
@vvitorr - None
@BKTales - None
@alexandrehenrique0 - None
@joaofigueiredo123 - None
@Henrique-1211487 - None
EOF
)"

# Daily 33 - 20/05/2026
create_daily 33 "20/05/2026" "$(cat <<'EOF'
What will I do today:
@vvitorr - Begin Postgres config and .env support scripts for NFR08 remote RDBMS (#177).
@BKTales - Design minimal TCP protocol v1 and message framing for RCOMP infrastructure (#180).
@alexandrehenrique0 - Implement time skip for remaining flights and improve modularity in flight_simulator.c (#175).
@joaofigueiredo123 - Start US105 shared-memory simulation POC with semaphores and child processes (#49).
@Henrique-1211487 - Implement US073 flight route creation service and repository layer.

What did I do yesterday:
@vvitorr - Continued Sprint 3 documentation structure and Postgres planning (#177).
@BKTales - Reviewed Sprint 3 RCOMP scope and prepared TCP/UDP infrastructure design (#180).
@alexandrehenrique0 - Refactored flight_simulator.c time changes and began modularization (#175).
@joaofigueiredo123 - Drafted US105 POC design for hybrid simulation environment.
@Henrique-1211487 - Continued US073 flight route domain model implementation.

Difficulties:
@vvitorr - None
@BKTales - None
@alexandrehenrique0 - None
@joaofigueiredo123 - None
@Henrique-1211487 - None
EOF
)"

# Daily 34 - 21/05/2026
create_daily 34 "21/05/2026" "$(cat <<'EOF'
What will I do today:
@vvitorr - Continue NFR08 Postgres configuration and .env scripts preparation (#177).
@BKTales - Begin TCP/UDP infrastructure implementation with embedded server and session auth (#180).
@alexandrehenrique0 - Add fuel capacity, payload and MTOW validation for each flight leg in C (#175).
@joaofigueiredo123 - Continue US105 SHM POC: parent threads, shared memory allocation and semaphore setup.
@Henrique-1211487 - Add US073 flight route UI and controller with uniqueness validation rules.

What did I do yesterday:
@vvitorr - Planned Postgres config and .env support for NFR08 remote RDBMS deployment.
@BKTales - Designed minimal TCP protocol v1 with message framing for remote client applications.
@alexandrehenrique0 - Implemented time skip for remaining flights and improved modularity in flight_simulator.c (#175).
@joaofigueiredo123 - Started US105 shared-memory simulation POC architecture (#49).
@Henrique-1211487 - Implemented US073 flight route creation service and persistence layer.

Difficulties:
@vvitorr - None
@BKTales - None
@alexandrehenrique0 - None
@joaofigueiredo123 - None
@Henrique-1211487 - None
EOF
)"

# Daily 35 - 22/05/2026
create_daily 35 "22/05/2026" "$(cat <<'EOF'
What will I do today:
@vvitorr - Finalize Postgres config/.env support and deployment scripts for NFR08 (close #177).
@BKTales - Continue TCP/UDP infrastructure skeleton with default ports and host configuration (#180).
@alexandrehenrique0 - Integrate leg validation (fuel, payload, MTOW) with physics engine in flight_simulator.c (#175).
@joaofigueiredo123 - Test US105 SHM POC with basic parent-child process communication.
@Henrique-1211487 - Write US073 flight route unit tests and documentation for Sprint 3 DoD.

What did I do yesterday:
@vvitorr - Continued NFR08 Postgres configuration and .env scripts development.
@BKTales - Started TCP/UDP infrastructure implementation with embedded server (#180).
@alexandrehenrique0 - Began fuel capacity, payload and MTOW per-leg validation in C simulator (#175).
@joaofigueiredo123 - Continued US105 SHM POC with shared memory and semaphore setup.
@Henrique-1211487 - Added US073 flight route UI and controller with domain validation rules.

Difficulties:
@vvitorr - None
@BKTales - None
@alexandrehenrique0 - None
@joaofigueiredo123 - None
@Henrique-1211487 - None
EOF
)"

# Daily 36 - 23/05/2026
create_daily 36 "23/05/2026" "$(cat <<'EOF'
What will I do today:
@vvitorr - Document flight simulator flux and prepare integration notes for US100 area simulation (#181).
@BKTales - Finalize TCP/UDP infrastructure baseline and weather permission fixes for WEATHER_PERSON role (#13, #180).
@alexandrehenrique0 - Restore physics with functional time and integrate committed leg validations (close #175).
@joaofigueiredo123 - Continue US105 implementation and prepare flight plan domain for US080.
@Henrique-1211487 - Complete US073 flight route tests and begin US074 route deactivation design.

What did I do yesterday:
@vvitorr - Finalized Postgres config/.env support and deployment scripts (close #177).
@BKTales - Continued TCP/UDP infrastructure with port and host defaults (#180).
@alexandrehenrique0 - Integrated fuel, payload and MTOW leg validation with physics engine (#175).
@joaofigueiredo123 - Tested US105 SHM POC with parent-child process communication.
@Henrique-1211487 - Wrote US073 flight route unit tests and Sprint 3 documentation.

Difficulties:
@vvitorr - None
@BKTales - None
@alexandrehenrique0 - None
@joaofigueiredo123 - None
@Henrique-1211487 - None
EOF
)"

# Daily 37 - 25/05/2026
create_daily 37 "25/05/2026" "$(cat <<'EOF'
What will I do today:
@vvitorr - Write flight simulator flux documentation and begin RCOMP default ports configuration (#181, #180).
@BKTales - Deliver TCP/UDP infrastructure baseline (close #180) and fix weather data permission for WEATHER_PERSON (#13).
@alexandrehenrique0 - Continue flight simulator validation work and support pilot domain downstream stories.
@joaofigueiredo123 - Begin US105 shared-memory simulation POC (#49) and prepare US080 flight plan domain.
@Henrique-1211487 - Start US073 flight route creation (domain, UI and tests) per Sprint 3 W1 priorities.

What did I do yesterday:
@vvitorr - Documented flight simulator flux and prepared US100 area simulation integration notes.
@BKTales - Finalized TCP/UDP infrastructure and aligned weather permission tests (#13).
@alexandrehenrique0 - Restored physics with functional time and integrated leg validations (close #175).
@joaofigueiredo123 - Continued US105 SHM implementation and US080 flight plan preparation.
@Henrique-1211487 - Completed US073 flight route tests and started US074 deactivation design.

Difficulties:
@vvitorr - None
@BKTales - None
@alexandrehenrique0 - None
@joaofigueiredo123 - None
@Henrique-1211487 - None
EOF
)"

# Daily 38 - 26/05/2026
create_daily 38 "26/05/2026" "$(cat <<'EOF'
What will I do today:
@vvitorr - Write US085 flight plan validation documentation and begin C-side validation integration (#190).
@BKTales - Start US042/US043 weather import and consultation implementation plus US091 logging dashboard.
@alexandrehenrique0 - Align flight plan export paths with Java integration and continue simulator validation (#175).
@joaofigueiredo123 - Continue US105 SHM POC and prepare US080 flight plan draft lifecycle.
@Henrique-1211487 - Continue US073 route creation and begin US074 route deactivation implementation.

What did I do yesterday:
@vvitorr - Published flight simulator flux documentation (close #181) and configured RCOMP default ports (#180).
@BKTales - Delivered TCP/UDP infrastructure (close #180), fixed weather permissions (#13) and cleaned sprint planning scope.
@alexandrehenrique0 - Implemented US075 pilot creation: domain, services, bootstrap, persistence, UI and tests (close #182-#185).
@joaofigueiredo123 - Planned US105 architecture and reviewed US080 flight plan requirements.
@Henrique-1211487 - Advanced US073 flight route domain, UI and persistence implementation.

Difficulties:
@vvitorr - None
@BKTales - None
@alexandrehenrique0 - None
@joaofigueiredo123 - None
@Henrique-1211487 - None
EOF
)"

# Daily 39 - 27/05/2026
create_daily 39 "27/05/2026" "$(cat <<'EOF'
What will I do today:
@vvitorr - Implement US078 ATCC remote TCP app, US100 simulation UI, US120/US121 DSL work and RCOMP modules (#37, #171-#173, #180).
@BKTales - Implement US042 bulk weather CSV import, US043 consultation, US082 weather attach and US091 log dashboard.
@alexandrehenrique0 - Fix flight plan export path to sibling flight_simulator directory with timestamp filenames.
@joaofigueiredo123 - Support pilot remote access flows (US086) and flight plan workflows for Sprint 3 integration.
@Henrique-1211487 - Continue US073/US074 route management and prepare for US044 Weather Person TCP client.

What did I do yesterday:
@vvitorr - Published US085 flight plan validation documentation (close #190).
@BKTales - Planned US042/US043 weather features and US091 HTTP+AJAX logging dashboard.
@alexandrehenrique0 - Implemented US076 pilot listing (close #186-#189), fixed pilot company context (#191, #192) and ATCC JPA refactor (#179).
@joaofigueiredo123 - Continued US105 SHM POC development (#49).
@Henrique-1211487 - Continued US073 route creation and US074 deactivation design.

Difficulties:
@vvitorr - None
@BKTales - None
@alexandrehenrique0 - None
@joaofigueiredo123 - None
@Henrique-1211487 - None
EOF
)"

# Daily 40 - 28/05/2026
create_daily 40 "28/05/2026" "$(cat <<'EOF'
What will I do today:
@vvitorr - Fix logging dashboard view filtering and configure debug logger for HTTP/UDP logs (#170).
@BKTales - Deliver US042 bulk import, US043 consult, US082 weather attach, US091 log viz, JaCoCo gate and Lombok (#14, #15, #41, #170, #202).
@alexandrehenrique0 - Document US077 pilot removal (#198) and refactor pilot JPA mapping (#179).
@joaofigueiredo123 - Implement US105 shared-memory simulation environment (close #49).
@Henrique-1211487 - Continue US073/US074 route features and prepare US044 Weather Person remote client.

What did I do yesterday:
@vvitorr - Delivered US078 ATCC remote TCP app (close #37), US100 simulation UI (#171), US120 semantic DSL errors (#172), US121 flight plan import (#173) and RCOMP modules (#180).
@BKTales - Cleaned sprint planning document and prepared weather/logging feature commits.
@alexandrehenrique0 - Fixed flight plan export path with timestamp-based filenames to prevent overwrites.
@joaofigueiredo123 - Supported pilot listing/creation company-scoped session fixes from 26/05.
@Henrique-1211487 - Advanced US073 route creation and US074 deactivation implementation.

Difficulties:
@vvitorr - None
@BKTales - None
@alexandrehenrique0 - None
@joaofigueiredo123 - None
@Henrique-1211487 - None
EOF
)"

# Daily 41 - 29/05/2026
create_daily 41 "29/05/2026" "$(cat <<'EOF'
What will I do today:
@vvitorr - Continue US085 C validation integration and US108 step-by-step semaphore synchronization.
@BKTales - Support US090 UDP client integration testing against US091 logging dashboard.
@alexandrehenrique0 - Begin US090 UDP logging emission from remote access flows and US106 function-specific threads.
@joaofigueiredo123 - Continue US080 flight plan creation and prepare US086 Pilot remote TCP client.
@Henrique-1211487 - Implement US074 route deactivation and begin US044 Weather Person TCP client.

What did I do yesterday:
@vvitorr - Fixed logging dashboard view filtering (#170) and configured HTTP/UDP debug logging.
@BKTales - Delivered US042 bulk import (#14), US043 consult (#15), US082 weather attach (#41), US091 log viz (#170), JaCoCo coverage gate and Lombok dependency (#202).
@alexandrehenrique0 - Documented US077 pilot removal (#198) and refactored pilot JPA mapping (#179).
@joaofigueiredo123 - Implemented US105 shared-memory simulation environment (close #49).
@Henrique-1211487 - Continued US073/US074 route management features.

Difficulties:
@vvitorr - None
@BKTales - None
@alexandrehenrique0 - None
@joaofigueiredo123 - None
@Henrique-1211487 - None
EOF
)"

# Daily 42 - 01/06/2026
create_daily 42 "01/06/2026" "$(cat <<'EOF'
What will I do today:
@vvitorr - Implement US077 pilot removal with inactivation rules and begin US108 step sync semaphores.
@BKTales - Implement US107 violation notification via condition variables between SCOMP threads.
@alexandrehenrique0 - Implement US106 function-specific threads: safety thread and report thread with mutex/cond vars.
@joaofigueiredo123 - Implement US080 flight plan creation with draft lifecycle, route/aircraft/pilot/fuel binding.
@Henrique-1211487 - Implement US074 flight route deactivation with planned-flight blocking rules.

What did I do yesterday:
@vvitorr - Continued US085 C validation integration and US108 semaphore synchronization design.
@BKTales - Supported US090 UDP client testing against US091 logging dashboard ports (UDP 2227 / HTTP 2224).
@alexandrehenrique0 - Implemented CreatePilotCollaboratorDTO (#197) and ResponsePilotCollaboratorDTO (#196) with updated comms server handlers.
@joaofigueiredo123 - Planned US080 flight plan domain model and US086 Pilot remote TCP client architecture.
@Henrique-1211487 - Advanced US074 route deactivation design and US044 Weather Person TCP client planning.

Difficulties:
@vvitorr - None
@BKTales - None
@alexandrehenrique0 - None
@joaofigueiredo123 - None
@Henrique-1211487 - None
EOF
)"

# Daily 43 - 02/06/2026
create_daily 43 "02/06/2026" "$(cat <<'EOF'
What will I do today:
@vvitorr - Integrate US085 C-side flight plan validation with Java backoffice and DSL semantic rules.
@BKTales - Complete US107 condition variable notification between safety and report threads.
@alexandrehenrique0 - Continue US106 safety thread implementation with mutex and condition variables.
@joaofigueiredo123 - Continue US080 flight plan persistence and controller tests toward 90% JaCoCo coverage.
@Henrique-1211487 - Implement US044 Weather Person remote TCP client for US041-US043 weather operations.

What did I do yesterday:
@vvitorr - Implemented US077 pilot removal with inactivation and flight-plan assignment blocking rules.
@BKTales - Implemented US107 violation notification via condition variables in SCOMP thread model.
@alexandrehenrique0 - Started US106 function-specific threads: safety and report threads with synchronization primitives.
@joaofigueiredo123 - Implemented US080 flight plan creation with draft lifecycle and domain bindings.
@Henrique-1211487 - Implemented US074 flight route deactivation with date-based rules and planned-flight checks.

Difficulties:
@vvitorr - None
@BKTales - None
@alexandrehenrique0 - None
@joaofigueiredo123 - None
@Henrique-1211487 - None
EOF
)"

# Daily 44 - 03/06/2026
create_daily 44 "03/06/2026" "$(cat <<'EOF'
What will I do today:
@vvitorr - Complete US085 validation tests and integrate C validator with US120 DSL semantic rules.
@BKTales - Implement TCP session authentication (US030) on all remote endpoints per Sprint 3 integration task.
@alexandrehenrique0 - Implement US090 UDP logging client emission from remote access flows.
@joaofigueiredo123 - Implement US086 Pilot remote TCP client for US080-US082 and US085 operations.
@Henrique-1211487 - Finalize US073/US074 route management tests and US044 Weather Person client validation.

What did I do yesterday:
@vvitorr - Integrated US085 C-side flight plan validation with Java backoffice flows.
@BKTales - Completed US107 condition variable notification between SCOMP threads.
@alexandrehenrique0 - Continued US106 safety thread with mutex/cond var synchronization.
@joaofigueiredo123 - Continued US080 flight plan persistence layer and controller unit tests.
@Henrique-1211487 - Implemented US044 Weather Person remote TCP client for weather data operations.

Difficulties:
@vvitorr - None
@BKTales - None
@alexandrehenrique0 - None
@joaofigueiredo123 - None
@Henrique-1211487 - None
EOF
)"

# Daily 45 - 04/06/2026
create_daily 45 "04/06/2026" "$(cat <<'EOF'
What will I do today:
@vvitorr - Complete US108 step-by-step semaphore lock-step synchronization per time step.
@BKTales - Test end-to-end remote access flows: weather attach, logging dashboard and TCP auth.
@alexandrehenrique0 - Integrate US090 UDP log emission with US091 dashboard event store.
@joaofigueiredo123 - Implement Java-C bridge to invoke flight_simulator for area simulation (US100 integration).
@Henrique-1211487 - Run integration tests for US044 Weather Person client and US073/US074 route workflows.

What did I do yesterday:
@vvitorr - Completed US085 validation tests and integrated C validator with US120 DSL semantics.
@BKTales - Implemented TCP session authentication on remote endpoints (US030 integration).
@alexandrehenrique0 - Implemented US090 UDP logging client for remote access event emission.
@joaofigueiredo123 - Implemented US086 Pilot remote TCP client for flight plan and validation operations.
@Henrique-1211487 - Finalized US073/US074 route tests and validated US044 Weather Person client flows.

Difficulties:
@vvitorr - None
@BKTales - None
@alexandrehenrique0 - None
@joaofigueiredo123 - None
@Henrique-1211487 - None
EOF
)"

# Daily 46 - 05/06/2026
create_daily 46 "05/06/2026" "$(cat <<'EOF'
What will I do today:
@vvitorr - Run manual end-to-end tests for ATCC (#37), Pilot (US086) and area simulation (US100) flows.
@BKTales - Verify US042/US043/US082 weather flows and US091 logging dashboard with remote UDP events.
@alexandrehenrique0 - Test US090 UDP logging integration with US091 dashboard and fix any port resolution issues.
@joaofigueiredo123 - Run mvn verify, fix CI failures and complete US080/US086 documentation in docs/sprint3/.
@Henrique-1211487 - Complete docs/sprint3/ documentation for US073, US074 and US044 per Sprint 3 DoD.

What did I do yesterday:
@vvitorr - Completed US108 step-by-step semaphore synchronization for SCOMP time-step lock-step.
@BKTales - Tested end-to-end remote access: weather attach, TCP auth and logging dashboard consumption.
@alexandrehenrique0 - Integrated US090 UDP log emission with US091 LogEventStore and HTTP AJAX dashboard.
@joaofigueiredo123 - Implemented Java-C bridge subprocess invocation for flight_simulator area simulation.
@Henrique-1211487 - Ran integration tests for US044 Weather Person client and route management workflows.

Difficulties:
@vvitorr - None
@BKTales - None
@alexandrehenrique0 - None
@joaofigueiredo123 - None
@Henrique-1211487 - None
EOF
)"

# Daily 47 - 08/06/2026
create_daily 47 "08/06/2026" "$(cat <<'EOF'
What will I do today:
@vvitorr - Close US078 ATCC remote access, US085 validation and US108 step sync for W3 deadline; prepare LAPR4 demo.
@BKTales - Coordinate Sprint 3 W3 review: verify US042/US043/US082/US091 deliverables and board status flow (NFR01).
@alexandrehenrique0 - Close US090 UDP logging client and US106 thread model for SCOMP W3 milestones.
@joaofigueiredo123 - Close US080 flight plan creation and US086 Pilot remote TCP client for W3 deadline.
@Henrique-1211487 - Close US074 route deactivation and US044 Weather Person client; finalize route documentation.

What did I do yesterday:
@vvitorr - Ran manual end-to-end tests for ATCC, Pilot remote and US100 area simulation flows.
@BKTales - Verified weather import/consultation/attach flows and US091 logging dashboard with UDP events.
@alexandrehenrique0 - Tested US090-US091 UDP logging integration and resolved port configuration issues.
@joaofigueiredo123 - Ran mvn verify, fixed CI issues and completed US080/US086 Sprint 3 documentation.
@Henrique-1211487 - Completed docs/sprint3/ documentation for US073, US074 and US044 per DoD.

Difficulties:
@vvitorr - None
@BKTales - None
@alexandrehenrique0 - None
@joaofigueiredo123 - None
@Henrique-1211487 - None
EOF
)"

echo "All 23 daily draft issues created successfully."
