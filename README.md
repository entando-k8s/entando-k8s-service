[![Build Status](https://img.shields.io/endpoint?url=https%3A%2F%2Fstatusbadge-jx.apps.serv.run%2Fentando-k8s%2Fentando-k8s-service)](https://github.com/entando-k8s/devops-results/tree/logs/jenkins-x/logs/entando-k8s/entando-k8s-service/master)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=entando-k8s_entando-k8s-service&metric=alert_status)](https://sonarcloud.io/dashboard?id=entando-k8s_entando-k8s-service)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=entando-k8s_entando-k8s-service&metric=coverage)](https://entando-k8s.github.io/devops-results/entando-k8s-service/master/jacoco/index.html)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=entando-k8s_entando-k8s-service&metric=vulnerabilities)](https://entando-k8s.github.io/devops-results/entando-k8s-service/master/dependency-check-report.html)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=entando-k8s_entando-k8s-service&metric=code_smells)](https://sonarcloud.io/dashboard?id=entando-k8s_entando-k8s-service)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=entando-k8s_entando-k8s-service&metric=security_rating)](https://sonarcloud.io/dashboard?id=entando-k8s_entando-k8s-service)
[![Technical Debt](https://sonarcloud.io/api/project_badges/measure?project=entando-k8s_entando-k8s-service&metric=sqale_index)](https://sonarcloud.io/dashboard?id=entando-k8s_entando-k8s-service)

# Entando K8s Service
This service serves as an abstraction layer over K8S apis.

## Install

You'll need a Kubernetes cluster running, configure the environments described down below and execute the project.

## Environment Variables
|Variable|Description|
|:---|:---|
|KUBERNETES_NAMESPACE| The kubernetes namespace that this service is in. Defaults to `entando`|
|KUBERNETES_ENTANDO_APP_NAME|The entando app name that this service is in. Defaults to `entando-dev`.|

### Optional Environment Variables:
>- `LOG_LEVEL`: Log level. Default: `INFO`

