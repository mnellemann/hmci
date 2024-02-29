#!/bin/sh

remove_config() {
    echo "Leaving /etc/${service_name}.toml for manual deletion."
    #test -f "/etc/${service_name}.toml" && mv "/etc/${service_name}.toml" "/etc/${service_name}.toml.bak"
}

remove_systemd() {
    sysctl=$(command -v deb-systemd-invoke || echo systemctl)
    $sysctl stop "${service_name}" >/dev/null || true
    $sysctl disable "${service_name}" >/dev/null || true
    test -f "/etc/systemd/system/${service_name}.service" && rm -f "/etc/systemd/system/${service_name}.service"
    $sysctl --system daemon-reload >/dev/null || true
}

remove_sysv_linux() {
    echo "WARN: No support for ${service_name} on SysV Linux"
}

remove_sysv_aix() {
    /etc/rc.d/init.d/${service_name} stop
    rm -f "/etc/rc.d/init.d/${service_name}" "/etc/rc.d/rc2.d/K${service_name}" "/etc/rc.d/rc2.d/S${service_name}"
}

remove_sysv() {
    if [ x$(uname | grep AIX) = x"" ]; then
        remove_sysv_linux
    else
        remove_sysv_aix
    fi
}

# Remove configuration file
remove_config

# Detect if we are running on a systemd based Linux
if [ x$(command -v systemctl) = x"" ]; then
    remove_sysv
else
    remove_systemd
fi

exit 0
