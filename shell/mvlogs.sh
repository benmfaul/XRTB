#~/bin/sh
#
# Rename, copy and delete log files off of a remote RTB4FREE system
#

CERT=~ben/certs/elbritrtb.pem
RTB=ubuntu@elrtb.com
WORKDIR=.

DATE=$(date +%Y%m%d)
mkdir $WORKDIR/logs
mkdir $WORKDIR/logs/$DATE


ssh -i $CERT $RTB mv XRTB/logs/request XRTB/logs/request.$DATE
scp -i $CERT $RTB:XRTB/logs/request.$DATE logs/$DATE/request
ssh -i $CERT $RTB rm XRTB/logs/request.$DATE

ssh -i $CERT $RTB mv XRTB/logs/forensiq XRTB/logs/forensiq.$DATE
scp -i $CERT $RTB:XRTB/logs/forensiq.$DATE logs/$DATE/forensiq
ssh -i $CERT $RTB rm XRTB/logs/forensiq.$DATE

ssh -i $CERT $RTB mv XRTB/logs/bid XRTB/logs/bid.$DATE
scp -i $CERT $RTB:XRTB/logs/bid.$DATE logs/$DATE/bid
ssh -i $CERT $RTB rm XRTB/logs/bid.$DATE

ssh -i $CERT $RTB mv XRTB/logs/nobid XRTB/logs/nobid.$DATE
scp -i $CERT $RTB:XRTB/logs/nobid.$DATE logs/$DATE/nobid
ssh -i $CERT $RTB rm XRTB/logs/nobid.$DATE

ssh -i $CERT $RTB mv XRTB/logs/win XRTB/logs/win.$DATE
scp -i $CERT $RTB:XRTB/logs/win.$DATE logs/$DATE/win
ssh -i $CERT $RTB rm XRTB/logs/win.$DATE

ssh -i $CERT $RTB mv XRTB/logs/accounting XRTB/logs/accounting.$DATE
scp -i $CERT $RTB:XRTB/logs/accounting.$DATE logs/$DATE/accounting
ssh -i $CERT $RTB rm XRTB/logs/accounting.$DATE

tar -cvzf $DATE.zip logs/$DATE
rm -rf logs/$DATE


