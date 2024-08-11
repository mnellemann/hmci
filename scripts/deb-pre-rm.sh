#!/bin/sh
# shellcheck disable=SC2154
# Debian pre removal script
# See https://wiki.debian.org/MaintainerScripts
# $1 == "upgrade"
# $1 == "remove"
# $1 == "purge"

#echo "Running Debian Pre Removal Script with: $@"

# shellcheck disable=SC3037
if [ "$1" = "upgrade" ] ; then
    # We don't do anything
    echo -n ""
fi

if [ "$1" = "remove" ] ; then
    remove_service "$service_name"
fi

if [ "$1" = "purge" ] ; then
    remove_config "$service_name"
fi
