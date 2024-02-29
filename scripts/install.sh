#!/bin/sh

install_config() {
    test -f "/etc/${service_name}.toml" || cp "${service_conf}" "/etc/${service_name}.toml"
}

install_systemd() {
    sysctl=$(command -v deb-systemd-invoke || echo systemctl)
    test -f "/etc/systemd/system/${service_name}.service" || cp "${service_systemd}" "/etc/systemd/system/${service_name}.service"
    $sysctl --system daemon-reload >/dev/null || true
    if ! $sysctl is-enabled "${service_name}" >/dev/null
    then
        $sysctl enable "${service_name}" >/dev/null || true
        $sysctl start "${service_name}" >/dev/null || true
    else
        $sysctl restart "${service_name}" >/dev/null || true
    fi
}

install_sysv_linux() {
    echo "WARN: No support for "${service_name}" on SysV Linux"
}

install_sysv_aix() {
    test -f "/etc/rc.d/init.d/${service_name}" || cp "${service_sysv}" "/etc/rc.d/init.d/${service_name}"
    chmod 0755 "/etc/rc.d/init.d/${service_name}"
    ln -sf "/etc/rc.d/init.d/${service_name}" "/etc/rc.d/rc2.d/S${service_name}"
    ln -sf "/etc/rc.d/init.d/${service_name}" "/etc/rc.d/rc2.d/K${service_name}"
}

install_sysv() {
    if [ x$(uname | grep AIX) = x"" ]; then
        install_sysv_linux
    else
        install_sysv_aix
    fi
}

# Install configuration file
install_config

# Detect if we are running on a systemd based Linux
if [ x$(command -v systemctl) = x"" ]; then
    install_sysv
else
    install_systemd
fi

exit 0
