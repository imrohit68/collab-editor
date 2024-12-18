FROM openjdk:17-jdk-slim
COPY target/collab-editor-0.0.1-SNAPSHOT.jar collab-editor-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java","-jar","/collab-editor-0.0.1-SNAPSHOT.jar"]