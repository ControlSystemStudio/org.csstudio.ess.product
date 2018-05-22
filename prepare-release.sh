#!/bin/bash
set -e

# Check parameters
if [ $# != 4 ]
then
  echo You must provide the product version, milestone, notes, git \(e.g. \"prepare-release.sh 3.3.0.0 \"https://github\" \"https://github\" \"Some notes\" false\"\)
  exit -1
fi

VERSION=$1
MILESTONE=$2
NOTES=$3
PUSH=$4
BUILD_DIR="./build"

echo ::: Prepare splash :::
java -jar $BUILD_DIR/ImageLabeler-2.0.jar $VERSION 462 43 $BUILD_DIR/splash-template.bmp plugins/se.ess.ics.csstudio.product/splash.bmp "European Spallation Source Edition" 19 151 plugins/se.ess.ics.csstudio.startup.intro/icons/ess96.png 366 140

echo ::: Change about dialog version :::
echo 0=$VERSION > plugins/se.ess.ics.csstudio.product/about.mappings

echo ::: Change Ansible reference file :::
echo $VERSION > features/org.csstudio.ess.product.configuration.feature/rootfiles/ess-version.txt

echo ::: Updating plugin versions :::
mvn -Dtycho.mode=maven org.eclipse.tycho:tycho-versions-plugin:1.0.0:set-version -DnewVersion=$VERSION -Dartifacts=se.ess.ics.csstudio,se.ess.ics.csstudio.features,org.csstudio.ess.product.configuration.feature,org.csstudio.ess.product.core.feature,org.csstudio.ess.product.eclipse.feature,se.ess.ics.csstudio.plugins,se.ess.ics.csstudio.display.builder,se.ess.ics.csstudio.fonts,se.ess.ics.csstudio.product,se.ess.ics.csstudio.startup.intro,se.ess.ics.csstudio.repository
# update product because set-version doesn't
sed -i '' -e 's/\(<product[^>]* version="\)[^"]*\("[^>]*>\)/\1'${VERSION}'\2/g' repository/cs-studio-ess.product
sed -i '' -e 's/\(<product\.version>\)[^<]*\(\<\/product\.version>\)/\1'${VERSION}'\2/g' pom.xml

HTML="<h2>Version ${VERSION} - $(date +"%Y-%m-%d")</h2><ul>"

if [ -n "${NOTES}" ];
then
  HTML="${HTML}<li>${NOTES}</li>";
fi

HTML="${HTML}<li><a href=\"${MILESTONE}\" shape=\"rect\">Closed Issues</a></li></ul>"

# html encode &
HTML=$(echo $HTML | sed 's/&/\&amp;/g;')
# escape all backslashes first
HTML="${HTML//\\/\\\\}"
# escape slashes
HTML="${HTML//\//\\/}"
# escape asterisks
HTML="${HTML//\*/\\*}"
# escape full stops
HTML="${HTML//./\\.}"
# escape [ and ]
HTML="${HTML//\[/\\[}"
HTML="${HTML//\]/\\]}"
# escape ^ and $
HTML="${HTML//^/\\^}"
HTML="${HTML//\$/\\\$}"
# remove newlines
HTML="${HTML//[$'\n']/}"

sed -i '' -e '/<\/p>/ a\
'"${HTML}" plugins/se.ess.ics.csstudio.startup.intro/html/changelog.html

if [ "$PUSH" = "true" ]
then
  echo ::: Committing version $VERSION :::
  git commit -a -m "Updating changelog, splash, manifests to version $VERSION"
  echo ::: Tagging version $VERSION :::
  git tag ESS-CSS-$VERSION
  echo ::: Pushing changes :::
  git push origin
  git push origin ESS-CS-Studio-$VERSION
fi
