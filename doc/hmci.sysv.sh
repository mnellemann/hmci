#!/bin/sh
### BEGIN INIT INFO
# Provides:
# Required-Start:    $remote_fs $syslog
# Required-Stop:     $remote_fs $syslog
# Default-Start:     2 3 4 5
# Default-Stop:      0 1 6
# Short-Description: Start daemon at boot time
# Description:       Enable service provided by daemon.
### END INIT INFO

dir="/opt/hmci"
cmd="/opt/hmci/bin/hmci"
args=""
user="nobody"
name="hmci"
description="HMC Insights"

pid_file="/var/run/$name.pid"
stdout_log="/var/log/$name.log"
stderr_log="/var/log/$name.err"

# Java 8+ runtime required - Uncomment and export JAVA_HOME if needed
#JAVA_HOME=/usr/java8_64
#JAVA_HOME=/opt/ibm-semeru-open-XX-jre
#JAVA_HOME=/opt/ibm-semeru-open-XX-jdk
#JAVA_HOME=/opt/ibm/ibm-semeru-certified-XX-jre
#JAVA_HOME=/opt/ibm/ibm-semeru-certified-XX-jdk

# Ensure numbers formatted with dot and not comma for thousand-separator
LC_ALL=C


get_pid() {
    cat "$pid_file"
}

is_running() {
    [ -f "$pid_file" ] && ps -p $(get_pid) > /dev/null 2>&1
}

case "$1" in
    start)
    if is_running; then
        echo "Already started"
    else
        echo "Starting $description"
        cd "$dir" || exit 1
        if [ -z "$user" ]; then
            $cmd $args >> "$stdout_log" 2>> "$stderr_log" &
        else
            su - "$user" $cmd $args >> "$stdout_log" 2>> "$stderr_log" &
        fi
        echo $! > "$pid_file"
        if ! is_running; then
            echo "Unable to start, see $stdout_log and $stderr_log"
            exit 1
        fi
    fi
    ;;
    stop)
    if is_running; then
        echo "Stopping $description.."
        kill $(get_pid)
        for i in 1 2 3 4 5 6 7 8 9 10
        # for i in `seq 10`
        do
            if ! is_running; then
                break
            fi
            sleep 1
        done
        echo

        if is_running; then
            echo "Not stopped; may still be shutting down or shutdown may have failed"
            exit 1
        else
            echo "Stopped"
            if [ -f "$pid_file" ]; then
                rm "$pid_file"
            fi
        fi
    else
        echo "Not running"
    fi
    ;;
    restart)
    $0 stop
    if is_running; then
        echo "Unable to stop, will not attempt to start"
        exit 1
    fi
    $0 start
    ;;
    status)
    if is_running; then
        echo "Running"
    else
        echo "Stopped"
        exit 1
    fi
    ;;
    *)
    echo "Usage: $0 {start|stop|restart|status}"
    exit 1
    ;;
esac

exit 0
