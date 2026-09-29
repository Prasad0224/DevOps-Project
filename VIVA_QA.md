# DevOps Technical Viva Q&A Guide

### Q1: What is the core purpose of the DevOps lifecycle in this project?
**A**: To automate the end-to-end software delivery process—from code commit through automated compilation, unit testing, packaging, staging deployment, browser-based regression testing, containerization, and configuration management—ensuring that only verified, auditable artifacts reach production.

### Q2: How is Continuous Integration (CI) implemented?
**A**: Jenkins monitors the GitHub repository via SCM polling (`pollSCM`). On changes, it triggers an automated build that executes `mvn compile` and `mvn test` (running 7 unit tests). If any test fails, the build terminates immediately, preventing defective code from progressing.

### Q3: What is the role of Selenium in the pipeline?
**A**: Selenium WebDriver executes 5 headless Chrome end-to-end tests against the deployed Tomcat staging instance (`http://localhost:8081/digital-asset-approval-platform`). It acts as a **Quality Gate**: if any UI workflow fails, subsequent deployment and promotion stages are skipped.

### Q4: Why is packaging done as a WAR rather than a JAR?
**A**: The WAR packaging with `spring-boot-starter-tomcat` under `<scope>provided</scope>` allows the single artifact to be deployed directly into standalone servlet containers like Apache Tomcat 10.1.x, while simultaneously remaining executable via `java -jar` using Spring Boot's embedded launcher.

### Q5: What is the difference between a Docker image and a Docker container?
**A**: A Docker image is an immutable, read-only snapshot containing the application binary, dependencies, and OS libraries. A Docker container is a runnable, isolated instance of that image with a thin read-write layer and mapped network interfaces.

### Q6: Why was a local Docker registry used on port 5000?
**A**: The local registry (`localhost:5000` via `registry:2`) enables full container push and pull workflows without requiring external internet bandwidth, Docker Hub account credentials, or exposing internal academic project artifacts publicly.

### Q7: Why are ports 8080, 8081, 8082, and 8083 distinct?
**A**:
- `8080`: Jenkins CI/CD automation server.
- `8081`: Apache Tomcat 10.1.60 staging application server.
- `8082`: Dockerized application container running the verified image.
- `8083`: Ansible-managed isolated Ubuntu target host.
- `5000`: Local Docker registry.
Assigning dedicated ports prevents port binding conflicts across all running subsystems on the host.

### Q8: What is Configuration Management and why Ansible instead of Puppet?
**A**: Configuration Management ensures servers reach and maintain a defined, predictable state. Ansible was chosen over Puppet because it is **agentless** (communicating over standard SSH/Python), uses human-readable **declarative YAML**, requires no dedicated master server daemon, and integrates seamlessly with containerized targets.

### Q9: What is Idempotency in Ansible and how was it verified?
**A**: Idempotency means executing a playbook multiple times produces the exact same system state without unintended side effects. When `site.yml` was run a second time against `daap-target`, Ansible reported `ok=16 changed=0`, proving that all resources (packages, users, files, configs) were already in their desired state and required zero modifications.

### Q10: How does the Rollback mechanism work in this project?
**A**: The project uses versioned directory releases (`/opt/daap/releases/<version>/`). The active application is referenced via an atomic symbolic link (`/opt/daap/current`). If a release fails health verification, `rollback.yml` atomically updates the symlink back to the previous stable version (e.g., `1.0.9`) and restarts `supervisor`, restoring full availability within seconds without re-downloading or reinstalling.

### Q11: How is the application process managed on the Ansible target node?
**A**: Using `supervisor`. Inside containerized environments where `systemd` is typically not running, Supervisor provides robust process supervision, automatic restart on failure, log redirection (`/opt/daap/logs/app.log`), and non-root process isolation under user `daap`.

### Q12: What happens if Selenium tests fail during the Jenkins pipeline?
**A**: In declarative Jenkins pipelines, a stage failure marks the build as `FAILED` and halts downstream execution. As verified in Build #5, when Selenium fails, Stages 8 through 14 (`Docker Build`, `Docker Tag`, `Docker Push`, `Docker Deploy`, `Production Promotion`, `Ansible Provisioning`, and `Ansible Health Check`) are completely skipped.

### Q13: What are the primary architectural limitations of this setup?
**A**: The architecture operates on a single development machine using an in-memory database and an ephemeral local Docker registry. In a full production environment, these would be replaced by a managed Kubernetes cluster, cloud registry (ECR/ACR), external relational database (PostgreSQL), and automated multi-zone load balancing.
