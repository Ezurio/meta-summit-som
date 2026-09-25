# Persistent ELE key stores require the NVM daemon to be available before HSM
# clients open the store. Limit automatic enablement to secure i.MX93 images
# where summit-prov-imx uses persistent OEM_IMPORT_MK_SK keys.
SYSTEMD_AUTO_ENABLE:mx93-generic-bsp:summit-secure = "enable"

FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_URI:append:mx93-generic-bsp:summit-secure = " file://10-summit-persistent-storage.conf"

FILES:${PN}:append:mx93-generic-bsp:summit-secure = " \
	${systemd_system_unitdir}/nvm_daemon.service.d/10-summit-persistent-storage.conf \
"

do_install:append:mx93-generic-bsp:summit-secure() {
	install -D -m 0644 \
		"${UNPACKDIR}/10-summit-persistent-storage.conf" \
		"${D}${systemd_system_unitdir}/nvm_daemon.service.d/10-summit-persistent-storage.conf"
}