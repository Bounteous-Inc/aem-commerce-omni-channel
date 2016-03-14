#!/bin/bash

echo "************* L322 - Lab4 Solution - Start Initialisation ***********"
git checkout lab4-solution
mvn clean install package -PautoInstallPackage
echo "************* L322 - Lab4 Solution - End Initialisation  ***********"
