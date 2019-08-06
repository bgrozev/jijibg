#! /bin/sh
#
# INIT script for Jijibg
# Version: 1.0  02-Dec-2016  boris@jitsi.org
#
### BEGIN INIT INFO
# Provides:          jijibg 
# Required-Start:    $local_fs $remote_fs
# Required-Stop:     $local_fs $remote_fs
# Default-Start:     2 3 4 5
# Default-Stop:      0 1 6
# Short-Description: Jijibg
# Description:       Jijibg
### END INIT INFO

. /lib/lsb/init-functions

# Include Jijibg defaults if available
if [ -f /etc/jitsi/jijibg/config ]; then
    . /etc/jitsi/jijibg/config
fi

PATH=/usr/local/sbin:/usr/local/bin:/sbin:/bin:/usr/sbin:/usr/bin
DAEMON=/usr/share/jijibg/jijibg.sh
NAME=jijibg
USER=jijibg
PIDFILE=/var/run/jijibg.pid
LOGFILE=/var/log/jitsi/jijibg.log

#DAEMON_OPTS=" --host=$JVB_HOST --domain=$JVB_HOSTNAME --port=$JVB_PORT --secret=$JVB_SECRET $JVB_OPTS"
DAEMON_OPTS=""

test -mucClientManager $DAEMON || exit 0

set -e

stop() {
    if [ -f $PIDFILE ]; then
        PID=$(cat $PIDFILE)
    fi
    echo -n "Stopping $NAME: "
    if [ $PID ]; then
        kill $PID || true
        rm $PIDFILE || true
        echo "$NAME stopped."
    else
        echo "$NAME doesn't seem to be running."
    fi
}

start() {
    if [ -f $PIDFILE ]; then
        echo "$NAME seems to be already running, we found pidfile $PIDFILE."
        exit 1
    fi
    echo -n "Starting $NAME: "
    DAEMON_START_CMD="JAVA_SYS_PROPS=\"$JAVA_SYS_PROPS\" exec $DAEMON $DAEMON_OPTS < /dev/null >> $LOGFILE 2>&1"
    AUTHBIND_CMD=""
    if [ "$AUTHBIND" = "yes" ]; then
        AUTHBIND_CMD="/usr/bin/authbind --deep /bin/bash -c "
        DAEMON_START_CMD="'$DAEMON_START_CMD'"
    fi
    start-stop-daemon --start --quiet --background --chuid $USER --make-pidfile --pidfile $PIDFILE \
        --exec /bin/bash -- -c "$AUTHBIND_CMD $DAEMON_START_CMD"
    echo "$NAME started."
}

reload() {
    echo 'Not yet implemented.'
}

status() {
    status_of_proc -p $PIDFILE java "$NAME" && exit 0 || exit $?
}

case "$1" in
  start)
    start
    ;;
  stop)
    stop
    ;;
  restart)
    stop
    start
    ;;
  reload)
    reload
    ;;
  force-reload)
    reload
    ;;
  status)
    status
    ;;
  *)
    N=/etc/init.d/$NAME
    echo "Usage: $N {start|stop|restart|reload|status}" >&2
    exit 1
    ;;
esac

exit 0
