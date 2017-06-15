#!/bin/bash

NM=baba-chess-trainer
GZ=$NM.gz
WGET=https://github.com/calint/ba-imgs/blob/master/$GZ?raw=true

apt-get update &&
apt-get -y install openjdk-8-jre-headless wget &&
cd / && wget -O $GZ $WGET && tar -xzvf $GZ &&
cp -a $NM/howto/systemd/ba.service /etc/systemd/system/ &&
systemctl enable ba &&
systemctl start ba