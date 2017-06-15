#!/bin/bash -x
IP=$1
NM=${2:-baba-chess-trainer}
GZ=~/$NM.gz
GIT=http://github.com/calint/$NM

echo " * init linode ubuntu 16 lts at $IP" &&
echo "     from $GIT" &&

echo " * copy ssh public key to root" &&
ssh-copy-id root@$IP &&

echo " * install java 8" &&
#ssh root@$IP "apt-get update && apt-get -y upgrade && apt-get -y install openjdk-8-jre-headless && git clone $GIT /$NM" &&
ssh root@$IP "apt-get update && apt-get -y install openjdk-8-jre-headless git" &&

#echo " * install app $NM" &&
#ssh root@$IP "git clone $GIT /$NM" &&

echo " * install app $NM" &&
scp $GZ root@$IP:/ &&
ssh root@$IP "cd / && tar -xzvf $NM.gz -C /" &&

echo " * copy systemd service file ba.service to /etc/systemd/system/" &&
scp ba.service root@$IP:/etc/systemd/system/ &&

echo " * enable service" &&
ssh root@$IP systemctl enable ba &&

echo " * reboot" &&
ssh root@$IP reboot && 

echo " * done"
