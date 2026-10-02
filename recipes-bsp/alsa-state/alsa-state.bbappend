FILESEXTRAPATHS:prepend:summitsom:summitsom_smarc := "${THISDIR}/${PN}/smarc:"
FILESEXTRAPATHS:prepend:summitsom:summitsom_osm := "${THISDIR}/${PN}/osm:"
FILESEXTRAPATHS:prepend:summitsom:imx8mp-summitsom := "${THISDIR}/${PN}/imx8mp-summitsom:"

SRC_URI:append:summitsom:summitsom_smarc = " file://sound-drv.conf"
SRC_URI:append:summitsom:summitsom_osm = " file://sound-drv.conf"
SRC_URI:append:summitsom:imx8mp-summitsom = " file://sound-drv.conf"

FILES:${PN}:append:summitsom:summitsom_smarc = " ${systemd_system_unitdir}"
FILES:${PN}:append:summitsom:summitsom_osm = " ${systemd_system_unitdir}"
FILES:${PN}:append:summitsom:imx8mp-summitsom = " ${systemd_system_unitdir}"

do_install:append:summitsom:summitsom_smarc() {
    if ${@bb.utils.contains('DISTRO_FEATURES','systemd','true','false',d)}; then
        install -D -m 0644 -t ${D}${systemd_system_unitdir}/alsa-restore.service.d/ \
            ${UNPACKDIR}/sound-drv.conf
    fi
}

do_install:append:summitsom:summitsom_osm() {
    if ${@bb.utils.contains('DISTRO_FEATURES','systemd','true','false',d)}; then
        install -D -m 0644 -t ${D}${systemd_system_unitdir}/alsa-restore.service.d/ \
            ${UNPACKDIR}/sound-drv.conf
    fi
}

do_install:append:summitsom:imx8mp-summitsom() {
    if ${@bb.utils.contains('DISTRO_FEATURES','systemd','true','false',d)}; then
        install -D -m 0644 -t ${D}${systemd_system_unitdir}/alsa-restore.service.d/ \
            ${UNPACKDIR}/sound-drv.conf
    fi
}
