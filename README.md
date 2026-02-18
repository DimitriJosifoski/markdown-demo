# markdown-demo

A documentation repository containing architectural decision records (ADRs) and design documentation for the Steelworks Project system.

## Overview

This repository serves as the central documentation hub for the Steelworks Project - an internal university system designed to help students discover campus events and enable administrators to manage event submissions.

## Purpose

The Steelworks Project is designed to:
- Help students browse and discover upcoming campus events
- Allow event organizers to submit events for approval
- Enable staff administrators to review and manage event submissions
- Provide a simple, maintainable solution for internal university use

## Documentation Structure

The repository contains the following documentation:

### `/docs` Directory

- **[architecture_decision_records.md](docs/architecture_decision_records.md)** - Core architectural decisions including:
  - Client-Server architecture pattern
  - Monolith deployment strategy
  - Layered architecture for code organization
  - Single database approach
  - Synchronous interaction model

- **[tech_stack_decision_records.md](docs/tech_stack_decision_records.md)** - Technology stack decisions including:
  - Spring Boot (MVC) for backend framework
  - Thymeleaf for server-side templating
  - JPA/Hibernate for data persistence
  - PostgreSQL for the database

- **[assumptions_scope.md](docs/assumptions_scope.md)** - Project assumptions and scope boundaries including:
  - Target user base and expected usage patterns
  - Resource constraints and team size
  - Features explicitly excluded from the current phase

## Key Design Decisions

### Architecture Pattern
The system uses a **Client-Server architecture** with a web-based frontend communicating with a centralized backend API.

### Deployment Strategy
A **Monolith** approach is used to keep deployment simple and reduce operational complexity for the small engineering team.

### Technology Stack
- **Backend**: Spring Boot with Spring MVC
- **Frontend**: Thymeleaf (server-side templating)
- **Database**: PostgreSQL (production), H2 (development/testing)
- **ORM**: Spring Data JPA with Hibernate

### Target Audience
- Students browsing events (hundreds of users)
- Event organizers submitting events (dozens of users)
- Staff administrators reviewing submissions (small number)

## Project Constraints

- **Team Size**: 2-4 engineers
- **Resources**: Limited budget and operational resources
- **Performance**: Low to moderate concurrent usage
- **Context**: Internal university system (not a public commercial product)

## Out of Scope

The following features are explicitly excluded from the current phase:
- Native mobile applications
- Public APIs for external partners
- Real-time chat or live streaming
- Complex role-based permission systems
- Advanced analytics or recommendation engines

## Contributing

This repository contains architectural documentation and decision records. When making architectural changes or decisions:

1. Document the context, decision, and alternatives considered
2. Update the relevant ADR document in the `/docs` directory
3. Ensure consistency with existing architectural patterns
4. Consider the impact on the small engineering team maintaining the system
