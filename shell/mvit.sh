#!/bin/bash
#
# Moveit script
#
# Place in /etc/cron/hourly
# Be sure to chmod +x
#
CERT=~ben/certs/rtb4free_key.pem
RTB=ubuntu@rtb4free.com

WORKDIR=.

DATE=$(date +%Y%m%d)
TIME=$(date +%T)
mkdir -p $WORKDIR/logs/$RTB


ssh -i $CERT $RTB sudo cp /var/log/rtb4free.log /var/log/rtb4free.log.$DATE.$TIME
ssh -i $CERT $RTB sudo truncate -s0 /var/log/rtb4free.log
scp -i $CERT $RTB:/var/log/rtb4free.log.$DATE.$TIME $WORKDIR/logs/$RTB
ssh -i $CERT $RTB sudo rm /var/log/rtb4free.log.$DATE.$TIME

ssh -i $CERT $RTB sudo cp XRTB/logs/request XRTB/logs/request.$DATE.$TIME
ssh -i $CERT $RTB sudo truncate -s0 XRTB/logs/request
scp -i $CERT $RTB:XRTB/logs/request.$DATE.$TIME $WORKDIR/logs/$RTB

