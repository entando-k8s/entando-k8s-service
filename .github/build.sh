#!/bin/bash

mvn -B clean

(mvn org.codehaus.mojo:license-maven-plugin:2.5.0:aggregate-download-licenses &> ./license-maven-plugin.log) &
BKMVNPID="$!"

mvn versions:set -DnewVersion="$ARTIFACT_VERSION"

mvn -B package || {
  echo "::error::Failed building the project"
  exit 99
}

echo ""
echo "~> Waiting for license download completion"
wait "$BKMVNPID" || true

if grep -q "BUILD SUCCESS" ./license-maven-plugin.log; then
  echo "~> License download completed with success"
else
  echo "::error::License download terminated with error"
  exit 99
fi
