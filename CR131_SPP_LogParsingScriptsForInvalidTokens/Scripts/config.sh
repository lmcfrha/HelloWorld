#!/bin/bash
###########################################
# CONFIG PARAMETERS
###########################################
#
# The folder on the SPP servers containing the log files:
LOGFOLDER="/var/log/miep/tel/VG8/group2"
# The pattern of the log files:
LOGFILEPATTERN=./TrafficEventLog_.*xml$
# Kickass XPath to extract the E22 E28 of concern
XPATH="//S77/S266[(E7=410 and E26='Unregistered')]"
TOKEN_SERVICE="/*[self::E22 or self::E28]"
TIMESTAMP="/*[self::E74]"
# And for GCM channel:
XPATH_GCM="//S77/S245[E2120='200' and E2125[contains(.,'NotRegistered') ] ]"
TOKEN_SERVICE_GCM="/*[self::E1030 or self::E218]"
TIMESTAMP_GCM="/*[self::E74]"
# The max time the CURL request for deletion waits for an answer (sec):
CURL_TIMEOUT=3
CC_ENDPOINT="http://odp4care2lab1.msg.lab.t-mobile.com/ses/customercare/v2/services"
CC_USER="customercare"
CC_PASSWORD="ccpwd"
CC_ADDINFO="App+Uninstall"
#
LAST_LOG_PROCESSED="/opt/ericsson/CR131/last_log_processed"
LAST_LOG_PROCESSED_GCM="/opt/ericsson/CR131/last_log_processed_GCM"

JANKSY_TAC_LIST=".*|^35935806$|^35774606$|^35946206$|^35936206$|^35936106$|^35946406$|^35936406$|^35936306$|^35946506$|^35914606$|^35773006$|^35971506$|^35911606$|^35971706$|^35971806$|^35971906$|^35972006$|^35972106$|^35375307$|^35256707$|^35375407$|^35375207$|^35775207$|^35591607$|^35975507$|^35975407$|^35721907$|^35616407$|^35775107$|^35501808$|^35598708$|^35775408$|^35775308$|^35598008$|^35775808$|^35775908$|^35561908$|^35375607$|^35375707$|^35255707$|^35403407$|^35403507$|^35600007$|^35672907$|^35882907$"

DATE=`date +%Y-%m-%d`
APPLOGFOLDER="./logs"
LOGRETENTION='0'

###########################################
# Functions
###########################################
function notJanski {
device=$1
temp=${device//:/ }
dev=(${temp//-/ })
if [[ ${dev[0]} == "urn" &&  ${dev[1]} == "gsma" &&  ${dev[2]} == "imei" ]]
then
   if [[ ${dev[2]} =~ $JANKSY_TAC_LIST ]]
   then echo "false"
   else echo "true"
   fi

else
   echo "true"
fi
}

