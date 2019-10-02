[![Build Status](https://jenkins.entandocloud.com/buildStatus/icon?job=de-entando-k8s-service-master)](https://jenkins.entandocloud.com/view/Digital%20Exchange/job/de-entando-k8s-service-master/)
[![Coverage Status](https://coveralls.io/repos/github/entando/entando-k8s-service/badge.svg?branch=migrate-models)](https://coveralls.io/github/entando/entando-k8s-service?branch=migrate-models)

# Entando K8s Service
This service serves as an abstraction layer to the Kubernetes Plugin custom resource. It is meant to be used by `entando-core` to deploy and check status of the plugins on the cluster.

## Install

You'll need a Kubernetes cluster running, configure the environments described down below and execute the project.

## Environment Variables
>- `KUBERNETES_NAMESPACE`: The kubernetes namespace that this service is in. Defaults to `entando`.
>- `KUBERNETES_ENTANDO_APP_NAME`: The entando app name that this service is in. Defaults to `entando-dev`.

### Optional Environment Variables:
>- `LOG_LEVEL`: Log level. Default: `INFO`

## Dependencies
This project uses Entando `web-commons` which uses Entando `keycloak-connector`.

In order to make it work on dev environment, you have to clone and install the dependencies or just add to your IntelliJ workspace.

* Web Commons: https://github.com/entando/web-commons
* Keycloak Connector: https://github.com/entando/keycloak-commons

### CLI:
```
$ git clone git@github.com:entando/web-commons.git
$ git clone git@github.com:entando/keycloak-commons.git

$ cd web-commons && mvn install -Dmaven.test.skip=true && cd ..
$ cd keycloak-commons && mvn install -Dmaven.test.skip=true && cd ..
```
