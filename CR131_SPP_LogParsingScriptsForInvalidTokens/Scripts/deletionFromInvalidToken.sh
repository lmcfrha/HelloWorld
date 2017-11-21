#!/bin/bash 
###########################################
# CONFIG PARAMETERS AND FUNCTION DEFINITION
###########################################
#
source ./config.sh

###########################################
# Select the logs which need to be parsed
###########################################

if [ -f $LAST_LOG_PROCESSED ]; then
   LOG_FILE_NAME=`cat $LAST_LOG_PROCESSED`
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
#     Log timestamp, convert to Epoc
#######################################
      dateFromLog=`echo $parsedTimeStamp | cut -d";" -f$index_ts`
      dateEpocLog=`date "+%s" -d "$dateFromLog"`
 
# echo "$apnsToken $service $dateFromLog $dateEpocLog" 
      msisdn_devId=(`java -jar dbquery.jar msisdn $apnsToken $service`)
echo -e "\n$(date)\nLog timestamp:  $dateFromLog Check DB for $apnsToken and $service:\n ${msisdn_devId[@]}" >> $APPLOGFOLDER/scriptLogs_${DATE}.log

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
echo -e "\n...$msisdnX is not Janski: $isNotJanski" >> $APPLOGFOLDER/scriptLogs_${DATE}.log
          if [[ $isNotJanski == 'true' ]]
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
echo "......$timeStampX - $dateEpocLog = $age NOT stale, NOT calling CC delete" >> $APPLOGFOLDER/scriptLogs_${DATE}.log  
                  fi
                  let t=t+2
              done

############################################
# CC delete if stale:
############################################
              if [[ "$isStale" == true ]]; then
echo "......$timeStampX - $dateEpocLog = $age stale, calling CC delete"  >> $APPLOGFOLDER/scriptLogs_${DATE}.log
                 curlCmd="curl -i -X DELETE -H Accept:application/json -H Content-Type:application/json -u $CC_USER:$CC_PASSWORD '$CC_ENDPOINT?msisdn=$msisdnX&deviceId=$devIdX&serviceNames=$service&additionalInfo=$CC_ADDINFO'"
                 curl -i -X DELETE -H Accept:application/json -H Content-Type:application/json -u $CC_USER:$CC_PASSWORD "$CC_ENDPOINT?msisdn=$msisdnX&deviceId=$devIdX&serviceNames=$service&additionalInfo=$CC_ADDINFO"
                 echo  $apnsToken $service $msisdnX $devIdX $curlCmd >> $APPLOGFOLDER/PARSED_${logfile:2} 
              else
                 echo  $apnsToken $service $msisdnX $devIdX " NotJanski NOTStale"  >> $APPLOGFOLDER/PARSED_${logfile:2}
              fi
          else
             echo  $apnsToken $service $msisdnX $devIdX "Janski" >> $APPLOGFOLDER/PARSED_${logfile:2}
          fi
          let n=n+2
      done

      let index=index+2
  done
  echo ${logfile:2} > $LAST_LOG_PROCESSED
done
############################################
# Log Cleanup
############################################
find $APPLOGFOLDER/ -maxdepth 1 -type f -mtime +$LOGRETENTION -exec rm {} \;
exit
