#!/bin/sh
#DIR=`dirname $0`
#cd $DIR
#rm -f deploy/shutdown.xml
#java -server \
#    -Xms1G \
#    -Xmx1G \
#    -XX:MetaspaceSize=96M \
#    -XX:MaxMetaspaceSize=256m  \
#    -Djava.net.preferIPv4Stack=true  \
#    -Djava.awt.headless=true \
#    -Dcom.sun.management.jmxremote \
#    -cp .:./config:./lib/* ${start-class} "$@"

DIR=`dirname $0`
cd $DIR
rm -f deploy/shutdown.xml
java -server \
    -Xms2G \
    -Xmx2G \
    -XX:MetaspaceSize=256M \
    -XX:MaxMetaspaceSize=512m  \
    -Djava.net.preferIPv4Stack=true  \
    -Djava.awt.headless=true \
    -Dcom.sun.management.jmxremote \
    -cp .:./config:./lib/* ${start-class} "$@"