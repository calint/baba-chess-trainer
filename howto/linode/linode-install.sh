#!/bin/bash
IP=$1
NM=${2:-baba-chess-trainer}
GZ=$NM.gz
WGET=https://github.com/calint/ba-imgs/blob/master/$GZ?raw=true

echo " * init linode ubuntu 16 lts at $IP" &&
echo "     from $WGET" &&

echo " * copy ssh public key to root" &&
ssh-copy-id root@$IP &&

echo " * update package list and install packages" &&
ssh root@$IP "apt-get update && apt-get -y install openjdk-8-jre-headless wget" &&

echo " * install $WGET"
ssh root@$IP "cd / && wget -O $GZ $WGET && tar -xzvf $GZ" &&

echo " * enable ba server" &&
ssh root@$IP "cp -a /$NM/howto/systemd/ba.service /etc/systemd/system/ && systemctl enable ba && systemctl start ba" &&

echo " * done"
