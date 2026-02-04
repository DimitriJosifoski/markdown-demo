# Architecture Decision Record: Campus Event Hub Technology Stack

## 1. Backend Framework: Spring Boot (MVC)
**Status:** Accepted

### Context
The project requires a robust, maintainable framework that supports a layered architecture and is suitable for a small team of engineers.

### Decision
We will use **Spring Boot** with the **Spring MVC** module. This provides a "convention-over-configuration" approach, speeding up development for the Event Submission and Review features.

### Alternatives Considered
* **Node.js (Express):** Rejected. While fast, it lacks the strict structural patterns that help junior teams maintain a Layered Architecture over time.
* **Python (Django):** A strong contender, but Spring Boot was chosen for its superior type safety and deep integration with enterprise-grade tooling.

### Consequences
* **Positive:** Built-in support for security, validation, and testing; massive ecosystem and documentation.
* **Negative:** Larger memory footprint compared to lightweight frameworks; steep learning curve for those unfamiliar with Dependency Injection.

---

## 2. Templating Engine: Thymeleaf
**Status:** Accepted

### Context
To keep the deployment a "Monolith" and reduce complexity, we want to avoid the overhead of a separate frontend build system (like React or Vue).

### Decision
We will use **Thymeleaf** as our server-side templating engine. It allows us to serve dynamic HTML directly from the Spring Boot application.

### Alternatives Considered
* **JSP (JavaServer Pages):** Rejected. JSP is considered legacy technology and does not offer the "Natural Templates" (valid HTML) that Thymeleaf provides.
* **React/Single Page Application (SPA):** Rejected. This would require an API-first approach and a separate deployment/build pipeline, which is out of scope for our limited resources.

### Consequences
* **Positive:** No need for a separate frontend repository; easy to pass data from the Service layer to the UI; fast initial page loads.
* **Negative:** Less "snappy" feel compared to a modern SPA; requires a full page reload for most interactions (unless combined with HTMX/JavaScript).

---

## 3. Data Persistence: JPA / Hibernate
**Status:** Accepted

### Context
We need a way to map our Java "Event" objects to database tables while ensuring the system remains easy to extend for future features like "Featured Events."

### Decision
We will use **Spring Data JPA (with Hibernate)** as our Object-Relational Mapper (ORM).

### Alternatives Considered
* **JDBC Template:** Rejected. While faster, it requires writing significant amounts of boilerplate SQL which increases the maintenance burden for a small team.
* **MyBatis:** Rejected. Requires manual SQL mapping which can be error-prone compared to the automated mapping of JPA.

### Consequences
* **Positive:** Dramatically reduces boilerplate code for CRUD operations; automatically handles table creation and schema updates.
* **Negative:** "Magic" behavior can make complex queries difficult to debug for junior developers; potential for performance issues (N+1 query problem) if not monitored.

---

## 4. Database: PostgreSQL
**Status:** Accepted

### Context
The system requires a reliable, ACID-compliant database to handle event statuses (Submitted, Approved, Rejected) and structured searching/filtering.

### Decision
We will use **PostgreSQL** as our primary relational database.

### Alternatives Considered
* **MySQL:** A valid alternative, but PostgreSQL was chosen for its superior handling of complex queries and its reputation for reliability in academic/professional environments.
* **H2 (In-Memory):** We will use H2 for local development/testing, but it is rejected for production as it does not persist data reliably.

### Consequences
* **Positive:** Highly extensible; excellent performance for the "Event Discovery" search requirements; free and open-source.
* **Negative:** Requires more active configuration/management compared to a "NoSQL" database like MongoDB.
