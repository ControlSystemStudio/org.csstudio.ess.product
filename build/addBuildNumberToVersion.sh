#!/bin/bash
#

export CSSVER=$(cat ../features/org.csstudio.ess.product.configuration.feature/rootfiles/ess-version.txt)
export GITREPO=$1
export JBUILD=$2

if [ "$GITREPO" = "master" ]
then

	echo "--------------------------------------------------"
	echo "Adding build number ${JBUILD} to version ${CSSVER}..."
	echo "--------------------------------------------------"

	cd ..
  ./prepare-next-release.sh ${CSSVER}b${JBUILD} false

fi
