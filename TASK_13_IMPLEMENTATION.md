# Task 13 Implementation: Configuration Management with Ansible

## 1. Overview & Architecture
The configuration management solution for the Digital Asset Approval Platform (DAAP) is built using **Ansible** and containerized orchestration to ensure reproducibility without requiring native Windows Ansible or complex virtual machines:
- **Ansible Controller**: Containerized in `daap-ansible-img` (Ubuntu 22.04 with `ansible` and `openssh-client`/`sshpass`).
- **Target Node**: Containerized in `daap-target-img` (`daap-target`), an isolated Ubuntu 22.04 system on Docker bridge network `daap-net`.
- **Target Host Port**: Host port `8083` mapped to target container port `8083`.
- **Process Manager**: `supervisor` managing the executable Spring Boot WAR under dedicated user `daap`.

---

## 2. Configuration Specification

### 2.1 Inventory (`ansible/inventory.ini`)
```ini
[daap_nodes]
daap-target ansible_host=daap-target ansible_port=22 ansible_user=ansible ansible_password=ansible ansible_ssh_common_args='-o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null' ansible_become_password=ansible

[daap_nodes:vars]
ansible_python_interpreter=/usr/bin/python3
```

### 2.2 System Packages
The playbook installs:
- `openjdk-17-jre-headless` (Java 17 runtime)
- `curl` (HTTP verification utility)
- `supervisor` (daemon process supervision)

### 2.3 User & Group
- System Group: `daap` (`system: yes`)
- Dedicated System User: `daap` (`system: yes`, `create_home: no`, `shell: /usr/sbin/nologin`)

### 2.4 Directory Structure (`/opt/daap/`)
- Base Directory: `/opt/daap/` (Owner `daap:daap`, mode `0755`)
- Release Directory: `/opt/daap/releases/`
- Active Version Directory: `/opt/daap/releases/{{ app_version }}/`
- Active Release Symlink: `/opt/daap/current -> /opt/daap/releases/{{ app_version }}`
- Configuration Directory: `/opt/daap/config/`
- Logging Directory: `/opt/daap/logs/` (`app.log`, `app_error.log`)

### 2.5 Deployed Files
- **Artifact**: `target/digital-asset-approval-platform.war` deployed to `/opt/daap/releases/{{ app_version }}/app.war`
- **Application Properties**: `/opt/daap/config/application.properties`
  ```properties
  spring.application.name=digital-asset-approval-platform
  server.port=8083
  spring.servlet.multipart.max-file-size=10MB
  spring.servlet.multipart.max-request-size=10MB
  logging.level.com.platform=INFO
  logging.level.org.springframework.web=INFO
  ```

### 2.6 Service & Process Management (`supervisor`)
Supervisor configuration at `/etc/supervisor/conf.d/daap.conf`:
```ini
[program:daap]
command=/usr/bin/java -jar /opt/daap/current/app.war --spring.config.additional-location=file:/opt/daap/config/application.properties --server.port=8083
directory=/opt/daap/current
user=daap
autostart=true
autorestart=true
startsecs=5
stopasgroup=true
killasgroup=true
stdout_logfile=/opt/daap/logs/app.log
stderr_logfile=/opt/daap/logs/app_error.log
stdout_logfile_maxbytes=10MB
stderr_logfile_maxbytes=10MB
environment=JAVA_HOME="/usr/lib/jvm/java-17-openjdk-amd64"
```

---

## 3. First Ansible Execution

### 3.1 Execution Command
```bash
docker run --rm --network daap-net \
  -v "${PWD}/ansible:/ansible" \
  -v "${PWD}/target:/target" \
  daap-ansible-img -i inventory.ini site.yml
```

### 3.2 Real First Execution Result
```
PLAY [Configure and Deploy Automated Digital Asset Approval Platform] **********

TASK [Gathering Facts] *********************************************************
ok: [daap-target]

TASK [1. Update apt cache] *****************************************************
ok: [daap-target]

TASK [2. Install required system packages (Java 17, curl, supervisor)] *********
changed: [daap-target]

TASK [3. Create dedicated system group for application] ************************
changed: [daap-target]

TASK [4. Create dedicated non-root application user] ***************************
changed: [daap-target]

TASK [5. Create application directory structure] *******************************
changed: [daap-target] => (item=/opt/daap)
changed: [daap-target] => (item=/opt/daap/releases)
changed: [daap-target] => (item=/opt/daap/config)
changed: [daap-target] => (item=/opt/daap/logs)

TASK [6. Create versioned release directory] ***********************************
changed: [daap-target]

TASK [7. Deploy application configuration (application.properties)] ************
changed: [daap-target]

TASK [8. Deploy application WAR artifact] **************************************
changed: [daap-target]

TASK [9. Point current symlink to the release version] *************************
changed: [daap-target]

TASK [10. Configure supervisor service for application] ************************
changed: [daap-target]

TASK [11. Ensure supervisor service is active] *********************************
changed: [daap-target]

RUNNING HANDLER [Reload Supervisor] ********************************************
changed: [daap-target]

TASK [13. Ensure daap process is started in supervisor] ************************
ok: [daap-target]

TASK [14. Wait for application port 8083 to be available] **********************
ok: [daap-target]

TASK [15. Verify application health check endpoint] ****************************
ok: [daap-target]

TASK [16. Log health check confirmation] ***************************************
ok: [daap-target] => {
    "msg": "DAAP Application is healthy on port 8083. Status: Application is running."
}

PLAY RECAP *********************************************************************
daap-target                : ok=17   changed=11   unreachable=0    failed=0    skipped=0    rescued=0    ignored=0   
```
* **Saved Log**: [`docs/evidence/ansible_first_run.txt`](file:///c:/Users/prash/Desktop/DevOps/DevOps-Project/docs/evidence/ansible_first_run.txt)
