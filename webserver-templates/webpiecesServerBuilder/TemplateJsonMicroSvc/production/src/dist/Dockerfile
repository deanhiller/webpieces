FROM adoptopenjdk/openjdk11:jre-11.0.6_10-alpine
RUN mkdir -p ./webpieces
COPY . ./webpieces/
COPY config/logback.cloudrun.xml ./webpieces/config/logback.xml
WORKDIR "/webpieces"
ENTRYPOINT ./bin/webpiecesexample -http.port=:$PORT -hibernate.persistenceunit=production
