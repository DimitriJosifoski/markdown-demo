# Steelworks Production Tracker

A **Spring Boot** web application that cross-references Quality, Shipping, and Production data so Operations Analysts can detect production-line issues and defects using lot IDs and dates.

> **Course:** CS 480 Practicum · **Date:** February 2026

---

## Table of Contents

1. [Project Description](#project-description)
2. [How to Run / Build](#how-to-run--build)
3. [Usage Examples](#usage-examples)
4. [How to Run Tests](#how-to-run-tests)
5. [Architecture Overview](#architecture-overview)
6. [Tech Stack](#tech-stack)
7. [Project Structure](#project-structure)
8. [Database Schema](#database-schema)
9. [Acceptance Criteria → Implementation Map](#acceptance-criteria--implementation-map)
10. [Test Coverage by AC](#test-coverage-by-ac)
11. [Environment Variables to Configure](#environment-variables-to-configure)
12. [Design Documents](#design-documents)

---

## Project Description

Steelworks Production Tracker is a server-rendered web application built for Operations Analysts at a steel manufacturing company. It consolidates data from three separate domains — **Quality / Defects**, **Shipping / Fulfillment**, and **Production / Line Activity** — into a single interface so analysts can quickly identify which production lines are generating the most defects, which shipped batches may pose a quality risk, and where data inconsistencies exist.

### User Story

> **As an** Operations Analyst,  
> **I want to** look up files using lot IDs and dates,  
> **So that I can** filter and group data to detect production lines' issues and defects.

### Key Capabilities

- **Lot Lookup with Fuzzy Matching** — search for lots even when the ID format varies (e.g. `LOT-123`, `LOT 123`, `lot123` all resolve to the same record).
- **Dashboard Summary** — view production-line defect rankings, shipping risk alerts, defect trend arrows (↑ / ↓ / ―), orphaned data, and data conflicts at a glance.
- **Time Grouping Toggle** — switch the dashboard between Daily, Weekly (default), and Monthly aggregation.
- **Lot Detail View** — drill into a single lot to see its production logs, shipping records, defect history, line attribution, and source-file traceability.
- **Data Integrity Checks** — automatically flag orphaned lots (lots with no production or shipping activity) and data conflicts (a single lot appearing on multiple production lines).

---

## How to Run / Build

### Prerequisites

- **Java 17** or later — verify with `java -version`
- **Maven 3.8+** — verify with `mvn -version`
- **PostgreSQL database** on Render (or a local instance)

### Quick Start

**Clone and run:**

```bash
# Mac/Linux
chmod +x run-dev.sh
./run-dev.sh

# Windows PowerShell
.\run-dev.ps1
```

That's it! The script loads your database credentials from `.env` and starts the app on **http://localhost:8080**.

> **Note:** You need a `.env` file with your database connection details in the project root. See [`.env.example`](.env.example) for the format.

### Database Setup (First Time)

Connect to your PostgreSQL instance and run the schema:

```bash
psql "your-connection-string" -f db/schema.sql
```

### Build & Package

```bash
# Compile + run tests + create JAR
mvn clean package

# Compile + create JAR (skip tests)
mvn clean package -DskipTests
```

### 5. Seed sample data (optional)

Use the queries in [`db/sample_queries.sql`](db/sample_queries.sql) as a reference, or insert test data via pgAdmin / DBeaver.

---

## Usage Examples

Once the application is running at `http://localhost:8080`:

### View the Dashboard

Navigate to **http://localhost:8080/** or **http://localhost:8080/dashboard**.

The dashboard displays five panels:

| Panel | What It Shows |
|---|---|
| **Production Line Rankings** | Lines ranked by defect count in the selected period (AC5) |
| **Shipping Risk Alerts** | Batches shipped despite having critical defects (AC6) |
| **Defect Trends** | Defect types with trend arrows comparing current vs. previous period (AC7) |
| **Orphaned Lots** | Lots that exist in the `lots` table but have no production or shipping records (AC10) |
| **Data Conflicts** | Lots that appear on more than one production line (AC11) |

Use the **time-grouping toggle** at the top of the dashboard to switch between Daily, Weekly, and Monthly views:

```
http://localhost:8080/dashboard?timeGrouping=DAILY
http://localhost:8080/dashboard?timeGrouping=WEEKLY   (default)
http://localhost:8080/dashboard?timeGrouping=MONTHLY
```

### Search for a Lot (Fuzzy Match)

Navigate to **http://localhost:8080/lots** and enter a lot ID in the search box. The search uses fuzzy matching, so format variations are handled automatically:

```
Search input:  "LOT-2024-001"   →  finds lot LOT2024001
Search input:  "lot 2024 001"   →  finds lot LOT2024001
Search input:  "LOT2024001"     →  finds lot LOT2024001
```

Or hit the search endpoint directly:

```
http://localhost:8080/lots/search?q=LOT-2024-001
```

### View Lot Details

Click any lot in the list or navigate directly:

```
http://localhost:8080/lots/LOT2024001
```

The detail page shows:
- **Shipping status** — "Shipped" or "In Inventory" (AC3)
- **Production line attribution** — which line produced this lot (AC4)
- **Defect history** — all defects logged against this lot
- **Source file traceability** — the original CSV/file name and row number for each record (AC9)
- **Data conflict warning** — a banner appears if the lot was produced on multiple lines (AC11)

---

## How to Run Tests

Tests use an **in-memory H2 database** (no external services or credentials needed).

```bash
# Run all tests:
mvn test

# Run a single test class:
mvn test -Dtest=FuzzyMatchServiceTest

# Run a single test method:
mvn test -Dtest=LotServiceTest#shippingStatus_shipped

# Run all tests and generate a Surefire report:
mvn surefire-report:report
# Report is written to target/site/surefire-report.html
```

**Test classes and what they cover:**

| Test Class | Tests | Covers |
|---|---|---|
| `FuzzyMatchServiceTest` | 15 | AC2 (normalisation + fuzzy matching) |
| `LotServiceTest` | 8 | AC1, AC2, AC3, AC4, AC9, AC11 |
| `DashboardServiceTest` | 14 | AC5, AC6, AC7, AC8, AC10, AC11 |
| `DataIntegrityServiceTest` | 5 | AC10, AC11 |
| `DashboardControllerTest` | 5 | AC5–AC8, AC10, AC11 (HTTP layer) |
| `LotControllerTest` | 5 | AC1–AC4, AC9 (HTTP layer) |

All 11 Acceptance Criteria are covered by at least one test.

---

## Architecture Overview

| Decision | Choice | Rationale |
|---|---|---|
| Communication | Client–Server | Clear separation; standard for web apps |
| Deployment | Monolith | Small team; single codebase and deploy unit |
| Code Organisation | Layered Architecture | Predictable; junior-friendly |
| Data Ownership | Single PostgreSQL DB | ACID compliance; hosted on Render |
| Interaction Model | Synchronous | Immediate feedback; simplest model |

See full ADRs in [`docs/architecture_decision_records.md`](docs/architecture_decision_records.md).

---

## Tech Stack

| Layer | Technology |
|---|---|
| Backend Framework | Spring Boot 3.2.5 (Spring MVC) |
| Templating | Thymeleaf |
| ORM | Spring Data JPA / Hibernate |
| Database | PostgreSQL (Render-hosted) |
| Testing DB | H2 (in-memory, PostgreSQL mode) |
| Build Tool | Maven |
| Java Version | 17 (LTS) |
| CSS | Bootstrap 5 + custom `styles.css` |

See full tech stack ADRs in [`docs/tech_stack_decision_records.md`](docs/tech_stack_decision_records.md).

---

## Project Structure

```
markdown-demo/
├── pom.xml                              # Maven build configuration
├── .env.example                         # Template for environment variables
├── .gitignore
├── db/
│   ├── schema.sql                       # PostgreSQL DDL (run this on Render)
│   └── sample_queries.sql               # Example analytical queries
├── docs/
│   ├── architecture_decision_records.md
│   ├── assumptions_scope.md
│   ├── data_design.md
│   └── tech_stack_decision_records.md
└── src/
    ├── main/
    │   ├── java/com/steelworks/tracker/
    │   │   ├── SteelworksTrackerApplication.java   # Entry point
    │   │   ├── controller/
    │   │   │   ├── DashboardController.java        # Dashboard routes
    │   │   │   └── LotController.java              # Lot lookup routes
    │   │   ├── dto/                                 # Data Transfer Objects
    │   │   │   ├── DashboardDTO.java
    │   │   │   ├── DataConflictDTO.java
    │   │   │   ├── DefectTrendDTO.java
    │   │   │   ├── LineDefectCountDTO.java
    │   │   │   ├── LotDetailDTO.java
    │   │   │   ├── OrphanedLotDTO.java
    │   │   │   └── ShippingRiskDTO.java
    │   │   ├── model/                               # JPA Entities
    │   │   │   ├── Customer.java
    │   │   │   ├── DefectType.java
    │   │   │   ├── Lot.java
    │   │   │   ├── ProductionLine.java
    │   │   │   ├── ProductionLog.java
    │   │   │   └── ShippingLog.java
    │   │   ├── repository/                          # Spring Data JPA repos
    │   │   │   ├── CustomerRepository.java
    │   │   │   ├── DefectTypeRepository.java
    │   │   │   ├── LotRepository.java
    │   │   │   ├── ProductionLineRepository.java
    │   │   │   ├── ProductionLogRepository.java
    │   │   │   └── ShippingLogRepository.java
    │   │   └── service/                             # Business logic
    │   │       ├── DashboardService.java
    │   │       ├── DataIntegrityService.java
    │   │       ├── FuzzyMatchService.java
    │   │       └── LotService.java
    │   └── resources/
    │       ├── application.properties               # Main config
    │       ├── static/css/styles.css                # Custom CSS
    │       └── templates/
    │           ├── dashboard.html                   # Summary dashboard
    │           ├── lots.html                        # Lot list + search
    │           ├── lot-detail.html                  # Single lot detail
    │           └── fragments/layout.html            # Shared navbar/footer
    └── test/
        ├── resources/
        │   └── application-test.properties          # H2 test config
        └── java/com/steelworks/tracker/
            ├── controller/
            │   ├── DashboardControllerTest.java
            │   └── LotControllerTest.java
            └── service/
                ├── DashboardServiceTest.java
                ├── DataIntegrityServiceTest.java
                ├── FuzzyMatchServiceTest.java
                └── LotServiceTest.java
```

---

## Database Schema

The full DDL is in [`db/schema.sql`](db/schema.sql). Run it against your Render PostgreSQL instance before starting the application.

**Key tables:**

| Table | Purpose |
|---|---|
| `lots` | Central anchor; holds lot IDs and the normalised ID for fuzzy matching |
| `production_logs` | Manufacturing floor activity (defects, line, shift) |
| `shipping_logs` | Fulfillment records (ship date, customer, carrier) |
| `production_lines` | Reference table for physical manufacturing lines |
| `defect_types` | Standardised defect categories with severity levels |
| `customers` | Downstream buyers receiving shipments |

**Schema additions beyond the original design:**

| Column | Table | Why |
|---|---|---|
| `normalized_lot_id` | `lots` | AC2: pre-computed normalised form for O(log n) fuzzy lookups |
| `source_file` | `production_logs`, `shipping_logs` | AC9: traceability back to the original import file |
| `source_row_number` | `production_logs`, `shipping_logs` | AC9: exact row in the source file |

---

## Acceptance Criteria → Implementation Map

| AC | Description | Implemented In |
|---|---|---|
| **AC1** | Join Quality, Shipping, Production via Lot ID | `Lot` entity relationships; `LotService.buildLotDetail()` |
| **AC2** | Fuzzy Matching for Lot IDs | `FuzzyMatchService.normalize()`; `Lot.normalizedLotId` column; `LotRepository.findByNormalizedLotId()` |
| **AC3** | Shipping Status Logic ("Shipped" / "In Inventory") | `LotService.buildLotDetail()` → checks `ShippingLogRepository.existsByLotIdAndShipStatus()` |
| **AC4** | Line Attribution (defect → production line) | `LotService.buildLotDetail()` → extracts line name from `ProductionLog.productionLine` |
| **AC5** | Production Line Ranking by defect count | `DashboardService.getLineRankings()` → `ProductionLogRepository.countDefectsByLineInRange()` |
| **AC6** | Shipping Risk Alert (critical defects + shipped) | `DashboardService.getShippingRisks()` → `ShippingLogRepository.findProblematicShippedBatches()` |
| **AC7** | Defect Trending with up/down arrows | `DashboardService.getDefectTrends()` compares current vs previous period counts |
| **AC8** | Default Weekly time grouping + Daily/Monthly toggle | `DashboardService.buildDashboard()` defaults to "WEEKLY"; `DashboardController` accepts `?timeGrouping=` param |
| **AC9** | Source Transparency (file name + row number) | `source_file` & `source_row_number` columns on `ProductionLog` & `ShippingLog`; shown in lot detail template |
| **AC10** | Orphaned Data flagging | `LotRepository.findOrphanedLots()` JPQL query; `DashboardService.getOrphanedLots()` |
| **AC11** | Data Conflict detection (lot on multiple lines) | `ProductionLogRepository.findLotsWithMultipleLines()` JPQL; `DashboardService.getDataConflicts()` |

---

## Test Coverage by AC

Every AC is covered by **at least one** test. Below is the mapping:

| AC | Test Class | Test Method(s) |
|---|---|---|
| **AC1** | `LotServiceTest` | `lotDetail_includesProductionLine`, `lotDetail_includesDefectSummary` |
| | `LotControllerTest` | `listLots_returnsOk`, `lotDetail_returnsDetail` |
| **AC2** | `FuzzyMatchServiceTest` | All 15 tests (normalize + isMatch) |
| | `LotServiceTest` | `findByFuzzyId_matchesWithSpaces`, `findByFuzzyId_noMatch` |
| | `LotControllerTest` | `searchLot_fuzzyMatchFound`, `searchLot_fuzzyMatchNotFound` |
| **AC3** | `LotServiceTest` | `shippingStatus_shipped`, `shippingStatus_inInventory` |
| | `LotControllerTest` | `lotDetail_returnsDetail` (DTO contains shipping status) |
| **AC4** | `LotServiceTest` | `lotDetail_includesProductionLine` |
| | `LotControllerTest` | `lotDetail_returnsDetail` (DTO contains production line) |
| **AC5** | `DashboardServiceTest` | `getLineRankings_ranksCorrectly`, `getLineRankings_noDefects` |
| | `DashboardControllerTest` | `rootUrl_returnsDashboard`, `dashboardContainsLineRankings` |
| **AC6** | `DashboardServiceTest` | `getShippingRisks_returnsRisks`, `getShippingRisks_noneFound` |
| | `DashboardControllerTest` | `rootUrl_returnsDashboard`, `dashboardContainsShippingRisks` |
| **AC7** | `DashboardServiceTest` | `defectTrend_up`, `defectTrend_down`, `defectTrend_flat`, `defectTrend_new` |
| | `DashboardControllerTest` | `rootUrl_returnsDashboard` |
| **AC8** | `DashboardServiceTest` | `buildDashboard_defaultsToWeekly_whenNull`, `buildDashboard_defaultsToWeekly_whenBlank`, `buildDashboard_respectsDaily`, `buildDashboard_respectsMonthly` |
| | `DashboardControllerTest` | `dashboardWithDailyToggle` |
| **AC9** | `LotServiceTest` | `lotDetail_includesSourceInfo` |
| | `LotControllerTest` | `lotDetail_returnsDetail` |
| **AC10** | `DashboardServiceTest` | `getOrphanedLots_findsOrphans`, `getOrphanedLots_noneOrphaned` |
| | `DataIntegrityServiceTest` | `findOrphanedLots_returnsOrphans`, `hasOrphanedData_true`, `hasOrphanedData_false` |
| | `DashboardControllerTest` | `rootUrl_returnsDashboard` |
| **AC11** | `DashboardServiceTest` | `getDataConflicts_findsConflicts`, `getDataConflicts_noConflicts` |
| | `DataIntegrityServiceTest` | `findDataConflicts_returnsConflicts`, `hasDataConflicts_false` |
| | `LotServiceTest` | `lotDetail_multipleLines_showsConflict` |
| | `DashboardControllerTest` | `rootUrl_returnsDashboard` |

---

## Environment Variables to Configure

Create a `.env` file in the project root with your database credentials. See [`.env.example`](.env.example) for the format.

| Variable | Description | Example |
|---|---|---|
| `DB_HOST` | PostgreSQL hostname | `localhost` or `your-render-db.com` |
| `DB_PORT` | PostgreSQL port | `5432` |
| `DB_NAME` | Database name | `steelworks` |
| `DB_USERNAME` | Database user | `postgres` |
| `DB_PASSWORD` | Database password | `your_password` |
| `SERVER_PORT` | App port (optional) | `8080` |

> **Note:** `.env` is in `.gitignore` and is never committed to version control.

---

## Design Documents

| Document | Description |
|---|---|
| [`docs/architecture_decision_records.md`](docs/architecture_decision_records.md) | System-level architecture decisions |
| [`docs/tech_stack_decision_records.md`](docs/tech_stack_decision_records.md) | Technology choices and alternatives |
| [`docs/data_design.md`](docs/data_design.md) | Entity definitions and ERD |
| [`docs/assumptions_scope.md`](docs/assumptions_scope.md) | Project assumptions and exclusions |