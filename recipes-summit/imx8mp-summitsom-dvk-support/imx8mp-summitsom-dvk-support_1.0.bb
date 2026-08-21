SUMMARY = "Summit SOM DVK Init Configurations"

LICENSE = "Ezurio-Clause"
LIC_FILES_CHKSUM = "file://LICENSE.ezurio;md5=fd3dd0630b215465b6f50540642d5b93"

inherit allarch systemd update-rc.d

SRC_URI = " \
    file://LICENSE.ezurio \
    file://modem \
    file://gpio-init.service \
    file://gpio-init.init \
    file://somlte.nmconnection \
    "

S = "${UNPACKDIR}"

RDEPENDS:${PN} = " \
    libgpiod-tools \
    summit-networkmanager-wwan \
    qfirehose \
    "

SYSTEMD_SERVICE:${PN} = "gpio-init.service"
SYSTEMD_AUTO_ENABLE = "enable"

INITSCRIPT_NAME = "gpio-init"
INITSCRIPT_PARAMS = "defaults 01"

do_install() {
    install -D -m 0755 "${S}/modem" \
        "${D}${bindir}/modem"
    install -D -m 0600 "${S}/somlte.nmconnection" \
        "${D}${libdir}/NetworkManager/system-connections/somlte.nmconnection"

    if ${@bb.utils.contains('DISTRO_FEATURES', 'systemd', 'true', 'false', d)}; then
        install -D -m 0644 "${S}/gpio-init.service" \
            "${D}${systemd_system_unitdir}/gpio-init.service"
    fi
    install -D -m 0755 "${S}/gpio-init.init" \
        "${D}${sysconfdir}/init.d/gpio-init"
}