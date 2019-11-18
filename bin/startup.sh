#!/bin/sh

ulimit -c unlimited

/usr/local/jdk1.8/bin/java -server -Xms1024M -Xmx1024M -Xmn384M -Xss256k -jar /app/kbase-media/kbase-media_v1.1.0-GA.jar --spring.config.location=application.yml > stdout.log &
tail -f stdout.log
