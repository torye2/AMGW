# Java 21 기반 이미지 사용
FROM openjdk:21-jdk-slim

# 애플리케이션 실행 폴더
WORKDIR /app

# JAR 파일 복사 (빌드 후 생성된 jar 파일)
COPY build/libs/*.jar app.jar

# 포트 열기
EXPOSE 8080

# 실행 명령
ENTRYPOINT ["java", "-jar", "app.jar"]
