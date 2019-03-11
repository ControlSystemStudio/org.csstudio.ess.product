#!/bin/bash

VERSION=$1

declare -a repos=("org.csstudio.display.builder"
                "cs-studio-thirdparty"
                "ess-css-extra"
                "cs-studio"
                "maven-osgi-bundles"
                "org.csstudio.ess.product"
               )

for i in "${repos[@]}"
do
    cd ../$i/
    git checkout production
    git pull origin production
    git merge master
    git push origin production
    git tag ESS-CS-Studio-$VERSION
    git checkout master
done

exit 0
