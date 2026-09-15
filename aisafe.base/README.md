# AISafe Base Skeleton

Base skeleton prepared to start a new project using the EAPLI framework.
All sample/example domain code was removed.

## Build

```bash
mvn clean install
```

## Bootstrap

```bash
mvn -pl aisafe.bootstrap exec:java -Dexec.mainClass=eapli.aisafe.infrastructure.bootstrapers.Bootstrapper
```

## Lombok

Lombok is configured in the parent `pom.xml` (all modules inherit it). In VS Code / Cursor, install the recommended extensions (Java Extension Pack and Lombok Annotations Support). Workspace settings enable `java.jdt.ls.lombokSupport.enabled`. After changing the POM, run **Java: Clean Java Language Server Workspace** from the command palette.
