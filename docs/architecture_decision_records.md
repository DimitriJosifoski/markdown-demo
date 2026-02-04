# Architecture Decision Record: Campus Event Hub Core Infrastructure

## 1. System Roles & Communication: Client–Server
**Status:** Accepted

### Context
The system needs to support students browsing events and admins reviewing them. Usage is internal to the university with low-to-moderate concurrent traffic.

### Decision
We will use a **Client–Server** architecture. A web-based frontend will communicate with a centralized backend API via HTTPS.

### Alternatives Considered
* **Event-Driven Architecture (EDA):** Rejected due to unnecessary operational complexity (brokers, async state management) for a simple CRUD application.

### Consequences
* **Positive:** Clear separation of concerns; easier to debug; industry-standard for web apps.
* **Negative:** The server remains a single point of failure (though mitigated by low availability requirements).

---

## 2. Deployment & Evolution: Monolith
**Status:** Accepted

### Context
The engineering team is small (2–4 people) and budget/operational resources are limited.

### Decision
We will deploy the application as a **Monolith**. All functional components (submission, discovery, and admin review) will reside in a single codebase and deployment unit.

### Alternatives Considered
* **Microservices:** Rejected as the overhead of service orchestration and inter-service networking exceeds the team's capacity and the project's scale.

### Consequences
* **Positive:** Simple deployment pipeline; easier end-to-end testing; lower cloud hosting costs.
* **Negative:** Larger codebase can become harder to navigate over time if internal boundaries are not respected.

---

## 3. Code Organization: Layered Architecture
**Status:** Accepted

### Context
The team includes junior engineers who need a familiar, predictable pattern to ensure maintainability and testability.

### Decision
We will follow a **Layered Architecture** pattern:
1.  **Presentation Layer:** API Controllers/Routes.
2.  **Service/Application Layer:** Business logic (e.g., event status transitions).
3.  **Persistence/Data Layer:** Database access and queries.

### Alternatives Considered
* **Feature-Based Architecture:** While modular, it can lead to inconsistent patterns across different feature folders for a junior team.

### Consequences
* **Positive:** Highly predictable; easy to enforce "clean" dependency directions (top-down).
* **Negative:** Can lead to "leaky abstractions" where database logic occasionally creeps into the service layer.

---

## 4. Data & State Ownership: Single Database
**Status:** Accepted

### Context
Events must move through a lifecycle (Submitted → Approved/Rejected). Data consistency is vital for the "Event Discovery" user story.

### Decision
We will use a **Single Relational Database** to store all event, user, and administrative data.

### Alternatives Considered
* **Database per Service:** Rejected as it requires a microservices deployment and introduces complex data synchronization issues.

### Consequences
* **Positive:** ACID compliance ensures event status updates are reflected instantly; simplified backups and migrations.
* **Negative:** A single database schema can become a bottleneck if the data model grows extremely complex.

---

## 5. Interaction Model: Synchronous
**Status:** Accepted

### Context
Students and organizers expect immediate feedback (confirmation messages or validation errors) when interacting with the system.

### Decision
Primary interactions will be **Synchronous**. The client will wait for a response from the server before updating the UI state.

### Alternatives Considered
* **Asynchronous Processing:** Rejected for core flows, as there are no long-running processes or high-scale requirements that justify the complexity of queues.

### Consequences
* **Positive:** Simplest programming model; immediate error reporting for the user.
* **Negative:** Long-running tasks (like future email integrations) could potentially block the UI thread if not eventually moved to a background worker.
