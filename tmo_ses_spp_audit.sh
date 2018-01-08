#!/bin/bash
# This script will be used to audit the TMO SES SPP SSM and SDD nodes.
# Managed by TMO SES SI Team
#
logfile=audit_output_`date +%y-%m-%d-%H:%M:%S`.log
echo " "
echo "Starting Audit of all the nodes, please check the output file for any errors...."
echo " " >>$logfile
echo "Audit Started" >> $logfile
echo " " >>$logfile
echo "*********************" >>$logfile
echo "NSCTRL services status" >>$logfile
echo "*********************" >>$logfile
echo " " >>$logfile
ansible sesssm -m shell -a 'nsctrl status' -i ../tmo-nsim-ses-ansible-inventory/ >>$logfile 2>&1
echo " " >>$logfile
echo "**********************" >>$logfile
echo "SSM ESA Cluster Status" >>$logfile
echo "**********************" >>$logfile
echo " " >>$logfile
ansible sesssm -m shell -a '/usr/local/esa/bin/esaclusterstatus' -i ../tmo-nsim-ses-ansible-inventory/ >>$logfile 2>&1
echo " " >>$logfile
echo "**********************" >>$logfile
echo "SSM FMACTIVEALARMS" >>$logfile
echo "**********************" >>$logfile
echo " " >>$logfile
ansible sesssm -m shell -a '/usr/local/esa/bin/fmactivealarms' -i ../tmo-nsim-ses-ansible-inventory/ >>$logfile 2>&1
echo " " >>$logfile
echo "**********************" >>$logfile
echo "CONSUL MEMBERS" >>$logfile
echo "**********************" >>$logfile
echo " " >>$logfile
ansible sesssm -m shell -a 'consul members' -i ../tmo-nsim-ses-ansible-inventory/ >>$logfile 2>&1
echo " " >>$logfile
echo "**********************" >>$logfile
echo "SES Troubleshooter logs" >>$logfile
echo "**********************" >>$logfile
echo " " >>$logfile
ansible sests -m shell -a 'pallogviewer -20 /var/log/miep/troubleshooter_log.xml |grep Text' -i ../tmo-nsim-ses-ansible-inventory/ >>$logfile 2>&1
echo " " >>$logfile
echo "**********************" >>$logfile
echo "SPP Troubleshooter logs" >>$logfile
echo "**********************" >>$logfile
echo " " >>$logfile
ansible sppts -m shell -a 'pallogviewer -20 /var/log/miep/troubleshooter_log.xml |grep Text' -i ../tmo-nsim-ses-ansible-inventory/ >>$logfile 2>&1
echo " " >>$logfile
echo "**********************" >>$logfile
echo "SDD Cluster status" >>$logfile
echo "**********************" >>$logfile
echo " " >>$logfile
ansible sessdd -m shell -a 'crm_mon -Afr -1' -i ../tmo-nsim-ses-ansible-inventory/ >>$logfile 2>&1
echo " "
echo "Audit Completed. Make sure to check $logfile for detailed output and errors"
echo " "
