#!/bin/bash

echo "************* L322 - Start Initialisation ***********"
echo "************* Performing Git Fetch ************"
git fetch
echo "************* Performing Git pull *************"
git pull
echo "************* Performing Copy of Maven dependencies to local repository"
cp -rf external/* ~/.m2/repository
echo "************* Performing Git checkout *********"
git checkout lab1
echo """""""""""""" Performing Auto install package of Lab1 ******"
mvn clean install -PautoInstallPackage
echo "************* Copy Presentation, Workbook and Cheatsheets to Desktop"
cp /Users/l322/Desktop/AEM-Commerce-Lab-WS/aem-commerce-omni-channel/documentation/* /Users/l322/Desktop/
echo "************* L322 - End Initialisation  ***********"
