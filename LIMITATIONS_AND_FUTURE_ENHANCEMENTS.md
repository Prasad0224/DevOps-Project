# Architectural Limitations and Future Enhancements

## 1. Current Architecture Limitations

1. **Single-Host Environment**:
   - All components (Jenkins orchestrator, Tomcat servlet container, Docker engine, Local Docker registry, and Ansible target node) execute on a single physical development machine.
   - Resource contention (RAM/CPU) can influence startup and testing durations.

2. **Local Docker Registry**:
   - Uses an ephemeral local registry (`localhost:5000`) rather than a managed cloud registry (e.g., AWS ECR, Azure ACR, or Docker Hub).
   - Images are accessible only locally and do not support geo-replication or automated vulnerability scanning.

3. **Standalone / Isolated Target Container**:
   - The Ansible target node is simulated in an isolated Ubuntu Docker container (`daap-target`) rather than a bare-metal server or cloud VM (AWS EC2 / Azure VM).

4. **Lack of High Availability (HA) & Clustering**:
   - Single container/instance points of failure for Tomcat, Docker, and Ansible nodes; no auto-scaling or load-balancing reverse proxies (e.g., NGINX, HAProxy).

5. **Basic Security & Credential Storage**:
   - Application secrets and database state are maintained in memory / local properties files rather than an enterprise secrets store (HashiCorp Vault, AWS Secrets Manager).
   - HTTP transport is used instead of TLS/HTTPS certificates across all ports (8080, 8081, 8082, 8083, 5000).

6. **In-Memory Storage**:
   - The Digital Asset Approval Platform backend uses an in-memory repository structure for requests and audit logs; restarts clear volatile state.

---

## 2. Future Enhancements & Production Roadmap

1. **Container Orchestration with Kubernetes**:
   - Migrate Docker container and Ansible node workloads to a managed Kubernetes cluster (EKS/GKE/AKS).
   - Deploy as `Deployments` with `HorizontalPodAutoscaler` (HPA), `ConfigMaps`, and `Secrets`.

2. **Cloud Registry & Automated Vulnerability Scanning**:
   - Transition from local registry to cloud registries with integrated Trivy / Aqua / Clair container vulnerability scanning.

3. **Infrastructure as Code (IaC) with Terraform**:
   - Automate provisioning of underlying cloud virtual networks, subnets, VM instances, and IAM roles using modular Terraform scripts before Ansible configuration.

4. **Zero-Downtime Deployment Strategies**:
   - Implement **Blue/Green** or **Canary** deployment patterns using NGINX Ingress or Istio service meshes, allowing instant automated rollback upon health check regression.

5. **Enterprise Secrets Management**:
   - Integrate HashiCorp Vault with Jenkins and Ansible via plugins to dynamically inject sensitive database credentials and certificates.

6. **Monitoring, Metrics & Observability**:
   - Implement Prometheus and Grafana dashboards tracking JVM metrics (`micrometer-registry-prometheus`), container CPU/memory usage, and response latencies.
   - Centralize logs using an ELK/EFK stack (Elasticsearch, Fluent Bit, Kibana) or Loki.

7. **End-to-End TLS / HTTPS Encryption**:
   - Enforce HTTPS across all public endpoints with automated Let's Encrypt / Cert-Manager SSL/TLS certificates.
