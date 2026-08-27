# Automated Digital Asset Approval Platform

A single portal for submitting digital assets for approval with real-time status tracking and auditable workflows.

## Problem Statement
Organizations that publish digital assets rely on manual, email- or chat-based approval processes. Requests get lost, there is no single place to see approval status, and reviewers lack structured ways to act. This platform provides a centralized approval queue, tracking, and audit trails.

## Tech Stack
*   **Backend**: Java 17, Spring Boot 3
*   **Build Tool**: Maven
*   **Database**: MySQL / PostgreSQL
*   **Frontend**: HTML/CSS/JavaScript (Thymeleaf/React)
*   **Deployment**: Apache Tomcat + Nginx
*   **CI/CD**: GitHub Actions

## Local Development Setup
1.  Install JDK 17+, Maven, and MySQL/PostgreSQL locally.
2.  Clone the GitHub repository.
3.  Configure database connection in `application.properties`.
4.  Run: `mvn clean install` to build the project.
5.  Run: `mvn spring-boot:run` to start the app locally (default port 8080).
6.  Access the app at `http://localhost:8080`.
7.  For deployment testing, package as WAR (`mvn package`) and deploy to local Tomcat.

## Folder Structure
*   `/src/main/java`: Application source code (controllers, services, models)
*   `/src/main/resources`: Config files, templates, application.properties
*   `/src/test/java`: Unit and integration tests
*   `/docs`: SRS, diagrams, and project documentation
*   `/.github/ISSUE_TEMPLATE`: Bug report and feature request templates

## How to Contribute
*   **Branching Policy**: Direct commits to `main` are disabled. All changes go through pull requests.
*   **Branch Naming**: 
    *   `feature/*` for new features (e.g., `feature/asset-submission-form`)
    *   `bugfix/*` for bug fixes (e.g., `bugfix/status-not-updating`)
    *   `release/*` for releases (e.g., `release/v1.0`)
*   **PR Process**: Create a pull request to merge changes. Ensure CI build passes and obtain at least 1 reviewer approval before merging.

## License
MIT License
