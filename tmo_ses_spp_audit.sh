#!/bin/bash
# This script will be used to audit the TMO SES SPP SSM and SDD nodes.
# Managed by TMO SES SI Team
#
echo " "
echo "Starting Audit on all the nodes, please wait...."
logfile=audit_output_`date +%y-%m-%d-%H:%M:%S`.log
echo " " >>$logfile
echo "Audit Started" >> $logfile
echo " " >>$logfile
# Function to call the ansible commands

audit () { 		
echo " " >>$logfile
echo "***************************************************************************" >>$logfile
echo " $2 on $1" >>$logfile
echo "***************************************************************************" >>$logfile
echo " " >>$logfile
ansible $1 -m shell -a "$2" -i ../tmo-nsim-ses-ansible-inventory/ >>$logfile 2>&1
}

# Calling function to audit the systems

audit all 'nsctrl status'
audit sesssm '/usr/local/esa/bin/esaclusterstatus'
audit sesssm '/usr/local/esa/bin/fmactivealarms'
audit sesssm 'consul members'
audit sests 'pallogviewer -20 /var/log/miep/troubleshooter_log.xml |grep Text'
audit sppts 'pallogviewer -20 /var/log/miep/troubleshooter_log.xml |grep Text'
audit sessdd 'crm_mon -Afr -1'

echo " "
echo "Audit Completed. Make sure to check $logfile for detailed output and errors"
echo " "
