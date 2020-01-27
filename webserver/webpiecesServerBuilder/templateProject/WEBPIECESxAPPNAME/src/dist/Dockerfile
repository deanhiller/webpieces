FROM openjdk:8-jdk-alpine
RUN mkdir -p ./webpieces
COPY . ./webpieces/
COPY config/logback.cloudrun.xml ./webpieces/config/logback.xml
WORKDIR "/webpieces"
ENTRYPOINT ["./bin/WEBPIECESxAPPNAME"]
