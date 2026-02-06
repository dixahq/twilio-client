#FROM jvpdixa/alpine-java8
FROM 038101219289.dkr.ecr.eu-west-1.amazonaws.com/openjdk/java-25-amazon-corretto-jdk:25.0.1-al2023-2

COPY target/universal/stage /app
RUN chmod u+x /app/bin/start

EXPOSE 7001

ENTRYPOINT ["/app/bin/start"]

