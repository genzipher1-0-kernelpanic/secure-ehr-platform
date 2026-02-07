# Secure EHR Platform

This repository contains a comprehensive, secure Electronic Health Record (EHR) platform built on a microservices architecture. It demonstrates best practices for building robust, scalable, and secure healthcare applications, including role-based access control, end-to-end data encryption, and tamper-evident audit logging.

## Architecture

The platform is designed as a collection of decoupled microservices, each with a specific responsibility. This promotes scalability, fault tolerance, and independent development.

- **`api-gateway`**: The single entry point for all client requests. It handles routing, JWT validation, CORS, request aggregation, and other cross-cutting concerns.
- **`identity-service`**: Manages user authentication and authorization. It handles user registration, login, role management, and the issuance of JSON Web Tokens (JWTs).
- **`config-server`**: Provides centralized configuration management for all microservices using Spring Cloud Config.
- **`discovery-server`**: A Eureka server that allows services to register themselves and discover others.
- **`care-service`**: Manages the relationships and administrative aspects of patient care, such as doctor-patient assignments and access consents.
- **`ehr-service`**: The core service for managing patient health records. It handles creation, versioning, and secure storage of encrypted EHR data and lab files.
- **`audit-service`**: A critical security component that consumes events from a Kafka topic to create a tamper-evident, hash-chained log of all significant activities in the system. It also includes an alerting engine for suspicious behavior.
- **`frontend`**: A React-based single-page application that provides user interfaces for different roles, including doctors, receptionists, and administrators.
---
```mermaid
graph TB
    subgraph "Client Layer"
        UI[Web App SPA<br/>Login, Admin, Dashboards]
    end

    subgraph "Edge Layer"
        GW[API Gateway<br/>CORS, Throttling<br/>JWT Forwarding<br/>Request Correlation]
    end

    subgraph "Identity & Auth"
        AUTH[auth-service<br/>JWT Issuance<br/>Login, Refresh, MFA<br/>Roles: PATIENT/DOCTOR/ADMIN<br/>Admin Types: ADMIN0/1/2]
    end

    subgraph "Business Services"
        CARE[care-service<br/>Patient Assignments<br/>Event Publisher]
        EHR[ehr-service<br/>EHR Records & Storage<br/>DB Migrations]
        AUDIT[audit-service<br/>Kafka Consumer<br/>Hash Chaining<br/>Alert Detection<br/>Integrity Verification]
    end

    subgraph "Data Stores"
        MYSQL_AUTH[(MySQL<br/>auth_db<br/>Credentials)]
        MYSQL_CARE[(MySQL<br/>care_db<br/>:3308)]
        MYSQL_EHR[(MySQL<br/>ehr_db<br/>:3309)]
        MYSQL_AUDIT[(MySQL<br/>audit_db<br/>:3307<br/>audit_event<br/>alert<br/>integrity_check_run)]
        BLOB[Object Store<br/>Optional<br/>Large EHR Artifacts]
    end

    subgraph "Message Bus"
        KAFKA{Apache Kafka}
        subgraph "Topics"
            T_AUDIT[audit-events<br/>1 partition<br/>30d retention]
            T_DLT[audit-events.DLT<br/>Dead Letter]
            T_ALERTS[alerts<br/>3 partitions]
            T_USER[user-registered]
            T_PATIENT[patient-assign]
        end
    end

    subgraph "Infrastructure & Ops"
        ADMINER[Adminer<br/>DB Admin UI]
        KAFKAUI[Kafka UI<br/>Topic Monitoring]
        ZK[Zookeeper<br/>Kafka Coordination]
        INIT[kafka-init<br/>Topic Creation]
    end

    subgraph "Future/Planned"
        NOTIF[notification-service<br/>Alert Consumer<br/>Webhooks, Push]
    end

    subgraph "Monitoring Stack"
        PROM[Prometheus<br/>Metrics]
        GRAF[Grafana<br/>Dashboards]
        LOGS[ELK/EFK<br/>Centralized Logs]
    end

    %% Client to Gateway
    UI -->|HTTPS/TLS<br/>JWT Token| GW

    %% Gateway to Services
    GW -->|JWT Auth| AUTH
    GW -->|JWT Auth| CARE
    GW -->|JWT Auth| EHR
    GW -->|Admin Only<br/>RBAC| AUDIT

    %% Services to Databases
    AUTH -.->|Credentials<br/>Hashed| MYSQL_AUTH
    CARE -.->|Patient Data| MYSQL_CARE
    EHR -.->|EHR Records| MYSQL_EHR
    EHR -.->|Large Files| BLOB
    AUDIT -.->|Hash Chain<br/>Alerts<br/>Integrity| MYSQL_AUDIT

    %% Event Publishing
    CARE ==>|patient-assign<br/>user-registered<br/>audit-events| KAFKA
    AUTH ==>|audit-events| KAFKA
    EHR ==>|audit-events| KAFKA
    AUDIT ==>|AlertMessage| KAFKA

    %% Kafka Topics
    KAFKA --> T_AUDIT
    KAFKA --> T_DLT
    KAFKA --> T_ALERTS
    KAFKA --> T_USER
    KAFKA --> T_PATIENT

    %% Event Consumption
    T_AUDIT -.->|KafkaListener<br/>Concurrency=1<br/>Manual Ack| AUDIT
    T_USER -.->|Normalize| AUDIT
    T_PATIENT -.->|Normalize| AUDIT
    T_DLT -.->|Poison Messages| AUDIT
    T_ALERTS -.->|Future| NOTIF

    %% Infrastructure Dependencies
    ZK -.->|Coordination| KAFKA
    INIT -.->|Create Topics| KAFKA
    ADMINER -.->|Admin Access| MYSQL_CARE
    ADMINER -.->|Admin Access| MYSQL_EHR
    ADMINER -.->|Admin Access| MYSQL_AUDIT
    KAFKAUI -.->|Monitor| KAFKA

    %% Monitoring
    AUTH -.->|Metrics| PROM
    CARE -.->|Metrics| PROM
    EHR -.->|Metrics| PROM
    AUDIT -.->|Metrics| PROM
    PROM -.->|Visualize| GRAF
    AUTH -.->|Logs| LOGS
    CARE -.->|Logs| LOGS
    EHR -.->|Logs| LOGS
    AUDIT -.->|Logs| LOGS

    %% Styling
    classDef client fill:#4A90E2,stroke:#2E5C8A,stroke-width:3px,color:#FFFFFF
    classDef edge fill:#F39C12,stroke:#D68910,stroke-width:3px,color:#000000
    classDef service fill:#27AE60,stroke:#1E8449,stroke-width:3px,color:#FFFFFF
    classDef auth fill:#E74C3C,stroke:#C0392B,stroke-width:3px,color:#FFFFFF
    classDef db fill:#8E44AD,stroke:#6C3483,stroke-width:3px,color:#FFFFFF
    classDef kafka fill:#E67E22,stroke:#CA6F1E,stroke-width:3px,color:#FFFFFF
    classDef infra fill:#34495E,stroke:#2C3E50,stroke-width:3px,color:#FFFFFF
    classDef monitor fill:#F1C40F,stroke:#D4AC0D,stroke-width:3px,color:#000000
    classDef future fill:#BDC3C7,stroke:#95A5A6,stroke-width:2px,stroke-dasharray: 5 5,color:#2C3E50

    class UI client
    class GW edge
    class AUTH auth
    class CARE,EHR service
    class AUDIT service
    class MYSQL_AUTH,MYSQL_CARE,MYSQL_EHR,MYSQL_AUDIT,BLOB db
    class KAFKA,T_AUDIT,T_DLT,T_ALERTS,T_USER,T_PATIENT kafka
    class ADMINER,KAFKAUI,ZK,INIT infra
    class PROM,GRAF,LOGS monitor
    class NOTIF future
```
### Core Technologies

- **Backend**: Java 21, Spring Boot 3, Spring Cloud
- **Frontend**: React, Vite, Tailwind CSS
- **Data Persistence**: MySQL, Redis
- **Messaging**: Apache Kafka
- **Security**: Spring Security, JWT
- **Infrastructure**: Docker
## Key Features

### Security & Compliance

- **Role-Based Access Control (RBAC)**: Fine-grained access control based on user roles (e.g., `SUPER_ADMIN`, `SYSTEM_ADMIN`, `DOCTOR`, `PATIENT`). The `identity-service` embeds the user's role into the JWT, which is then used by downstream services to enforce permissions.
- **End-to-End Encryption**:
    - **EHR Data**: Patient records are encrypted at the application layer using AES/GCM before being stored in the database. The `ehr-service` manages an envelope encryption scheme with a master key.
    - **Lab Files**: Binary files (e.g., lab reports, X-rays) are encrypted using a unique data key for each file, which is then encrypted with the master key.
- **Tamper-Evident Auditing**: All critical actions (logins, data access, record updates) are published as events to a Kafka topic. The `audit-service` consumes these events and stores them in a database, creating a cryptographic hash chain. Each new log entry's hash is computed using the hash of the previous entry, making it computationally infeasible to tamper with logs without breaking the chain.
- **Security Alerting**: The `audit-service` continuously monitors the event stream for suspicious patterns, such as:
    - Multiple failed login attempts.
    - Bulk data access or export.
    - Unauthorized access attempts.
    - High-privilege administrative actions.
- **Secure Authentication**: Uses JWTs for stateless authentication. Includes refresh tokens and secure token storage. Also features session inactivity timeouts managed via Redis.

### Data Management

- **EHR Versioning**: Every modification to a patient's record creates a new, immutable version in the `ehr_record_version` table. This provides a complete, auditable history of changes.
- **Optimistic Locking**: The `ehr_record_current` table uses a `@Version` field to prevent race conditions and ensure data consistency during concurrent updates.
- **Secure File Storage**: Lab results and other sensitive files are stored encrypted on the file system, not directly in the database. The path and encryption key details are managed by the `ehr-service`.

## Getting Started

### Prerequisites

- Java 21
- Maven 3.8+
- Docker and Docker Compose
- Node.js 18+ and npm

### 1. Infrastructure Setup

The project uses Docker to manage its core infrastructure components.

**Start Databases and Caching:**
This command will start MySQL instances for each service, Redis for caching and session management, and Zipkin for distributed tracing.

```bash
cd docker
docker-compose up -d
```

**Start Kafka:**
The audit service relies on Apache Kafka. A separate Docker Compose file is provided to set it up.

```bash
cd backend/audit-service
docker-compose -f docker-compose.kafka.yml up -d
```
This will start Zookeeper, Kafka, and a Kafka-UI instance accessible at `http://localhost:8090`.

**Initialize Databases:**
SQL scripts for creating the necessary databases and tables are located in `infra/mysql-init/`. You can run these manually using a MySQL client connected to the respective ports (3307 for `identity-db`, 3308 for `care-db`, etc.).

### 2. Run Backend Services

Backend services must be started in a specific order due to dependencies on the Config Server and Discovery Server. For each service, navigate to its directory and run the Maven command.

1.  **Config Server**: `config-server`
2.  **Discovery Server**: `discovery-server`
3.  **Identity Service**: `identity-service`
4.  **Care Service**: `backend/care-service`
5.  **EHR Service**: `backend/ehr-service`
6.  **Audit Service**: `backend/audit-service`
7.  **API Gateway**: `backend/api-gateway`

**Example for starting the Config Server:**
```bash
cd config-server
mvn spring-boot:run
```
Repeat this process for all other services in the correct order.

### 3. Run Frontend Application

Navigate to the frontend directory, install dependencies, and start the development server.

```bash
cd frontend/web-app
npm install
npm run dev
```

The application will be available at `http://localhost:5173`.

### Default Credentials

The `identity-service` automatically creates a `SUPER_ADMIN` user on its first run. The credentials are an important part of the configuration:
- **Email**: `superadmin@genzipher.com`
- **Password**: `SuperAdmin@20260702`

These are defined in `identity-service/src/main/java/com/genzipher/identityservice/Bootstrap/SuperAdminInitializer.java` and can be overridden via `config-server`. It is highly recommended to change this password immediately after the first login. New users (System Admins, Doctors) are created with a secure, randomly generated temporary password that is displayed upon creation.
