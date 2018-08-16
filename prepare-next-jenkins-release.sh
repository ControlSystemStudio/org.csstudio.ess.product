#!/bin/bash
#

CSSVER=$(cat features/org.csstudio.ess.product.configuration.feature/rootfiles/ess-version.txt)
GITREPO=$1
JBUILD=$2

if [ "$GITREPO" = "master" ]
then

	echo "----------------------------------------------------------------------"
	echo "Adding build number ${JBUILD} to version ${CSSVER}..."
	echo "----------------------------------------------------------------------"

	./prepare-next-release.sh ${CSSVER}b${JBUILD} false

	if [ $? -eq 0 ]
	then
		echo "----------------------------------------------------------------------"
		echo "Version number ${CSSVER}b${JBUILD} succesfully updated."
		echo "----------------------------------------------------------------------"
	else
		echo "----------------------------------------------------------------------"
		echo "Errors updating version number ${CSSVER}b${JBUILD}."
		echo "----------------------------------------------------------------------"
		exit $? # terminate and indicate error
	fi

fi
