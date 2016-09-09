#!/bin/sh
mvn exec:exec -Dexec.executable=java -Dexec.args="-cp %classpath org.jitsi.jijibg.Main"
