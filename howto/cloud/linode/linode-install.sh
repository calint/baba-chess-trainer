#!/bin/bash -x
IP=$1
NM=${2:-baba-chess-trainer}
GZ=~/$NM.gz
#GIT=http://github.com/calint/$NM

echo " * init linode ubuntu 16 lts at $IP" &&
#echo "     from $GIT" &&
echo "     from $GZ" &&

echo " * copy ssh public key to root" &&
ssh-copy-id root@$IP &&

echo " * install java 8" &&
#ssh root@$IP "apt-get update && apt-get -y upgrade && apt-get -y install openjdk-8-jre-headless && git clone $GIT /$NM" &&
#ssh root@$IP "apt-get update && apt-get -y install openjdk-8-jre-headless git" &&
ssh root@$IP "apt-get -y install openjdk-8-jre-headless" &&

#echo " * install app $NM" &&
#ssh root@$IP "git clone $GIT /$NM" &&

echo " * install app $NM" &&
scp $GZ root@$IP:/ &&
ssh root@$IP tar -xzvf /$NM.gz -C / &&

echo " * copy systemd service file ba.service to /etc/systemd/system/" &&
ssh root@$IP cp -a /$NM/howto/cloud/linode/ba.service /etc/systemd/system/ &&

echo " * enable service" &&
ssh root@$IP systemctl enable ba &&

echo " * reboot" &&
ssh root@$IP reboot && 

echo " * done"
