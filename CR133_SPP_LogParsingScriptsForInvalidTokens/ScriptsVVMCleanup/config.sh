#!/bin/bash
###########################################
# CONFIG PARAMETERS
###########################################
#

# The max time the CURL request for deletion waits for an answer (sec):
CURL_TIMEOUT=3
CC_ENDPOINT="http://odp4care2.msg.eng.t-mobile.com:8080/ses/customercare/v2/services"
CC_USER="customercare"
CC_PASSWORD="ccpwd"
CC_ADDINFO="App+Uninstall"
#
DATE=`date +%Y-%m-%d`
APPLOGFOLDER="./logs"
LOGRETENTION='30'
BACKLOGINITIAL='240'
#
# Find out the DB IP and user passwd
#
databaseIp=`grep dmsDbConnectionString /opt/miep/etc/config/ConfigData.json | grep -oE '[0-9]+\.[0-9]+\.[0-9]+\.[0-9]+' | head -1`
port=`grep dmsDbConnectionString /opt/miep/etc/config/ConfigData.json | awk -F')' '{print $5}' | tr -dc '0-9'`
dbuser=`grep -w dmsDbApplicationId /opt/miep/etc/config/ConfigData.json | awk -F'"' '{print $4}'`
dbpass=`grep -w dmsDbApplicationIdPassword /opt/miep/etc/config/ConfigData.json | awk -F'"' '{print $4}'`
#
# Update the datasource.properties with values determined by the preceding commands
#
sed -i s/^DB_URL=.*/DB_URL=jdbc:oracle:thin:${dbuser}\\/${dbpass}@${databaseIp}:${port}\\/DESPDB/ datasource.properties
sed -i s/^USER=.*/USER=${dbuser}/ datasource.properties
sed -i s/^PASS=.*/PASS=${dbpass}/ datasource.properties

