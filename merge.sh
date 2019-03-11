#!/bin/bash

VERSION=$1

declare -a repos=("https://github.com/ESSICS/org.csstudio.display.builder.git"
                "https://github.com/ESSICS/cs-studio-thirdparty.git"
                "https://github.com/ESSICS/ess-css-extra.git"
                "https://github.com/ESSICS/cs-studio.git"
                "https://github.com/ESSICS/maven-osgi-bundles.git"
                "https://github.com/ESSICS/org.csstudio.ess.product.git"
               )

for i in "${repos[@]}"
do
    git checkout production
    git pull origin production
    git merge master
    git push origin production
    git tag ESS-CS-Studio-$VERSION
    git checkout master
done

exit 0
