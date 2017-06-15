#!/bin/bash -x
IP=$1
NM=${2:-baba-chess-trainer}
GIT=http://github.com/calint/$NM

echo " * init linode ubuntu 16 lts at $IP using $GIT" &&
echo "     from $GIT" &&

echo " * copy ssh public key to root" &&
ssh-copy-id root@$IP &&

echo " * install java 8" &&
#ssh root@$IP "apt-get update && apt-get -y upgrade && apt-get -y install openjdk-8-jre-headless && git clone $GIT /$NM" &&
ssh root@$IP apt-get -y install openjdk-8-jre-headless &&

echo " * install app $NM" &&
ssh root@$IP git clone $GIT /$NM &&

exit


echo " * copy systemd service file ba.service to /etc/systemd/system/" &&
scp ba.service root@$IP:/etc/systemd/system/ &&
ssh root@$IP systemctl enable ba &&
#echo " * update packages" &&
#ssh root@$IP apt-get -y upgrade &&

#echo " * install java runtime" &&
#ssh root@$IP apt-get -y install openjdk-8-jre-headless unzip &&

#echo " * copy install image  $IMG to $IP:/" &&
#scp $IMG root@$IP:/

#ssh root@$IP git clone $GIT $NM

#echo " * unpack image to /" &&
#ssh root@$IP tar xzf $IMG -C / && 

#echo " * adjust installed image" &&
#ssh root@$IP chmod +x /a/ba.sh && 

echo " * reboot" &&
ssh root@$IP reboot && 

echo " * done"
