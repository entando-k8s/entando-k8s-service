FROM entando/entando-ubi8-java11-base:6.4.0
ARG VERSION
### Required Openshift Labels
LABEL name="Entando Kubernetes Service" \
      maintainer="dev@entando.com" \
      vendor="Entando Inc." \
      version="v${VERSION}" \
      release="6.3.0" \
      summary="Entando infrastructure project for Kubernetes APIs" \
      description="Entando infrastructure project for kubernetes APIs"

COPY target/generated-resources/licenses /licenses

ENV PORT 8080
ENV CLASSPATH /opt/lib
EXPOSE 8080

# copy pom.xml and wildcards to avoid this command failing if there's no target/lib directory
COPY pom.xml target/lib* /opt/lib/

# NOTE we assume there's only 1 jar in the target dir
# but at least this means we don't have to guess the name
# we could do with a better way to know the name - or to always create an app.jar or something
COPY target/entando-k8s-service.jar /opt/app.jar
WORKDIR /opt
CMD ["java", "-XX:+UnlockExperimentalVMOptions", "-jar", "app.jar"]
