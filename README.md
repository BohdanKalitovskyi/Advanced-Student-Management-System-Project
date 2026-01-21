# PDF README

## Overview
The **Student Management System** is a JavaFX desktop application that lets users manage students, courses, and enrollments. It follows a clean MVC architecture, uses SQLite for persistence, and provides CRUD operations, CSV import/export, and real‑time statistics.

## Key Features
- **Smart Sorting & Filtering**: Sort by Name, Grade, or Age; filter by course.
- **Group Analytics**: Automatic average grade calculation for selected groups.
- **Modern UI**: High-performance interface with premium CSS styling.

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
The application uses an embedded SQLite file `students.db`. The schema is provided as a SQL script, and sample data can be imported via CSV:
- `database/schema.sql` – creates tables `students`, `courses`, `enrollments`.
- `students_seed.csv` – sample data for import.
- `database/backup.sql` – shows how to back-up and restore the DB.

To create the database schema from scratch:
```bash
sqlite3 students.db < database/schema.sql
```
After starting the application, you can use the **Import CSV** button to load `students_seed.csv`.

## Quick Start Checklist
1. Install **Java 23** and **Maven**.
2. Clone the repository (or copy the project folder).
3. Run `mvn clean compile`.
4. Run `mvn javafx:run` to start the UI.
5. Use the **Import CSV** button in the UI to import `students_seed.csv`.
6. Run `mvn test` to verify functionality.

---

