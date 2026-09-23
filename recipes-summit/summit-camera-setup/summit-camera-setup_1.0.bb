SUMMARY = "Summit SOM DVK Camera Setup"

LICENSE = "Ezurio"
NO_GENERIC_LICENSE[Ezurio] = "LICENSE.ezurio"
LIC_FILES_CHKSUM = "file://LICENSE.ezurio;md5=fd3dd0630b215465b6f50540642d5b93"

inherit allarch systemd

SYSTEMD_SERVICE_${PN} = "camera-handler@.service"

SRC_URI = " \
    file://LICENSE.ezurio \
    file://99-camera-devices.rules \
    file://99-camera-devices-sysv.rules \
    file://camera-display.sh \
    file://camera-handler@.service \
    file://camera-setup.sh \
    "

RDEPENDS:${PN} = "\
    v4l-utils \
    media-ctl \
    "

S = "${WORKDIR}"

FILES:${PN} += "${systemd_unitdir} ${sysconfdir}"

do_install () {
    install -D -m 0755 -t "${D}${bindir}" "${S}"/*.sh

    if ${@bb.utils.contains('DISTRO_FEATURES', 'systemd', 'true', 'false', d)}; then
        install -D -m 0644 -t "${D}${systemd_unitdir}/system" \
            "${S}/camera-handler@.service"
        install -D -m 0644 -t "${D}${nonarch_base_libdir}/udev/rules.d" \
            "${S}/99-camera-devices.rules"
    else
        install -D -m 0644 -t "${D}${nonarch_base_libdir}/udev/rules.d" \
            "${S}/99-camera-devices-sysv.rules"
    fi
}
