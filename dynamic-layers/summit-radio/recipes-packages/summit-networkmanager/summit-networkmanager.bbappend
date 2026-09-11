PACKAGECONFIG:append:imx8mp-summitsom = " modemmanager"

do_install:summitsom:append() {
	sed -i 's,--no-daemon,--no-daemon --state-file=${sysconfdir}/NetworkManager/NetworkManager.state,' \
		"${D}${systemd_system_unitdir}/NetworkManager.service"
}
