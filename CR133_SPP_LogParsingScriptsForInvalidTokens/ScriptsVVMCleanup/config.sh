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
