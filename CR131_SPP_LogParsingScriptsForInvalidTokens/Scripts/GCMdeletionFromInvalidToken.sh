#!/bin/bash 
###########################################
# CONFIG PARAMETERS AND FUNCTION DEFINITION
###########################################
#
source ./config.sh

###########################################
# Select the logs which need to be parsed
###########################################

if [ -f $LAST_LOG_PROCESSED_GCM ]; then
   LOG_FILE_NAME=`cat $LAST_LOG_PROCESSED_GCM`
   LOG_FILES=`cd $LOGFOLDER;find . -maxdepth 1 -type f -regex $LOGFILEPATTERN -newer $LOG_FILE_NAME`
   if [[ $LOG_FILES == "" ]];then
      LOGS_TO_PARSE=""
   else
      LOGS_TO_PARSE=`cd $LOGFOLDER;echo $LOG_FILES | xargs ls -tr`
   fi
else
   LOG_FILES=`cd $LOGFOLDER;find . -maxdepth 1 -type f -regex $LOGFILEPATTERN -mmin -60`
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
# Use XPATH to extract invalid token-service
##########################################

  parsed=(`xmllint --xpath "$XPATH_GCM$TOKEN_SERVICE_GCM" $LOGFOLDER/$logfile | sed 's/<E218>\|<E1030>/ /g; s/<\/E218>\|<\/E1030>//g'`)
  parsedTimeStamp=`xmllint --xpath "$XPATH_GCM$TIMESTAMP_GCM" $LOGFOLDER/$logfile | sed 's/<E74>//g; s/<\/E74>/;/g'`

  index=0
  index_ts=1
  let "max=${#parsed[@]}-2"
#echo -e "\nParsed ${parsed[@]}"

##########################################
# Get MSISDN-DevId's from DB query based on token-service
##########################################

  until [ $index -gt $max ]; do
      gcmToken=${parsed[$index+1]}
      service=${parsed[$index]}
#######################################
#     Log timestamp, convert to Epoc
#######################################
      dateFromLog=`echo $parsedTimeStamp | cut -d";" -f$index_ts`
      dateEpocLog=`date "+%s" -d "$dateFromLog"`

#echo "$gcmToken $service $dateFromLog $dateEpocLog"
      msisdn_devId=(`java -jar dbquery.jar msisdn $gcmToken $service`)
echo -e "\n$(date)\nLog timestamp:  $dateFromLog Check DB for $gcmToken and $service:\n ${msisdn_devId[@]}" >> $APPLOGFOLDER/scriptGCMLogs_${DATE}.log
#echo -e "\nCheck DB for $gcmToken and $service: ${msisdn_devId[@]}" 

##########################################
# Filter out Janski devices
##########################################
      n=0
      let "msisdnNb=${#msisdn_devId[@]}-2"
      until [ $n -gt $msisdnNb ]; do
# Ignore if Janski
          msisdnX=${msisdn_devId[$n]}
          devIdX=${msisdn_devId[$n+1]}
          isNotJanski=`notJanski $devIdX]}`
echo -e "\n...$msisdnX is not Jansky: $isNotJanski" >> $APPLOGFOLDER/scriptGCMLogs_${DATE}.log
          if [[ $isNotJanski == 'true' ]]
          then

##########################################
# Not Janski: before deleting, proceed to query DB based on MSISDN and Service (and DevId)
# and check that all DB timestamps are older than log the log timestamp before deleting
# If more recent, don't delete.
##########################################
              token_timestamp=(`java -jar dbquery.jar token $msisdnX $service $devIdX`)
echo -e "...Check DB based on $msisdnX $service $devIdX\n...${token_timestamp[@]}" >> $APPLOGFOLDER/scriptGCMLogs_${DATE}.log
              t=0
              isStale=true
              let "tokenNb=${#token_timestamp[@]}-2"
              until [ $t -gt $tokenNb ]; do
                  timeStampX=${token_timestamp[$t+1]}
                  let age=$(( $timeStampX - $dateEpocLog ))
                  if [[ $age -gt 0 ]]; then
                     isStale=false
echo "......$timeStampX - $dateEpocLog = $age NOT stale, NOT calling CC delete" >> $APPLOGFOLDER/scriptGCMLogs_${DATE}.log
                  fi
                  let t=t+2
              done

############################################
# CC delete if stale:
############################################
              if [[ "$isStale" == true ]]; then
echo "......$timeStampX - $dateEpocLog = $age stale, calling CC delete"  >> $APPLOGFOLDER/scriptGCMLogs_${DATE}.log
                 curlCmd="curl -i -X DELETE -H Accept:application/json -H Content-Type:application/json -u $CC_USER:$CC_PASSWORD '$CC_ENDPOINT?msisdn=$msisdnX&deviceId=$devIdX&serviceNames=$service&additionalInfo=$CC_ADDINFO'"
                 curl -i -X DELETE -H Accept:application/json -H Content-Type:application/json -u $CC_USER:$CC_PASSWORD "$CC_ENDPOINT?msisdn=$msisdnX&deviceId=$devIdX&serviceNames=$service&additionalInfo=$CC_ADDINFO"
                 echo  $apnsToken $service $msisdnX $devIdX " NotJanski Stale" $curlCmd >> $APPLOGFOLDER/PARSED_GCM_${logfile:2}
              else
                 echo  $apnsToken $service $msisdnX $devIdX " NotJanski NOTStale"  >> $APPLOGFOLDER/PARSED_GCM_${logfile:2}
              fi
          else
             echo  $apnsToken $service $msisdnX $devIdX "Janski" >> $APPLOGFOLDER/PARSED_GCM_${logfile:2}
          fi
          let n=n+2
      done

      let index=index+2
  done
  echo ${logfile:2} > $LAST_LOG_PROCESSED_GCM
done

exit
