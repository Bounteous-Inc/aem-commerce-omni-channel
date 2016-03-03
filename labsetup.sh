#!/bin/bash

echo "************* Welcome to L322 - Commerce Integration Framework - Omnichannel Experience ***********"
echo "************* Starting AEM 6.2 Author Instance ***********"
echo "************* Current Folder Path --> $PWD"
cd /Users/vvenkata/Documents/Varun/PresentationInventory/Presentation/Summit2016/LabMachine/Users/l322/Desktop
cd AEM6.2
echo "************* Current Folder Path --> $PWD"
cd author
echo "************* Current Folder Path --> $PWD"
cd crx-quickstart
echo "************* Current Folder Path --> $PWD"
bin/start
echo "************* Current Folder Path --> $PWD"
echo "************* Starting AEM 6.2 Author Instance - COMPLETE ***********"


echo "************* Starting AEM 6.2 Publish Instance ***********"
cd /Users/vvenkata/Documents/Varun/PresentationInventory/Presentation/Summit2016/LabMachine/Users/l322/Desktop
cd AEM6.2
echo "************* Current Folder Path --> $PWD"
cd publish
echo "************* Current Folder Path --> $PWD"
cd crx-quickstart
echo "************* Current Folder Path --> $PWD"
bin/start
echo "************* Current Folder Path --> $PWD"
echo "************* Starting AEM 6.2 Publish Instance - COMPLETE ***********"



echo "************* Starting Git repository update ***********"
cd /Users/vvenkata/Documents/Varun/PresentationInventory/Presentation/Summit2016/LabMachine/Users/l322/Desktop
cd AEM-Commerce-Lab-WS
echo "************* Current Folder Path --> $PWD"
cd aem-commerce-omni-channel
echo "************* Current Folder Path --> $PWD"
git pull
echo "************* Current Folder Path --> $PWD"
echo "************* Starting Git repository update  - COMPLETE ***********"




echo "************* Starting code workspace initialisation ***********"
chmod 775 init.sh
sh init.sh
echo "************* Current Folder Path --> $PWD"
echo "************* Code workspace initialisation - COMPLETE ***********"

