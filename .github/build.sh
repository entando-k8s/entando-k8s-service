#!/bin/bash

mvn -B clean

(mvn org.codehaus.mojo:license-maven-plugin:2.5.0:aggregate-download-licenses &> ./license-maven-plugin.log) &
BKMVNPID="$!"

# Check if parent has PR version and purge if needed
PARENT_VERSION=$(mvn help:evaluate -Dexpression=project.parent.version -q -DforceStdout)
if [[ "$PARENT_VERSION" == *"-PR"* ]]; then
  PARENT_GROUP_ID=$(mvn help:evaluate -Dexpression=project.parent.groupId -q -DforceStdout)
  PARENT_ARTIFACT_ID=$(mvn help:evaluate -Dexpression=project.parent.artifactId -q -DforceStdout)
  echo "~> Parent PR version detected ($PARENT_GROUP_ID:$PARENT_ARTIFACT_ID:$PARENT_VERSION), purging parent dependency cache"
  mvn dependency:purge-local-repository \
    -DmanualInclude="$PARENT_GROUP_ID:$PARENT_ARTIFACT_ID" \
    -DreResolve=false \
    -DactTransitively=false
fi

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
