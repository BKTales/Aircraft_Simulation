# Sprint [1] Planning - Group 2DA1

## 1. Sprint Details
* **Sprint Name:** Sprint 1
* **Start Date:** [09/03/2026]
* **End Date:** [05/04/2026]
* **Team coordinator:** Alexandre Henrique (1240720)

## 2. Sprint Goal
Setting up the project foundation — repository, scripts, and structure. The team must also design a Domain-Driven Design model for the AISafe system. Finally, each aggregate must be justified with a sequence diagram showing the invariant it enforces.

## 3. Team Capacity & Availability
| Team Member                    | Role                     | Expected Hours | Notes / Absences                |
|:-------------------------------|:-------------------------|:---------------|:--------------------------------|
| Alexandre Henrique - 1240720   | Team Coordinator and Dev | 15h            | Always available                |
| Bernardo Correia - 1241456     | Dev                      | 10h            | I don't work on weekends        |
| Vitor Carneiro - 1240680       | Dev                      | 12h            | I don't work on sundays         |
| João Figueiredo - 1231095      | Dev                      | 12h            | I don't work during the morning |
| Henrique Ribeiro - 1211487     | Dev                      | 12h            | Work when possible              |

> As horas esperadas são apenas uma estimativa.

## 4. Sprint Backlog (User Stories & Tasks)

| US           | Title                       | What to do                                                                           | Responsible        | Deadline   |
|--------------|-----------------------------|--------------------------------------------------------------------------------------|--------------------|------------|
| **US001**    | Technical constraints       | Follow technical constraints in section 5 (Java, Maven, GitHub, 90% test coverage)   | Alexandre Henrique | 18/03/2026 | 
| **US002**    | Project repository          | Create GitHub repo + setup GitHub project management tool                            | Bernardo Correia   | 18/03/2026 | 
| **US003**    | Project structure           | Configure project folder/module structure supporting architecture and ANTLR          | Vitor Carneiro     | 18/03/2026 | 
| **US004**    | CI server                   | Setup GitHub Actions/Workflows for continuous integration                            | Bernardo Correia   | 18/03/2026 | 
| **US005**    | Automated deployment        | Create scripts to build/run/deploy on Unix machines                                  | Bernardo Correia   | 18/03/2026 | 
| **US010**    | Domain model                | Elaborate a DDD Domain Model for the AISafe system                                   | Everyone           | 05/04/2026 |
| **US011**    | Aggregate justification     |                                                                                      |                    |            |
| **US011-T1** | Aggregate justification     | Pilot, Aircraft and Air Transport Company                                            | Alexandre Henrique | 05/04/2026 |
| **US011-T2** | Aggregate justification     | Weather Data and Air Control Area                                                    | Bernardo Correia   | 05/04/2026 |
| **US011-T3** | Aggregate justification     | Flight and Flight Simulation Report                                                  | João Figueiredo    | 05/04/2026 |
| **US011-T4** | Aggregate justification     | Route and Airport                                                                    | Vitor Carneiro     | 05/04/2026 |
| **US011-T5** | Aggregate justification     | Manufacturer, Engine Model and Aircraft Model                                        | Henrique Ribeiro   | 05/04/2026 |
| **NFR02**    | Glossary                    | Glossary development                                                                 | Henrique Ribeiro   | 19/04/2026 |

## 5. Definition of Done (DoD)
For a US to be considered complete in this Sprint, the following criteria must be met:

### Code Compiles Locally and on GitHub Actions
- Run `mvn clean install` locally without errors
- GitHub Actions workflow must have a job that runs the same command automatically on every push

### Unit Test Coverage > 90% (JaCoCo)
- Write unit tests for all domain and controller classes
- Add the JaCoCo plugin to `pom.xml`
- Run `mvn verify` to check the coverage report

### Documentation in Markdown inside `docs/` folder
- For each US create a folder like `docs/us001/`
- Inside write files such as `analysis.md`, `design.md`, `tests.md`

### PlantUML Diagrams Updated and Generated
- Write diagrams in `.puml` files
- Generate PNGs using PlantUML and include both in the repository

### Commits Linked to the Issue
- When committing, include the issue number in the message, e.g.:
  `git commit -m "fix: add airport validation #12"`

### Functionality Manually Tested
- Run the application and test the full flow of the US
- Confirm that all acceptance criteria from the requirements are met

---

> **Summary:** code + tests + docs + diagrams + organized commits + manual testing.

## 6. Risks & Impediments
* **Risk 1:** [E.g.: Learning curve of Domain-Driven Design (DDD) may delay domain development.]
* **Risk 2:** [E.g.: Git merge conflicts if the team works on the same bootstrap files.]
* **Mitigation Strategy:** [E.g.: Make small commits, communicate on Discord/WhatsApp before modifying base files, and request Code Review instead of committing directly to `main`.]

### 7. Technology Stack Compliance
- The solution must be implemented using **Java 17**
- **Maven** must be used as the build automation tool
- **ANTLR** must be used for DSL processing (lexer, parser, visitor, listener)
- **JPA/ORM** must be used for data persistence
- The system must support both **in-memory** and **relational database (RDBMS)** persistence
- Authentication and authorization must be implemented using an appropriate **framework**
- The **C language** must be used for flight simulation components (processes, pipes, signals, threads, shared memory, semaphores)
- **UDP** must be used for the flight logging server
- **TCP** must be used for remote client applications (Weather Person, ATCC, Pilot)
- **PlantUML** must be used to generate all UML diagrams
- All diagrams must be available in both `.puml` source and `.png` format in the repository