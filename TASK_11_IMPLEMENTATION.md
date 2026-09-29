# Task 11 Implementation: Docker Image and Container Lifecycle

## 1. Dockerfile & Base Image
* **File**: `Dockerfile`
* **Base Image**: `eclipse-temurin:17-jre` (Official Eclipse Temurin Java 17 JRE)
* **Configuration**:
  ```dockerfile
  FROM eclipse-temurin:17-jre
  WORKDIR /app
  COPY target/digital-asset-approval-platform.war app.war
  EXPOSE 8080
  ENTRYPOINT ["java", "-jar", "app.war"]
  ```
* **.dockerignore**: Excludes `.git`, `.idea`, build test caches, screenshots, and documentation, preserving `target/*.war`.

---

## 2. Image Name & Versioning
* **Image Name**: `digital-asset-approval-platform`
* **Versioned Tag**: `1.0.0`
* **Aliases**: `digital-asset-approval-platform:1.0.0`, `digital-asset-approval-platform:latest`
* **Local Registry Image**: `localhost:5000/digital-asset-approval-platform:1.0.0`, `localhost:5000/digital-asset-approval-platform:latest`

---

## 3. Build & Run Commands
* **Build Command**:
  ```bash
  docker build -t digital-asset-approval-platform:1.0.0 -t digital-asset-approval-platform:latest .
  ```
* **Run Command**:
  ```bash
  docker run -d --name digital-asset-approval-platform -p 8082:8080 digital-asset-approval-platform:1.0.0
  ```
* **Port Mapping**: Host Port `8082` -> Container Port `8080`

---

## 4. Container Lifecycle Evidence

### 4.1 docker ps Evidence
```
CONTAINER ID   IMAGE                                   COMMAND               CREATED          STATUS          PORTS                                         NAMES
916558e38495   digital-asset-approval-platform:1.0.0   "java -jar app.war"   12 seconds ago   Up 12 seconds   0.0.0.0:8082->8080/tcp, [::]:8082->8080/tcp   digital-asset-approval-platform
```

### 4.2 docker logs Evidence (Spring Boot Startup)
```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.1.2)

2026-09-29T02:02:35.164Z  INFO 1 --- [           main] c.p.DigitalAssetApprovalApplication      : Starting DigitalAssetApprovalApplication v0.0.1-SNAPSHOT using Java 17.0.20.1 with PID 1 (/app/app.war started by root in /app)
2026-09-29T02:02:36.431Z  INFO 1 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port(s): 8080 (http)
2026-09-29T02:02:36.997Z  INFO 1 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8080 (http) with context path ''
2026-09-29T02:02:37.015Z  INFO 1 --- [           main] c.p.DigitalAssetApprovalApplication      : Started DigitalAssetApprovalApplication in 2.408 seconds (process running for 3.087)
```

### 4.3 Container Inspect Command & Evidence
* **Command**:
  ```bash
  docker inspect digital-asset-approval-platform --format "{{.Id}} State={{.State.Status}} Running={{.State.Running}} Ports={{.NetworkSettings.Ports}}"
  ```
* **Output**:
  ```
  916558e38495d90206c6587637e360b2086db9b804285984694fd859bf216252 State=running Running=true Ports=map[8080/tcp:[map[HostIp:0.0.0.0 HostPort:8082] map[HostIp::: HostPort:8082]]]
  ```

### 4.4 Stop, Start/Restart, and Remove Commands
* **Stop**:
  ```bash
  docker stop digital-asset-approval-platform
  ```
* **Start / Restart**:
  ```bash
  docker start digital-asset-approval-platform
  ```
* **Stop & Remove**:
  ```bash
  docker stop digital-asset-approval-platform
  docker rm digital-asset-approval-platform
  ```

---

## 5. Local Docker Registry (Port 5000)

### 5.1 Local Registry Setup
* **Command**:
  ```bash
  docker run -d --restart unless-stopped --name daap-registry -p 5000:5000 registry:2
  ```
* **Verification**:
  ```
  CONTAINER ID   IMAGE        STATUS         PORTS                    NAMES
  0e071348ffdb   registry:2   Up 2 minutes   0.0.0.0:5000->5000/tcp   daap-registry
  ```

### 5.2 Registry Push Commands
```bash
docker tag digital-asset-approval-platform:1.0.0 localhost:5000/digital-asset-approval-platform:1.0.0
docker tag digital-asset-approval-platform:latest localhost:5000/digital-asset-approval-platform:latest
docker push localhost:5000/digital-asset-approval-platform:1.0.0
docker push localhost:5000/digital-asset-approval-platform:latest
```

### 5.3 Registry Verification Evidence (HTTP API)
* **Catalog Query**: `GET http://localhost:5000/v2/_catalog`
  ```json
  {
    "repositories": ["digital-asset-approval-platform"]
  }
  ```
* **Tag Query**: `GET http://localhost:5000/v2/digital-asset-approval-platform/tags/list`
  ```json
  {
    "name": "digital-asset-approval-platform",
    "tags": ["1.0.0", "latest"]
  }
  ```

---

## 6. Final Clean Running Container
* **Launch Command**:
  ```bash
  docker run -d --name digital-asset-approval-platform -p 8082:8080 localhost:5000/digital-asset-approval-platform:1.0.0
  ```
* **Status**: `Up (healthy)`
* **Active URLs**:
  - Application UI: [http://localhost:8082](http://localhost:8082) (`HTTP 200 OK`)
  - Health Check: [http://localhost:8082/api/health](http://localhost:8082/api/health) (`Application is running.`)
