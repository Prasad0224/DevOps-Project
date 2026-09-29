# Ansible Configuration Management for Digital Asset Approval Platform (DAAP)

This directory provides a production-grade, reproducible Ansible configuration management and release orchestration setup for the Digital Asset Approval Platform.

## Architecture

- **Ansible Controller**: Isolated Docker container (`daap-ansible-img`) with `ansible-core`.
- **Target Node**: Ubuntu 22.04 Docker container (`daap-target-img`) representing a clean staging/production host.
- **Port**: Host port `8083` mapped to target container `8083`.
- **Process Manager**: `supervisor` running the Spring Boot application as dedicated system user `daap`.
- **Layout**:
  ```
  /opt/daap/
  ├── releases/
  │   └── <version>/
  │       └── app.war
  ├── current -> /opt/daap/releases/<version>/
  ├── config/
  │   └── application.properties
  └── logs/
      ├── app.log
      └── app_error.log
  ```

## Playbooks

- `site.yml`: Installs packages (Java 17, curl, supervisor), configures system user/groups, directory hierarchy, deploys WAR, configures supervisor, and performs health verification.
- `rollback.yml`: Atomically shifts `current` symlink to a previous release version, restarts supervisor, and confirms health verification.
- `inventory.ini`: Target node definition with SSH connection settings.

## Execution

1. **Start Target Node**:
   ```bash
   docker run -d --name daap-target --network daap-net -p 8083:8083 daap-target-img
   ```

2. **Run Playbook (Controller Container)**:
   ```bash
   docker run --rm --network daap-net \
     -v "${PWD}/ansible:/ansible" \
     -v "${PWD}/target:/target" \
     daap-ansible-img -i inventory.ini site.yml
   ```

3. **Run Rollback**:
   ```bash
   docker run --rm --network daap-net \
     -v "${PWD}/ansible:/ansible" \
     -v "${PWD}/target:/target" \
     -e ROLLBACK_VERSION=1.0.9 \
     daap-ansible-img -i inventory.ini rollback.yml
   ```
