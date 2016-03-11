#!/bin/bash

echo "************* L322 - Start Initialisation ***********"
cd /Users/vvenkata/Documents/Varun/PresentationInventory/Presentation/Summit2016/LabMachine/Users/l322/Desktop/AEM-Commerce-Lab-WS/aem-commerce-omni-channel
git fetch
git checkout lab0
cp -rf external/* ~/.m2/repository
mvn clean install -PautoInstallPackage
echo "************* L322 - End Initialisation  ***********"
