#!/bin/bash

echo "************* L322 - Lab3 Solution - Start Initialisation ***********"
git checkout lab3-solution
mvn clean install package -PautoInstallPackage
echo "************* L322 - Lab3 Solution - End Initialisation  ***********"
