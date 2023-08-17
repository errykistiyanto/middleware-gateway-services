#!/bin/sh
DIR=`dirname $0`
cd $DIR
rm -f deploy/shutdown.xml
java -server \
    -Dappname=Eureka-Interface \
    -Xmx1G \
    -Djava.net.preferIPv4Stack=true \
    -Dcom.sun.management.jmxremote \
    -cp .:./resources:./lib/* ${start-class} "$@"