#!/bin/sh
# shellcheck disable=SC2154
# RedHat pre Uninstall script
# $1 == 1  for upgrade
# $1 == 0  for uninstall

#echo "Running RedHat Pre Un-Install Script with: $@"

# Uninstall
if [ "$1" = "0" ] ; then
    remove_config "$service_name"
    remove_service "$service_name"
fi

exit 0
