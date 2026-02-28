FROM openjdk:27-ea
ADD target/gotcha-api.jar gotcha-api.jar
ENTRYPOINT ["java", "-jar", "gotcha-api.jar"]