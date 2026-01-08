# ============================================
# Stage 1: Build Stage
# ============================================
FROM gradle:8.14-jdk17 AS builder

WORKDIR /app

# Gradle 캐시 최적화를 위해 의존성 파일 먼저 복사
COPY build.gradle settings.gradle ./
COPY gradle ./gradle

# 의존성 다운로드 (캐시 레이어)
RUN gradle dependencies --no-daemon || true

# 소스 코드 복사 및 빌드
COPY src ./src
RUN gradle bootJar --no-daemon -x test

# ============================================
# Stage 2: Runtime Stage
# ============================================
FROM eclipse-temurin:17-jre

WORKDIR /app

# 보안: non-root 사용자 생성
RUN groupadd -g 1001 appgroup && \
    useradd -u 1001 -g appgroup -s /bin/sh appuser

# 빌드된 JAR 복사
COPY --from=builder /app/build/libs/proj-objection-0.0.1-SNAPSHOT.jar app.jar

# 소유권 변경
RUN chown appuser:appgroup app.jar

# non-root 사용자로 전환
USER appuser

# 포트 노출
EXPOSE 8080

# JVM 최적화 옵션과 함께 실행
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "app.jar"]
