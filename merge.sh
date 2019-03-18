#!/bin/bash

VERSION=$1

declare -a repos=(
    "ess-css-extra"
    "maven-osgi-bundles"
    "cs-studio-thirdparty"
    "cs-studio"
    "org.csstudio.display.builder"
    "org.csstudio.ess.product"
)

declare -a gitcmds=(
    "git checkout production"
    "git pull origin production"
    "git merge master -m \"Merge master into production.\""
    "git push origin production"
    "git tag ESS-CS-Studio-$VERSION"
    "git checkout master"
)

git commit -a -m "Updating changelog, splash, manifests to version $VERSION"
git push origin

for i in "${repos[@]}"; do
    cd ../$i/
    echo `pwd`
    for k in "${gitcmds[@]}"; do
        $k
        ERROR_CODE_RETURNED=$?
        echo $ERROR_CODE_RETURNED
        if [[ $ERROR_CODE_RETURNED != 0 ]]; then
            echo "ERROR occurred running: $k"
            exit 1
        fi
    done
done
