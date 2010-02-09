#!/bin/sh

PORT=4051
NAME=jTest
OPTIONS='-Djava.awt.headless=true'

while ( : ) do

  DATE=`date`
  echo "starting server $DATE"
  echo "running ChatServer.jar $FLAGS $PORT"

  java $OPTIONS -jar ChatServer.jar -p $PORT -n $NAME


  if [ -r .killscript ]; then
    DATE=`date`;
    echo "autoscript killed $DATE"
    rm .killscript
    exit
  fi

  while [ -r pause ]; do
    sleep 60
  done

done
