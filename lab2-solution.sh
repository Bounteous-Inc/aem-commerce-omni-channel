#!/bin/bash

echo "************* L322 - Lab2 Solution - Start Initialisation ***********"
git checkout lab2-solution
mvn clean install package -PautoInstallPackage
echo "************* L322 - Lab2 Solution - End Initialisation  ***********"
