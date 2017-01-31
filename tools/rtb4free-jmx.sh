#/bin/sh
#
# This runs the RTB4FREE server
#
java -Xmx8g \
-Dcom.sun.management.jmxremote \
-Dcom.sun.management.jmxremote.port=9000 \
     -Dcom.sun.management.jmxremote.ssl=false \
     -Dcom.sun.management.jmxremote.authenticate=false \
     -Djava.rmi.server.hostname=bidder.adx.1trnvid.com \
-cp target/XRTB-0.0.1-SNAPSHOT-jar-with-dependencies.jar com.xrtb.bidder.RTBServer -s xrtb $1 $2 $3