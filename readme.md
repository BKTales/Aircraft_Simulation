# Project AIControl

> **⚠️ Academic Integrity and Legal Warning**
> This repository contains a project developed for academic purposes during the Degree in Informatics Engineering (LEI) at ISEP. It is made public strictly for portfolio and skill demonstration purposes.
> **For current and future ISEP students:** Copying this code, in whole or in part, to submit as your own work constitutes a severe violation of academic integrity rules (plagiarism). The authors of this repository take no responsibility for any disciplinary actions taken against students who misuse this code.

---

## 1. Description of the Project

AIControl is a backoffice management system developed for AISafe, a startup targeting the flight control market. The system manages core aviation domain entities including airports, air control areas, aircraft models, flight routes, weather data, and flight plans.

The project is developed by a team of students from ISEP as part of the 4th semester integrative project (2025/2026), covering the following course units: EAPLI, LAPR4, LPROG, RCOMP, and SCOMP.

The system is built using Domain Driven Design (DDD) principles and follows a layered architecture with a Java backend, in-memory or JPA (PostgreSQL) persistence, and a console-based UI.

## 2. Planning and Technical Documentation

| Document | Content |
|----------|---------|
| [Sprint planning](docs/sprint3/sprint_planning.md) | Sprint 3 backlog, DoD, dependencies |
| [**Flight Simulator — Engineering (SCOMP)**](docs/scomp/README.md) | C architecture, IPC, threads, US index |
| [**Flight Simulator — Tests**](docs/scomp/TESTS.md) | Test strategy, environments, traceability |
| [RCOMP](docs/sprint3/RCOMP/README.md) | Remote TCP/UDP access |
| [NFR08 Deployment](docs/sprint3/NFR08/deployment.md) | Remote PostgreSQL |

## 3. How to Build

Ensure you have the following installed:
- Java 17
- Maven 3.8+

From the repository root (the folder that contains this `readme.md` and `aisafe.base`), run:

```bash
cd aisafe.base
mvn clean install -DskipTests
```

To build including all tests:

```bash
cd aisafe.base
mvn clean install
```

## 4. How to Execute Tests

```bash
cd aisafe.base
mvn clean test
```

To generate a test coverage report:

```bash
cd aisafe.base
mvn clean install
```

## 5. How to Run

All console flows use the same entry point (`AISafeConsoleApp`): login once, then the root menu (role-based options, flight management where authorized).

**Unified launcher (recommended)**

```bash
cd aisafe.base
./run-aisafe.sh
```

The script builds the reactor, then starts the console. On first setup or when you need seed data and users, run bootstrap in the same step:

```bash
cd aisafe.base
./run-aisafe.sh --bootstrap
```

**Legacy script names** (same behaviour as `run-aisafe.sh`; kept for compatibility):

- `./run-backoffice.sh`
- `./run-flightmanagement.sh`

### Startup banner

The console clears the screen and prints an ASCII logo before each login screen:

```
                         ______     __     ______     ______     ______   ______   
                        /\  __ \   /\ \   /\  ___\   /\  __ \   /\  ___\ /\  ___\  
                        \ \  __ \  \ \ \  \ \___  \  \ \  __ \  \ \  __\ \ \  __\  
                         \ \_\ \_\  \ \_\  \/_____\  \ \_\ \_\  \ \_\    \ \_____\
                          \/_/\/_/   \/_/   \/_____/   \/_/\/_/   \/_/     \/_____/
```

## 6. How to Install/Deploy into Another Machine (or Virtual Machine)

1. Ensure Java 17 and Maven 3.8+ are installed on the target machine.
2. Clone the repository:

> Using HTTPS
```bash
git clone https://github.com/BKTales/Aircraft_Simulation.git
```

> Using SSH
```bash
git git@github.com:BKTales/Aircraft_Simulation.git
```

3. Configure the git hooks:

```bash
cd sem4pi2526-sem4pi2526_2da1
git config core.hooksPath .githooks
chmod +x .githooks/pre-push
```

4. Build the project:

```bash
cd aisafe.base
mvn clean install -DskipTests
```

5. **Persistence (NFR08, ecafeteria-style):**
   - **Default:** `./run-aisafe.sh` → JPA + PostgreSQL (credenciais em `.env`).
   - **In-memory (só testes):** `./run-aisafe-inmemory.sh`
   - Setup vs233: [docs/sprint3/NFR08/deployment.md](docs/sprint3/NFR08/deployment.md)

   ```bash
   cd aisafe.base
   # Criar .env local (template em ../AISAFE-config-fora-do-GitHub.md — enviar aos colegas fora do GitHub)
   ./run-aisafe.sh --bootstrap   # first time only
   ./run-aisafe.sh               # later runs (schema-generation=none)
   ```

## 7. How to Generate PlantUML Diagrams

To generate PlantUML diagrams for documentation execute the script (for the moment, only for linux/unix/macos):

```bash
curl -L https://github.com/plantuml/plantuml/releases/download/v1.2026.2/plantuml-1.2026.2.jar -o libs/plantuml-1.2026.2.jar

./generate-plantuml-diagrams.sh
```

The generated diagrams will be placed alongside their respective `.puml` source files in the `docs/` folder.

## 8. Simulation Component Requirements

The simulation component relies on operating-system-level features that are only available on Linux and Unix-based systems.

The simulator uses native process management mechanisms, including **forks**, **signals**, and related POSIX system libraries. Therefore, it is currently supported only on:

- Linux distributions
- macOS
- Other Unix/POSIX-compatible operating systems

Windows Users: Windows environments are not supported for running the simulation component natively. You must use a compatible Linux-based environment such as WSL2, a Linux virtual machine, or a containerized setup.

Before running the simulator, ensure that the required POSIX libraries and system utilities are available in your environment. 


