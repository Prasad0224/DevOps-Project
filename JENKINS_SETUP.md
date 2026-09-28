# Jenkins Pipeline Setup Guide

## 1. Required Jenkins Plugins
Ensure these plugins are installed in Jenkins (*Manage Jenkins > Plugins > Installed Plugins*):
* **Pipeline** (workflow-aggregator)
* **Git Plugin**
* **JUnit Plugin** (for publishing test results)

## 2. Job Creation & Configuration

* **Job Name**: `digital-asset-approval-platform-pipeline`
* **Job Type**: `Pipeline`

### Pipeline Definition:
* **Definition**: `Pipeline script from SCM`
* **SCM**: `Git`
* **Repository URL**: `https://github.com/Prasad0224/DevOps-Project.git`
* **Branch Specifier**: `*/develop` (or `*/main`)
* **Script Path**: `Jenkinsfile`

### Build Triggers:
* Check **Poll SCM**
* **Schedule**: `H/5 * * * *`

### Parameters (Initialized on first run from Jenkinsfile):
* `DEPLOY_ENV` (Choice: `test`, `staging`, `production`) — default `test`
* `TOMCAT_HOST` (String) — default `localhost`
* `TOMCAT_PORT` (String) — default `8080` (or `8081` if Jenkins is on `8080`)
* `TOMCAT_CONTEXT_PATH` (String) — default `digital-asset-approval-platform`
* `TOMCAT_WEBAPPS_DIR` (String) — optional explicit path to Tomcat `webapps/` folder
* `RUN_SELENIUM_E2E` (Boolean) — default `true`
