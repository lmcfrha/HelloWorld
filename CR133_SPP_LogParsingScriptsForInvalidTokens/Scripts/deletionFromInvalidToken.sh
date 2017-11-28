#!/bin/bash 
###########################################
# CONFIG PARAMETERS AND FUNCTION DEFINITION
###########################################
#
source ./config.sh

if [ -f runningFirst ]; then
   exit
fi
###########################################
# Select the logs which need to be parsed
###########################################

if [ -f $LAST_LOG_PROCESSED ]; then
   echo `date`" Crontab APNS channel Execution" >> $APPLOGFOLDER/scriptLogs_${DATE}.log
   LOG_FILE_NAME=`cat $LAST_LOG_PROCESSED`
   LOG_FILES=`cd $LOGFOLDER;find . -maxdepth 1 -type f -regex $LOGFILEPATTERN -newer $LOG_FILE_NAME`
   if [[ $LOG_FILES == "" ]];then
      LOGS_TO_PARSE=""
   else
      LOGS_TO_PARSE=`cd $LOGFOLDER;echo $LOG_FILES | xargs ls -tr`
   fi
else
   touch runningFirst
   echo `date`" InitialAPNS Execution" >> $APPLOGFOLDER/scriptLogs_${DATE}.log
   LOG_FILES=`cd $LOGFOLDER;find . -maxdepth 1 -type f -regex $LOGFILEPATTERN -mmin -$BACKLOGINITIAL`
   if [[ $LOG_FILES == "" ]];then
      LOGS_TO_PARSE=""
   else
      LOGS_TO_PARSE=`cd $LOGFOLDER;echo $LOG_FILES | xargs ls -tr`
   fi
fi

for logfile in $LOGS_TO_PARSE
do
##########################################
# On each log file,
# Use XPATH to extract invalid token-service and timestamp
##########################################

  parsed=(`xmllint --xpath "$XPATH$TOKEN_SERVICE" $LOGFOLDER/$logfile | sed 's/<E22>\|<E28>/ /g; s/<\/E22>\|<\/E28>//g'`)
  parsedTimeStamp=`xmllint --xpath "$XPATH$TIMESTAMP" $LOGFOLDER/$logfile | sed 's/<E74>//g; s/<\/E74>/;/g'`

  index=0
  index_ts=1
  let "max=${#parsed[@]}-2"
# echo "MAX=$max ${parsed[@]}"

##########################################
# Get MSISDN-DevId's from DB query based on token-service
##########################################

  until [ $index -gt $max ]; do
      apnsToken=${parsed[$index]}
      service=${parsed[$index+1]}
	  
#######################################
#     Check Services Black List
#######################################
      if [[ ! ($service =~ $SERVICES_BL) ]]; then 
	     
#######################################
#     Log timestamp, convert to Epoc
#######################################
         dateFromLog=`echo $parsedTimeStamp | cut -d";" -f$index_ts`
         dateEpocLog=`date "+%s" -d "$dateFromLog"`

# echo "$apnsToken $service $dateFromLog $dateEpocLog"
         msisdn_devId=(`java -jar dbquery.jar msisdn $apnsToken $service`)
echo -e "\n$(date)\nLog timestamp:  $dateFromLog Check DB for $apnsToken and $service:\n ${msisdn_devId[@]}" >> $APPLOGFOLDER/scriptLogs_${DATE}.log

##########################################
# Filter out Janski devices AND Native IOS devices
##########################################
         n=0
         let "msisdnNb=${#msisdn_devId[@]}-2"
         until [ $n -gt $msisdnNb ]; do
# Ignore if Janski
            msisdnX=${msisdn_devId[$n]}
            devIdX=${msisdn_devId[$n+1]}
            isNativeIOS=`nativeIOS $devIdX`
            isNotJanski=`notJanski $devIdX`
echo -e "\n...$msisdnX $devIdX is not Janski: $isNotJanski" >> $APPLOGFOLDER/scriptLogs_${DATE}.log
echo -e "...$msisdnX $devIdX is Native IOS: $isNativeIOS" >> $APPLOGFOLDER/scriptLogs_${DATE}.log
            if [[ $isNotJanski == 'true' && $isNativeIOS == 'false' ]]
            then
##########################################
# Not Janski: before deleting, proceed to query DB based on MSISDN and Service (and DevId)
# and check that all DB timestamps are older than log the log timestamp before deleting
# If more recent, don't delete.
##########################################
               token_timestamp=(`java -jar dbquery.jar token $msisdnX $service $devIdX`)
echo -e "...Check DB based on $msisdnX $service $devIdX\n...${token_timestamp[@]}" >> $APPLOGFOLDER/scriptLogs_${DATE}.log
               t=0
               isStale=true
               let "tokenNb=${#token_timestamp[@]}-2"
               until [ $t -gt $tokenNb ]; do
                  timeStampX=${token_timestamp[$t+1]}
                  let age=$(( $timeStampX - $dateEpocLog ))
                  if [[ $age -gt 0 ]]; then
                     isStale=false
echo "......$timeStampX - $dateEpocLog = $age NOT stale, NOT calling CC delete or DeletePushToken" >> $APPLOGFOLDER/scriptLogs_${DATE}.log
                  fi
                  let t=t+2
               done

############################################
# CC delete and Delete Push Token logic
############################################
               if [[ "$isStale" == true ]]; then
echo -e "\n `date`" >> $APPLOGFOLDER/PARSED_${logfile:2}
	          if [[ $service =~ $CALL_CC_DELETE_VOWIFI ]]; then
echo "......$timeStampX - $dateEpocLog = $age stale, calling CC delete"  >> $APPLOGFOLDER/scriptLogs_${DATE}.log
                     curlCmd="curl -i -X DELETE -H Accept:application/json -H Content-Type:application/json -u $CC_USER:$CC_PASSWORD '$CC_ENDPOINT?msisdn=$msisdnX&deviceId=$devIdX&serviceNames=vowifi&additionalInfo=$CC_ADDINFO'"
                     curl -i -X DELETE -H Accept:application/json -H Content-Type:application/json -u $CC_USER:$CC_PASSWORD "$CC_ENDPOINT?msisdn=$msisdnX&deviceId=$devIdX&serviceNames=vowifi&additionalInfo=$CC_ADDINFO"
                     echo  $apnsToken $service $msisdnX $devIdX " NotJanski, Not NativeIOS Stale" $curlCmd >> $APPLOGFOLDER/PARSED_${logfile:2}
                  fi
		  if [[ ! ($service =~ $NOCALL_DELETE_PUSH_TOKEN) ]]; then
echo "......calling Delete Push Token"  >> $APPLOGFOLDER/scriptLogs_${DATE}.log
                     curlCmd="curl -i -X DELETE -H Content-Type:application/json -H x-requestor-name:mts -u $DPT_USER:$DPT_PASSWORD '$DPT_ENDPOINT?msisdn=$msisdnX&device-id=$devIdX&service-name=$service'"
                     curl -i -X DELETE -H Content-Type:application/json -H x-requestor-name:mts -u $DPT_USER:$DPT_PASSWORD "$DPT_ENDPOINT?msisdn=$msisdnX&device-id=$devIdX&service-name=$service"
                     echo  $apnsToken $service $msisdnX $devIdX " NotJanski, Not NativeIOS Stale" $curlCmd >> $APPLOGFOLDER/PARSED_${logfile:2}                     
#curl -v -X DELETE -H 'Content-Type: application/json' -H 'x-requestor-name: mts' -u ses:ses01 "http://$myIP:8084/spp/token/v2?imsi={$myimsi}&msisdn={$mymsisdn}&device-id={$mydevid}&service-name={vowifi,vowifistg,voipstg,voipstg,voip,newvowifi}"
		  fi
               else
                 echo  $apnsToken $service $msisdnX $devIdX " NotJanski, NotNativeIOS, NotStale"  >> $APPLOGFOLDER/PARSED_${logfile:2}
               fi
            else
               echo  $apnsToken $service $msisdnX $devIdX "Janski or NativeIOS" >> $APPLOGFOLDER/PARSED_${logfile:2}
            fi
            let n=n+2
          done
      else
	     echo  $apnsToken $service "service ignored (belongs to SERVICES_BL)" >> $APPLOGFOLDER/PARSED_${logfile:2}
	  fi
      let index=index+2
  done
  echo ${logfile:2} > $LAST_LOG_PROCESSED
done
############################################
# Log Cleanup
############################################
find $APPLOGFOLDER/ -maxdepth 1 -type f -mtime +$LOGRETENTION -exec rm {} \;

if [[ -f runningFirst ]]; then
   rm -f runningFirst
   echo `date`" InitialAPNS Execution Completed" >> $APPLOGFOLDER/scriptLogs_${DATE}.log
else
   echo `date`" Crontab APNS channel Execution Completed" >> $APPLOGFOLDER/scriptLogs_${DATE}.log
fi
exit
