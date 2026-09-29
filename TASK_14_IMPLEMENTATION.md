# Task 14 Implementation: Automated Provisioning, Idempotency & Reliability Validation

## 1. Clean Target Node Provisioning
The target node (`daap-target`) was provisioned from a fresh Ubuntu 22.04 base:
- Container Name: `daap-target`
- Image: `daap-target-img`
- Network: `daap-net`
- Port Mapping: `0.0.0.0:8083->8083/tcp`
- SSH Daemon: active on internal port 22

---

## 2. First Execution Recap
The initial run configured all OS packages, created users and directories, copied the WAR artifact, configured supervisor, and validated the health check:
```
PLAY RECAP *********************************************************************
daap-target                : ok=17   changed=11   unreachable=0    failed=0    skipped=0    rescued=0    ignored=0   
```
* Evidence: [`docs/evidence/ansible_first_run.txt`](file:///c:/Users/prash/Desktop/DevOps/DevOps-Project/docs/evidence/ansible_first_run.txt)

---

## 3. Idempotency Test
The exact same playbook (`site.yml`) was executed a second time against the running node without configuration changes.

### Result
```
PLAY RECAP *********************************************************************
daap-target                : ok=16   changed=0    unreachable=0    failed=0    skipped=0    rescued=0    ignored=0   
```
* **Idempotency Status**: **VERIFIED** (`changed=0`)
* **Rationale**:
  - Packages are already present (`state: present`).
  - System group and user exist (`state: present`).
  - Directory modes and ownership already match `0755` and `daap:daap`.
  - Artifact and properties file checksums match, avoiding file re-transfers.
  - Symlink `current` already points to the active version.
  - Supervisor config matches, so no handler reloads were triggered.
* Evidence: [`docs/evidence/ansible_idempotency_run.txt`](file:///c:/Users/prash/Desktop/DevOps/DevOps-Project/docs/evidence/ansible_idempotency_run.txt)

---

## 4. Real Health Check Verification
Verification performed directly from the host environment:
- **Health Check URL**: `http://localhost:8083/api/health`
  - HTTP Status: `200 OK`
  - Body: `"Application is running."`
- **Application Portal URL**: `http://localhost:8083/`
  - HTTP Status: `200 OK`
  - Title: `"Digital Asset Approval Platform"`
* Evidence: [`docs/evidence/ansible_health_check.txt`](file:///c:/Users/prash/Desktop/DevOps/DevOps-Project/docs/evidence/ansible_health_check.txt)

---

## 5. Controlled Failure & Automated Rollback Demonstration

### 5.1 Simulated Failure Scenario
A corrupted release (`1.1.0-corrupt`) was deployed with an invalid executable artifact and pointed as `current`.
- When restarted, supervisor logged `daap: ERROR (spawn error)`.
- HTTP health checks to `http://localhost:8083/api/health` immediately timed out / refused connections.
- The previous release directory `/opt/daap/releases/1.0.9/app.war` remained completely intact.

### 5.2 Automated Rollback Execution (`rollback.yml`)
The rollback playbook was executed to restore the known working stable release (`1.0.9`):
```bash
docker run --rm --network daap-net \
  -v "${PWD}/ansible:/ansible" \
  -v "${PWD}/target:/target" \
  daap-ansible-img -i inventory.ini rollback.yml -e "rollback_version=1.0.9"
```

### 5.3 Rollback Result
```
PLAY [Rollback Application to Previous Stable Release] *************************

TASK [Gathering Facts] *********************************************************
ok: [daap-target]

TASK [1. Verify target rollback release directory exists] **********************
ok: [daap-target]

TASK [2. Fail if specified rollback release is missing] ************************
skipping: [daap-target]

TASK [3. Verify target release has valid application artifact] *****************
ok: [daap-target]

TASK [4. Fail if application artifact in rollback release is missing] **********
skipping: [daap-target]

TASK [5. Atomically point current symlink to rollback release] *****************
changed: [daap-target]

TASK [6. Restart application process via supervisor] ***************************
changed: [daap-target]

TASK [7. Wait for application port 8083 to become available] *******************
ok: [daap-target]

TASK [8. Verify health endpoint after rollback] ********************************
ok: [daap-target]

TASK [9. Report successful rollback confirmation] ******************************
ok: [daap-target] => {
    "msg": "Rollback SUCCESS: Active release restored to 1.0.9. Health check: Application is running."
}

PLAY RECAP *********************************************************************
daap-target                : ok=8    changed=2    unreachable=0    failed=0    skipped=2    rescued=0    ignored=0   
```
* **Status**: **VERIFIED** — Application immediately recovered to healthy state on port 8083.
* Evidence: [`docs/evidence/ansible_rollback_run.txt`](file:///c:/Users/prash/Desktop/DevOps/DevOps-Project/docs/evidence/ansible_rollback_run.txt)

---

## 6. Final Healthy State
- Target Node: `daap-target` running and active.
- Port: `8083` listening and serving `HTTP 200`.
- Health Endpoint: `http://localhost:8083/api/health` returns `"Application is running."`.
