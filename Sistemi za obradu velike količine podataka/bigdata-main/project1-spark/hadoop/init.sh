#!/bin/sh

sh /etc/bootstrap.sh -bash
/usr/local/hadoop/bin/hadoop dfsadmin -safemode wait
/usr/local/hadoop/bin/hadoop fs -put /var/opt/extrasensory/data/ /
while true; do sleep 1000; done