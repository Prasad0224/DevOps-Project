FROM eclipse-temurin:17-jre

WORKDIR /app

COPY target/digital-asset-approval-platform.war app.war

RUN mkdir -p /app/uploads && chmod 777 /app/uploads

VOLUME ["/app/uploads"]

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.war"]
