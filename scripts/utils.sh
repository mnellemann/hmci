#!/bin/sh


###
### Install Logic
###

install_config() {
    service_name=$1
    config_source=$2

    # Leave any existing configuration file
    test -f "/etc/${service_name}.toml" && return

    # Or install a previously used configuration file
    test -f "/etc/${service_name}.bak" && mv "/etc/${service_name}.bak" "/etc/${service_name}.toml"

    # Or install the default configuration file
    test -f "/etc/${service_name}.toml" || cp "${config_source}" "/etc/${service_name}.toml"
}

install_service_sysv_aix() {
    service_name=$1
    sysv_source=$2

    test -f "/etc/rc.d/init.d/${service_name}" || cp "${sysv_source}" "/etc/rc.d/init.d/${service_name}"
    chmod 0755 "/etc/rc.d/init.d/${service_name}"
    ln -sf "/etc/rc.d/init.d/${service_name}" "/etc/rc.d/rc2.d/S${service_name}"
    ln -sf "/etc/rc.d/init.d/${service_name}" "/etc/rc.d/rc2.d/K${service_name}"
}

install_service_sysv() {
    service_name=$1
    sysv_source=$2

    # shellcheck disable=SC2046
    if [ $(uname | grep AIX) = "" ]; then
        echo "WARN: No support for ${service_name} on SysV Linux"
    else
        install_service_sysv_aix "$service_name" "$sysv_source"
    fi
}

# shellcheck disable=SC2120
install_service_systemd() {
    service_name=$1
    systemd_source=$2

    sysctl=$(command -v deb-systemd-invoke || echo systemctl)
    test -f "/etc/systemd/system/${service_name}.service" || cp "${systemd_source}" "/etc/systemd/system/${service_name}.service"
    $sysctl --system daemon-reload >/dev/null || true
    if ! $sysctl is-enabled "${service_name}" >/dev/null
    then
        $sysctl enable "${service_name}" >/dev/null || true
        $sysctl start "${service_name}" >/dev/null || true
    else
        $sysctl restart "${service_name}" >/dev/null || true
    fi
}


install_service() {
    service_name=$1
    service_source_sysv=$2
    service_source_systemd=$3

    # Detect if we are running on a systemd based Linux
    # shellcheck disable=SC2046
    # shellcheck disable=SC2268
    if [ x$(command -v systemctl) = x"" ]; then
        install_service_sysv "$service_name" "$service_source_sysv"
    else
        install_service_systemd "$service_name" "$service_source_systemd"
    fi
}



###
### Upgrade Logic
###

refresh_service_systemd() {
    service_name=$1

    $sysctl restart "${service_name}" >/dev/null || true
}


refresh_service_sysv_aix() {
    service_name=$1

    "/etc/rc.d/init.d/${service_name}" restart
}

refresh_service_sysv() {
    service_name=$1

    # shellcheck disable=SC2046
    if [ $(uname | grep AIX) = "" ]; then
        echo "WARN: No support for ${service_name} on SysV Linux"
    else
        refresh_service_sysv_aix "$service_name"
    fi
}

refresh_service() {
    service_name=$1

    # Detect if we are running on a systemd based Linux
    # shellcheck disable=SC2046
    # shellcheck disable=SC2268
    if [ x$(command -v systemctl) = x"" ]; then
        refresh_service_sysv "$service_name"
    else
        refresh_service_systemd "$service_name"
    fi
}




###
### Uninstall Logic
###

purge_config() {
    service_name=$1

    test -f "/etc/${service_name}.bak" && rm "/etc/${service_name}.bak"
    test -f "/etc/${service_name}.toml" && rm "/etc/${service_name}.toml"
}

remove_config() {
    service_name=$1

    test -f "/etc/${service_name}.toml" && mv "/etc/${service_name}.toml" "/etc/${service_name}.bak"
}

remove_service_systemd() {
    service_name=$1

    sysctl=$(command -v deb-systemd-invoke || echo systemctl)
    $sysctl stop "${service_name}" >/dev/null || true
    $sysctl disable "${service_name}" >/dev/null || true
    test -f "/etc/systemd/system/${service_name}.service" && rm -f "/etc/systemd/system/${service_name}.service"
    $sysctl --system daemon-reload >/dev/null || true
}

remove_service_sysv_aix() {
    service_name=$1

    /etc/rc.d/init.d/"${service_name}" stop
    rm -f "/etc/rc.d/init.d/${service_name}"
    rm -f "/etc/rc.d/rc2.d/K${service_name}"
    rm -f "/etc/rc.d/rc2.d/S${service_name}"
}

remove_service_sysv() {
    service_name=$1

    # shellcheck disable=SC2046
    if [ $(uname | grep AIX) = "" ]; then
        echo "WARN: No support for ${service_name} on SysV Linux"
    else
        remove_service_sysv_aix "$service_name"
    fi
}

remove_service() {
    service_name=$1

    # Detect if we are running on a systemd based Linux
    # shellcheck disable=SC2268
    if [ x"$(command -v systemctl)" = x"" ]; then
        remove_service_sysv "$service_name"
    else
        remove_service_systemd "$service_name"
    fi

}
