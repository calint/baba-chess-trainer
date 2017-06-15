#!/bin/bash
IP=$1
NM=${2:-baba-chess-trainer}
GZIMG=${3:-~/$NM.gz}

echo " * init linode ubuntu 16 lts at $IP" &&
echo "     from $GZ" &&

echo " * copy ssh public key to root" &&
ssh-copy-id root@$IP &&

echo " * update package list" &&
ssh root@$IP apt-get update &&

echo " * install java 8" &&
ssh root@$IP apt-get -y install openjdk-8-jre-headless &&

echo " * install app $NM" &&
scp $GZIMG root@$IP:/ &&
ssh root@$IP tar -xzf /$NM.gz -C / &&

echo " * copy systemd service file ba.service to /etc/systemd/system/" &&
ssh root@$IP cp -a /$NM/howto/cloud/linode/ba.service /etc/systemd/system/ &&

echo " * enable service" &&
ssh root@$IP systemctl enable ba &&

echo " * start service" &&
ssh root@$IP systemctl start ba &&

echo " * done"
