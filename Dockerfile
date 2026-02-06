#FROM jvpdixa/alpine-java8
FROM 038101219289.dkr.ecr.eu-west-1.amazonaws.com/alpine-java8

COPY target/universal/stage /app
RUN chmod u+x /app/bin/start

EXPOSE 7001

ENTRYPOINT ["/app/bin/start"]

