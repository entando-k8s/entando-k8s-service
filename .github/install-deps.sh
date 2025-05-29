#!/bin/bash

PARENT_VERSION="v$(grep -m 1 '<parent>' pom.xml -A 4 | grep '<version>' | sed -E 's/.*<version>([^<]+)<\/version>.*/\1/')"

echo "~> Installing the dependency: entando-maven-root:$PARENT_VERSION"

(
  git clone --depth 1 --branch "${PARENT_VERSION}" "https://github.com/entando/entando-maven-root" &>/dev/null
  cd entando-maven-root
  mvn clean install &> mvn-install.log
  cd ..
  rm -rf entando-maven-root
) || {
  echo "::error::Error installing the entando-maven-root java dependency"
  cat mvn-install.log
  rm -rf entando-maven-root
  exit 99
}
