# README

## Overview
The **Student Management System** is a JavaFX desktop application that lets users manage students, courses, and enrollments. It follows a clean MVC architecture, uses SQLite for persistence, and provides CRUD operations, CSV import/export, and real‑time statistics.

## Prerequisites & Dependencies
- **Java 23** (or newer) – runtime and compiler.
- **Maven** – build tool (`mvn` must be in your `PATH`).
- **JavaFX 23** – included via Maven dependencies.
- **SQLite JDBC driver** – `org.xerial:sqlite-jdbc` (automatically pulled by Maven).
- **PlantUML** – for generating UML diagrams (optional, only needed to view the PNGs).

## Build & Compile
```bash
# Clean and compile the project
mvn clean compile
```
Maven will download all dependencies, compile the sources under `src/main/java`, and place class files in `target/classes`.

## Run the Application
```bash
# Launch the JavaFX UI
mvn javafx:run
```
The UI opens with a table of students, controls for adding/updating/removing entries, and a grade‑distribution chart.

## Test Suite
```bash
# Execute all JUnit 5 tests
mvn test
```
Tests cover the `StudentManagerImpl` implementation, database initialization, and connection handling.

## Database Setup
The application uses an embedded SQLite file `students.db`. The schema and sample data are provided as SQL scripts:
- `database/schema.sql` – creates tables `students`, `courses`, `enrollments`.
- `database/sample_data.sql` – inserts a handful of example records.
- `database/backup.sql` – shows how to back‑up and restore the DB.

To (re)create the database from scratch:
```bash
sqlite3 students.db < database/schema.sql
sqlite3 students.db < database/sample_data.sql
```
The application will automatically create the DB file if it does not exist.

## Architecture Diagrams
The system architecture and workflows are defined using PlantUML. You can find the source files in:
- `docs/diagrams/class_diagram.puml` – Class relationships and package structure.
- `docs/diagrams/sequence_diagram.puml` – Dynamic flow for the "Add Student" operation.

## Quick Start Checklist
1. Install **Java 23** and **Maven**.
2. Clone the repository (or copy the project folder).
3. Run `mvn clean compile`.
4. Run `mvn javafx:run` to start the UI.
5. Run `mvn test` to verify functionality.
6. Use the provided SQL scripts if you need to reset the database.

---

