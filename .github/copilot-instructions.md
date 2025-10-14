# Copilot Instructions for Voiceprint

## Project Overview
- **Voiceprint** is a multi-service, AI-powered voice diary platform. It uses a microservices architecture with Spring Cloud (Java), React (TypeScript), FastAPI (Python), and integrates with Redis, MySQL, Kafka, and AWS S3.
- The system supports real-time voice chat, STT/LLM/TTS pipelines, diary generation, group sharing, and notification features.

## Architecture & Key Components
- **Backend (Spring Boot, Java)**: Main business logic, REST APIs, WebSocket for real-time chat, JWT-based auth, Redis for session/message, Kafka for event streaming, S3 for storage.
  - Located in `services/backend-msa` (main), with shared modules in `services/common-auth`.
  - Other microservices: `config-service`, `discovery-service`, `gateway-service`, `notification-service` (each in `services/`).
- **Frontend (React, Vite, TypeScript)**: UI, state management (Redux), API integration via `src/api/axiosInstance.ts` (handles JWT, auto-refresh).
  - Located in `frontend/`.
- **AI/Voice (FastAPI, Python)**: Handles STT, LLM, TTS, and prompt logic (not in this repo, but referenced in architecture).
- **Infra**: Docker Compose for local orchestration (`docker-compose.yml`), Prometheus/Grafana for monitoring, Jenkins for CI/CD.

## Developer Workflows
- **Build & Run (Local, All Services)**:
  - `docker-compose up --build` (from project root) spins up all services (frontend, backend, MySQL, Redis, etc.).
  - Each service has its own Dockerfile and `.env` file for configuration.
- **Backend (Spring Boot)**:
  - Build: `./gradlew build` in each service directory.
  - Run locally: `./gradlew bootRun` (e.g., in `services/backend-msa`).
  - Test: `./gradlew test` (JUnit, see `build.gradle`).
- **Frontend**:
  - Dev: `npm run start` in `frontend/` (Vite dev server).
  - Build: `npm run build`.
  - Lint: `npm run lint`.
- **Database**: MySQL config/scripts in `mysql/` and `mysql_notification/`.
- **Monitoring**: Prometheus config in `monitoring/prometheus.yml`, Grafana dashboards in `grafana/`.

## Project-Specific Conventions
- **Branch Naming**: `type/JIRA-issue-desc/{front|back}` (see README for full rules).
- **Backend Structure**: Follows DDD-style layering (domain, adapter, application, global/config, etc.).
- **Auth**: JWT via `common-auth`, auto-refresh in frontend (`axiosInstance.ts`).
- **WebSocket**: `/ws` endpoint, with custom auth interceptor (`WebSocketAuthInterceptor`).
- **Redis**: Used for session, pub/sub (see `RedisConfig.java` in backend and notification-service).
- **API Base URLs**: Set via environment variables (see `.env` files, `VITE_API_BASE_URL` for frontend).

## Integration & Communication
- **Service Discovery**: Eureka (`discovery-service`).
- **Config**: Centralized via `config-service` (Spring Cloud Config).
- **Gateway**: API gateway in `gateway-service` (Spring Cloud Gateway).
- **Notifications**: Redis pub/sub, notification microservice, SSE for real-time delivery.
- **AI Integration**: Backend calls out to FastAPI-based AI server for LLM/STT/TTS.

## Examples & References
- See `services/backend-msa/src/main/java/com/voiceprint/backend/global/config/` for config patterns (security, WebSocket, Redis, etc.).
- See `frontend/src/api/axiosInstance.ts` for API/JWT handling.
- See `docker-compose.yml` for service orchestration and environment setup.

---

**When in doubt, check the README and service-specific directories for more details.**
