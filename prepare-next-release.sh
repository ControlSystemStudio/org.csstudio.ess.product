#!/bin/bash

set -e

#for debugging  each command...
#set -x

# Check parameters
if [ $# != 2 ]
then
  echo You must provide the next release product version, and git \(e.g. \"prepare-next-release.sh 3.3.1.0 false\"\)
  exit -1
fi

VERSION=$1
PUSH=$2
PROJECT_PATH="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BUILD_DIR="./build"

echo ""
echo "===="
echo "==== Path used: " ${PROJECT_PATH}
echo "====  JDK used: " $JAVA_HOME
echo "===="

echo ::: Prepare splash :::
java -jar $BUILD_DIR/ImageLabeler-2.0.jar "$VERSION (development)" 462 43 ${BUILD_DIR}/splash-template.bmp plugins/se.ess.ics.csstudio.product/splash.bmp "European Spallation Source Edition" 19 151 plugins/se.ess.ics.csstudio.startup.intro/icons/ess96.png 366 140

echo ::: Change about dialog version :::
echo 0=$VERSION > plugins/se.ess.ics.csstudio.product/about.mappings

echo ::: Change Ansible reference file :::
echo $VERSION > features/org.csstudio.ess.product.configuration.feature/rootfiles/ess-version.txt

echo ::: Updating plugin versions :::
mvn -Dtycho.mode=maven org.eclipse.tycho:tycho-versions-plugin:1.0.0:set-version -DnewVersion=$VERSION -Dartifacts=se.ess.ics.csstudio,se.ess.ics.csstudio.features,org.csstudio.ess.product.configuration.feature,org.csstudio.ess.product.core.feature,org.csstudio.ess.product.eclipse.feature,se.ess.ics.csstudio.plugins,se.ess.ics.csstudio.display.builder,se.ess.ics.csstudio.fonts,se.ess.ics.csstudio.product,se.ess.ics.csstudio.startup.intro,se.ess.ics.csstudio.repository

## update product because set-version doesn't
#echo ::: Updating product versions in product files :::
#COMMAND='s/(<product[^>]* version=")[^"]*("[^>]*>)/$1'${VERSION}'$2/g'
#echo ::::: sed command: ${COMMAND}
#echo ::::: repository/alarm-config.product
#java -jar build/jsed-1.0.0.jar "${COMMAND}" repository/alarm-config.product
#echo ::::: repository/alarm-notifier.product
#java -jar build/jsed-1.0.0.jar "${COMMAND}" repository/alarm-notifier.product
#echo ::::: repository/alarm-server.product
#java -jar build/jsed-1.0.0.jar "${COMMAND}" repository/alarm-server.product
#echo ::::: repository/cs-studio-ess.product
#java -jar build/jsed-1.0.0.jar "${COMMAND}" repository/cs-studio-ess.product
#echo ::::: repository/jms2rdb.product
#java -jar build/jsed-1.0.0.jar "${COMMAND}" repository/jms2rdb.product

echo ::: Updating product versions in master POM file :::
COMMAND='s/(<product\.version>)[^<]*(<[^>]*>)/$1'${VERSION}'$2/g'
echo ::::: sed command: ${COMMAND}
echo ::::: pom.xml
java -jar build/jsed-1.0.0.jar "${COMMAND}" pom.xml

if [ "$PUSH" = "true" ]
then
  echo ::: Committing version $VERSION :::
  git commit -a -m "Updating splash, manifests to version $VERSION"
  echo ::: Pushing changes :::
  git push origin
fi
