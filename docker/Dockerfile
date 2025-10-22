# --- builder: gradle 내장 ---
FROM gradle:8.10.2-jdk21 AS builder
WORKDIR /workspace

# AMGW 전체 복사 (build.context가 AMGW 폴더여야 함)
COPY . .

RUN gradle -Dorg.gradle.daemon=false clean bootJar

# 정적 리소스가 JAR에 들어갔는지 확인(없으면 실패)
RUN jar tf build/libs/*.jar | grep -E 'BOOT-INF/classes/static/.+' | head -n 20 \
 && jar tf build/libs/*.jar | grep 'BOOT-INF/classes/static/css/styles.css'

# --- runtime ---
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /workspace/build/libs/*.jar /app/app.jar
EXPOSE 8081
ENTRYPOINT ["java","-jar","/app/app.jar"]
