#!/bin/bash
#

PROJECT_PATH="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

CSSVER=$(cat ${PROJECT_PATH}/features/org.csstudio.ess.product.configuration.feature/rootfiles/ess-version.txt)
GITREPO=$1
JBUILD=$2

if [ "$GITREPO" = "master" ]
then

	echo "----------------------------------------------------------------------"
	echo "Adding build number ${JBUILD} to version ${CSSVER}..."
	echo "----------------------------------------------------------------------"

	pwd
	${PROJECT_PATH}/prepare-next-release.sh ${CSSVER}b${JBUILD} false

	echo "----------------------------------------------------------------------"
	echo "Version number ${CSSVER}b${JBUILD} succesfully updated."
	echo "----------------------------------------------------------------------"

fi
