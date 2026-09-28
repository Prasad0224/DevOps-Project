# Apache Tomcat Setup Guide

## 1. Version Requirement
* **Apache Tomcat 10.1.x** (Required for Spring Boot 3 / Jakarta EE 10 / Servlet 6.0).
* *Note: Tomcat 9 or earlier will fail with `javax.servlet` vs `jakarta.servlet` class mismatches.*

## 2. Ports & Paths
* **Recommended HTTP Port**: `8081` (Use `8081` if Jenkins occupies `8080`).
  * In `conf/server.xml`:
    ```xml
    <Connector port="8081" protocol="HTTP/1.1" connectionTimeout="20000" redirectPort="8443" />
    ```
* **Webapps Path**: `<TOMCAT_HOME>/webapps/`
* **WAR Deployment Path**: Copy `target/digital-asset-approval-platform.war` into `<TOMCAT_HOME>/webapps/digital-asset-approval-platform.war`
* **Application URL**: `http://localhost:8081/digital-asset-approval-platform`

## 3. Environment Variables
* `CATALINA_HOME`: Path to Tomcat directory (e.g., `C:\apache-tomcat-10.1.20`)
* `JAVA_HOME`: Path to JDK 17 (e.g., `C:\Program Files\Java\jdk-17`)

## 4. Commands (Windows)
* **Start Tomcat**:
  ```cmd
  %CATALINA_HOME%\bin\startup.bat
  ```
* **Stop Tomcat**:
  ```cmd
  %CATALINA_HOME%\bin\shutdown.bat
  ```
