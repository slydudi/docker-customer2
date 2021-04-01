FROM openjdk:8-jre-alpine3.9
WORKDIR /customer
COPY target/customer-0.0.1-SNAPSHOT.jar /demo.jar
CMD ["java", "-jar", "/demo.jar"]