
#FROM centos:7.6.1810

FROM  jhipster/jhipster:latest

USER root
RUN mkdir -p /app
COPY . /app/
EXPOSE 29090
CMD [ "./app/mvnw -DskipTests -Pprod clean verify" ]