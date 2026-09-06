FILESEXTRAPATHS:prepend:summitsom := "${THISDIR}/${PN}:"

SRC_URI:append:summitsom = "file://sound-drv.conf"

FILES:${PN} += "${systemd_system_unitdir}"

do_install:append:summitsom() {
    if ${@bb.utils.contains('DISTRO_FEATURES','systemd','true','false',d)}; then
        install -D -m 0644 -t ${D}${systemd_system_unitdir}/alsa-restore.service.d/ \
            ${UNPACKDIR}/sound-drv.conf
    fi
}
