# syntax=docker/dockerfile:1
# =============================================================================
# BeautyManager API - Imagen multi-stage
# =============================================================================
# Stage 1 (build): compila con Maven usando el wrapper del proyecto.
# Stage 2 (runtime): solo el JRE + el jar, sin toolchain de build.
# =============================================================================

# ---------- Stage 1: build ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

# Primero solo el pom: si las dependencias no cambian, la capa se cachea.
COPY pom.xml ./
RUN --mount=type=cache,target=/root/.m2 mvn -B -q dependency:go-offline

# Ahora el codigo y se empaqueta (skip tests: se corren en el pipeline/CI).
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -B -q clean package -DskipTests

# ---------- Stage 2: runtime ----------
FROM eclipse-temurin:21-jre-jammy AS runtime

# Usuario sin privilegios: la app no corre como root.
RUN groupadd --system --gid 1001 appgroup \
 && useradd --system --uid 1001 --gid appgroup --create-home --home-dir /app appuser

WORKDIR /app

# Se copia el jar desde la etapa de build.
COPY --from=build /build/target/*.jar app.jar

# --- Endpoints ---
# 8082: HTTP de la API
# 5005: JMX / DevTools remoto (opcional, ver compose)
EXPOSE 8082

USER appuser

# JVMflags para contenedor: respeta limites de memoria del host.
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -XX:+UseContainerSupport"

# Actuator/health sirve como healthcheck del contenedor.
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD ["sh", "-c", "curl -fsS http://localhost:8082/actuator/health || exit 1"]

# exec form con sh para poder expanding JAVA_OPTS sin requerir un entrypoint.
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar \"$@\"", "--"]
