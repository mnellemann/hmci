#!/bin/sh
# shellcheck disable=SC2154
# RedHat postTrans script
# $1 == 1  for install
# $1 == 2  for upgrade

#echo "Running RedHat Post Transaction Script with: $@"

# Install
if [ "$1" = "1" ] ; then
    install_config "$service_name" "$service_conf"
    install_service "$service_name" "$service_script_sysv" "$service_script_systemd"
fi

refresh_service "$service_name"

exit 0
