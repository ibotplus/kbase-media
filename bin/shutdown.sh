#! /bin/sh
kill -9 `ps -ef|grep java|grep -v grep|grep kbase-media|awk '{print $2}'`
