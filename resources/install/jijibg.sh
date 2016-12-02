#!/bin/bash

if [[ "$1" == "--help" ]]; then
    echo -e "Usage: $0. Period. Don't ask for --help. Just run it. :)"
    exit 1
fi

if [ `uname` == "Darwin" ] ;then
    SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
else
    SCRIPT_DIR="$(dirname "$(readlink -f "$0")")"
fi

mainClass="org.jitsi.jijibg.Main"
cp=$(JARS=($SCRIPT_DIR/jijibg*.jar $SCRIPT_DIR/lib/*.jar); IFS=:; echo "${JARS[*]}")

JIJIBG_PROPS_FILE=jijibg.properties
JIJIBG_PROPS=""
if [ -e $JIJIBG_PROPS_FILE ] ;then
    while read -r prop ;do 
        JIJIBG_PROPS="$JIJIBG_PROPS -D$prop"
    done < $JIJIBG_PROPS_FILE
fi
echo $JIJIBG_PROPS

exec java -Xmx2048M -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/tmp $JIJIBG_PROPS -cp $cp $mainClass $@
