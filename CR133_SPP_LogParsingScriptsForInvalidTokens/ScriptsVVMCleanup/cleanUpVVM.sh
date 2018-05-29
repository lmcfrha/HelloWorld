#!/bin/bash 
###########################################
# CONFIG PARAMETERS AND FUNCTION DEFINITION
###########################################
#
source ./config.sh

##########################################
# DB query to retrieve the deviced-Id's (dev-id not unique))
##########################################
java -jar oracledbquery-0.0.1-SNAPSHOT-jar-with-dependencies.jar VVMquery >  $APPLOGFOLDER/VVMresultSet_${DATE}
sort --key=1,1 -u  $APPLOGFOLDER/VVMresultSet_${DATE} | cut -f1 -d" " >   $APPLOGFOLDER/VVMresultSet_unique_${DATE}
while read -r devId
do

  curlCmd="curl -i -X DELETE -H Accept:application/json -H Content-Type:application/json -u $CC_USER:$CC_PASSWORD '$CC_ENDPOINT?deviceId=$devId&applicationCategory=$CC_APPCATEGORY&additionalInfo=$CC_ADDINFO'"
  httpStatus=`curl -o /dev/null -w "%{http_code}" --max-time $CURL_TIMEOUT -i -X DELETE -H Accept:application/json -H Content-Type:application/json -u $CC_USER:$CC_PASSWORD "$CC_ENDPOINT?deviceId=$devId&applicationCategory=$CC_APPCATEGORY&additionalInfo=$CC_ADDINFO"`
  echo  $devId $curlCmd "....." $httpStatus >>  $APPLOGFOLDER/VVMCleanupLogs_${DATE}.log
done <  $APPLOGFOLDER/VVMresultSet_unique_${DATE}


############################################
# Log Cleanup
############################################
find $APPLOGFOLDER/ -maxdepth 1 -type f -mtime +$LOGRETENTION -exec rm {} \;

exit



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
                    if [[ $service =~ $CONN_SERVICES ]]; then
                      curlCmd="curl -i -X DELETE -H Content-Type:application/json -H x-requestor-name:mts -u $DPT_USER:$DPT_PASSWORD '$DPT_ENDPOINT?device-id=$devIdX&service-name=$service'"
                      curl -i -X DELETE -H Content-Type:application/json -H x-requestor-name:mts -u $DPT_USER:$DPT_PASSWORD "$DPT_ENDPOINT?device-id=$devIdX&service-name=$service"
                      echo  $apnsToken $service $msisdnX $devIdX " NotJanski, Not NativeIOS Stale" $curlCmd >> $APPLOGFOLDER/PARSED_${logfile:2}                     
                    else
                      curlCmd="curl -i -X DELETE -H Content-Type:application/json -H x-requestor-name:mts -u $DPT_USER:$DPT_PASSWORD '$DPT_ENDPOINT?msisdn=$msisdnX&device-id=$devIdX&service-name=$service'"
                      curl -i -X DELETE -H Content-Type:application/json -H x-requestor-name:mts -u $DPT_USER:$DPT_PASSWORD "$DPT_ENDPOINT?msisdn=$msisdnX&device-id=$devIdX&service-name=$service"
                      echo  $apnsToken $service $msisdnX $devIdX " NotJanski, Not NativeIOS Stale" $curlCmd >> $APPLOGFOLDER/PARSED_${logfile:2}                     
                    fi
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

