PACKAGECONFIG:append:imx8mp-summitsom = " modemmanager"

do_install:append:summitsom() {
    if ${@bb.utils.contains('DISTRO_FEATURES', 'systemd', 'true', 'false', d)}; then
		sed -i 's,--no-daemon,--no-daemon --state-file=${sysconfdir}/NetworkManager/NetworkManager.state,' \
			"${D}${systemd_system_unitdir}/NetworkManager.service"
	fi

	sed -i '/^PIDFILE=/a DAEMON_OPTS="--state-file=${sysconfdir}/NetworkManager/NetworkManager.state"' \
		"${D}${sysconfdir}/init.d/network-manager"
}
