####
# This Dockerfile is used in order to build a container that runs the Quarkus application in native (no JVM) mode
#
# Before building the docker image run:
#
# mvn package -Pnative -Dnative-image.docker-build=true
#
# Then, build the image with:
#
# docker build -t 2021-leaderboard-service .
#
# Then run the container using:
#
# docker run -i --rm -p 8081:8081 2021-leaderboard-service
#
###

FROM registry.fedoraproject.org/fedora-minimal
WORKDIR /work/
COPY target/*-runner /work/application
EXPOSE 8081
CMD ["./application", "-Dquarkus.http.host=0.0.0.0"]