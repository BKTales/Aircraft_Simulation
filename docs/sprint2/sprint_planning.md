# Sprint [2] Planning - Group 2DA1

## 1. Sprint Details
* **Sprint Name:** Sprint 2
* **Start Date:** [13/04/2026]
* **End Date:** [17/05/2026]
* **Team coordinator:** João Figueiredo (1231095)

## 2. Sprint Goal
Project Development; structure in C & Java. We should be able to present the MVP required by sprint 2;

## 3. Team Capacity & Availability
| Team Member                    | Role                     | Expected Hours | Notes / Absences                |
|:-------------------------------|:-------------------------|:---------------|:--------------------------------|
| Alexandre Henrique - 1240720   | Dev                      | 15h            | Always available                |
| Bernardo Correia - 1241456     | Dev                      | 10h            | I don't work on weekends        |
| Vitor Carneiro - 1240680       | Dev                      | 12h            | I don't work on sundays         |
| João Figueiredo - 1231095      | Team Coordinator and Dev | 12h            | I don't work during the morning |
| Henrique Ribeiro - 1211487     | Dev                      | 12h            | Work when possible              |

> As horas esperadas são apenas uma estimativa.

## 4. Sprint Backlog (User Stories & Tasks)

### 4.1 EAPLI 

| US                | Title                                   | What to do                                              | Responsible        | Deadline   |
|-------------------|-----------------------------------------|---------------------------------------------------------|--------------------|------------|
| **US030**         | Authentication                          | Implement authentication.                               | Todos              | 14/05/2026 |
| **US031-033**     | Users                                   | Users management.                                       | João Figueiredo    | 17/05/2026 |
| **US041**         | Weather                                 | Implement weather data.                                 | Bernardo Correia   | 17/05/2026 |
| **US050**         | Air Control Area                        | Create and manage flight control areas.                 | Bernardo Correia   | 17/05/2026 |
| **US052**         | Airport                                 | Create and manage airports.                             | Vitor Carneiro     | 17/05/2026 |
| **US055**         | Create a aircraft model                 | Create a aircraft model                                 | Alexandre Henrique | 17/05/2026 |
| **US056**         | Create an engine model                  | Create an engine model                                  | Vitor Carneiro     | 17/05/2026 |
| **US057**         | Add an engine model to an aircraft mode | Implement aircraft models and engine models management. | Bernardo Correia   | 17/05/2026 |
| **US058**         | Remove engine model                     | Implement engine models removel.                        | Todos              | 17/05/2026 |
| **US060**         | Company                                 | Create air transport company.                           | Vitor Carneiro     | 17/05/2026 |
| **US061-062**     | Collaborators                           | Manage collaborators.                                   | Alexandre Henrique | 17/05/2026 |
| **Opt-US063-064** | Collaborators                           | Manage collaborators.                                   | Todos              | 17/05/2026 |
| **US070-72**      | Add aircraft to air transport           | Manage aircraft fleets.                                 | Henrique Ribeiro   | 17/05/2026 |
 
---

### 4.2 SCOMP

| US         | Title                    | What to do                                       | Responsible            | Deadline   |
|------------|--------------------------|--------------------------------------------------|------------------------|------------|
| **US100**  | Base Simulation          | Implement simulation with processes and threads. | João Figueiredo        | 17/05/2026 |
| **US101**  | Movement Processing      | Capture and process movements.                   | Bernardo Correia       | 17/05/2026 |
| **US102**  | Safety Violations        | Violations detection.                            | Alexandre Henrique     | 17/05/2026 |
| **US103**  | Execution Sync           | Execution synchronization (time step).           | Vitor Carneiro         | 17/05/2026 |
| **US109**  | Simulation Final Report  | Generate final simulation report.                | Henrique Ribeiro       | 17/05/2026 |

---

### 4.3 LPROG (DSL)

| US        | Title                                   | What to do                                                                                                | Responsible                     | Deadline   |
|-----------|-----------------------------------------|-----------------------------------------------------------------------------------------------------------|---------------------------------|------------|
| **US081** | Create a flight plan from a file        | Create a flight plan from a file.                                                                         | Vitor / João                    | 14/05/2026 |
| **US083** | Flight DSL specification and validation | Specify and implement Flight Descritpion DSL, so that flight plans can be formally defined and validated. | Alexandre / Henrique / Bernardo | 14/05/2026 |

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
