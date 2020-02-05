FROM openjdk:11-jre-slim

COPY app/ /app

EXPOSE 8080

WORKDIR /app
ENTRYPOINT ["/app/bin/database-tools"]
