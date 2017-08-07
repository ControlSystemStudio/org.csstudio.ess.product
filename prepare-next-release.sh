#!/bin/bash
set -e

# Check parameters
if [ $# != 2 ]
then
  echo You must provide the next release product version, and git \(e.g. \"prepare-next-release.sh 3.3.1 false\"\)
  exit -1
fi

VERSION=$1
PUSH=$2
BUILD_DIR="../org.csstudio.product/build"

echo ::: Prepare splash :::
java -jar $BUILD_DIR/ImageLabeler-2.0.jar "$VERSION (development)" 462 43 ../org.csstudio.product/plugins/org.csstudio.product/splash-template.bmp plugins/se.ess.ics.csstudio.product/splash.bmp "European Spallation Source Edition" 19 151 plugins/se.ess.ics.csstudio.startup.intro/icons/ess96.png 366 140

echo ::: Change about dialog version :::
echo 0=$VERSION > plugins/se.ess.ics.csstudio.product/about.mappings

echo ::: Change Ansible reference file :::
echo $VERSION > features/org.csstudio.ess.product.configuration.feature/rootfiles/ess-version.txt

echo ::: Updating plugin versions :::
mvn -Dtycho.mode=maven org.eclipse.tycho:tycho-versions-plugin:1.0.0:set-version -DnewVersion=$VERSION -Dartifacts=se.ess.ics.csstudio,se.ess.ics.csstudio.features,org.csstudio.ess.product.configuration.feature,org.csstudio.ess.product.core.feature,org.csstudio.ess.product.eclipse.feature,se.ess.ics.csstudio.plugins,se.ess.ics.csstudio.product,se.ess.ics.csstudio.startup.intro,se.ess.ics.csstudio.repository
# update product because set-version doesn't
sed -i '' -e 's/\(<product[^>]* version="\)[^"]*\("[^>]*>\)/\1'${VERSION}'\2/g' repository/cs-studio-ess.product
sed -i '' -e 's/\(<product\.version>\)[^<]*\(\<\/product\.version>\)/\1'${VERSION}'\2/g' pom.xml

if [ "$PUSH" = "true" ]
then
  echo ::: Committing version $VERSION :::
  git commit -a -m "Updating splash, manifests to version $VERSION"
  echo ::: Pushing changes :::
  git push origin
fi
