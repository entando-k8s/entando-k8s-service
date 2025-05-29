#!/bin/bash

OPT1="" OPT2=""
if ! ${SKIP_TESTS:-false}; then
  # ~ TEST setup
  OPT1+="-Ppre-deployment-verification"
  #OPT1+=" -Dsurefire.skipAfterFailure=false"
  OPT1+=" -Dmaven.test.failure.ignore=false"

  # ~ COVERAGE setup
  OPT2+="org.jacoco:jacoco-maven-plugin:prepare-agent"
  OPT2+=" org.jacoco:jacoco-maven-plugin:report"
fi

OPT3=""
if ! ${SKIP_SCANS:-false}; then
  # ~ SCAN setup
  OPT3+=" org.sonarsource.scanner.maven:sonar-maven-plugin:5.0.0.4389:sonar"
  OPT3+=" -Dsonar.verbose=true"
else
  SONAR_PROJECT_KEY=""
  SONAR_ORG=""
fi

# ~ version set
mvn versions:set -DnewVersion="$ARTIFACT_VERSION"

_mvn_verify() {
  if $VERBOSE; then
    echo "~> Running mvn verify with options: $*"
  fi

  mvn -B verify "$@"
}

_mvn_verify $OPT1 $OPT2 $OPT3 \
  ${SONAR_PROJECT_KEY:+-Dsonar.projectKey="$SONAR_PROJECT_KEY"} \
  ${SONAR_ORG:+-Dsonar.organization="$SONAR_ORG"} \
  -Ddependency-check.skip=true \
;

RV="$?"
.github/github-tools/mvn.test.report.generate
exit "$RV"
