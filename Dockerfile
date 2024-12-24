FROM node:14.10.1-buster-slim AS npmBuild
COPY client /client
WORKDIR /client
RUN npm --version
RUN npm install
RUN npm run build

FROM maven:3.9.9-amazoncorretto-21 AS build
COPY pom.xml .
COPY app /app/
COPY app-common/ /app-common/
COPY app-task/ /app-task/
COPY --from=npmBuild client/out/static/ /client/out/static/
COPY --from=npmBuild client/pom.xml /client/pom.xml
COPY --from=npmBuild client/assembly.xml /client/assembly.xml
# RUN mvn dependency:go-offline
# skip exec stage -- do not call npm build again in client since node is not available here.
RUN mvn -e -X -Dexec.skip clean package

FROM amazoncorretto:11.0.18
COPY --from=build app/target/accrptgen.jar accrptgen.jar
EXPOSE 8080
# $0, or ${0}, is the name of the script being executed
# The variable $@ is the array of all the input parameters. Using this variable within a for loop, we can iterate over the input and process all the arguments passed.
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -Xlog:gc*:file=gc.log -XX:MaxRAMPercentage=50 -jar accrptgen.jar ${0} ${@}"]

# map local volumes (directory), environment and run
# docker run -p 8080:8080 -it -v ${PWD}/local/:/local/ -e JAVA_OPTS=-Dspring.profiles.active=local accrptgen --storage.persistent.folder=/local/files/ --storage.temp.folder=/local/temp/