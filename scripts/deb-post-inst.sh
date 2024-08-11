#!/bin/sh
# shellcheck disable=SC2154
# Debian post install script
# See https://wiki.debian.org/MaintainerScripts
# $1 == "configure"
# $1 == "upgrade"

#echo "Running Debian Post Install Script with: $@"

# Configure package
if [ "$1" = "configure" ] ; then
    install_config "$service_name" "$service_conf"
    install_service "$service_name" "$service_script_sysv" "$service_script_systemd"
fi

# Upgrade package
if [ "$1" = "upgrade" ] ; then
    refresh_servie  "$service_name"
fi
