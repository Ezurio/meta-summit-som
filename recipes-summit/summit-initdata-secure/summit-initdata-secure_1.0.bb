SUMMARY = "Summit Secure Init Configurations"

LICENSE = "Ezurio-Clause"
LIC_FILES_CHKSUM = "file://LICENSE.ezurio;md5=fd3dd0630b215465b6f50540642d5b93"

inherit allarch systemd update-rc.d

SRC_URI = " \
    file://LICENSE.ezurio \
    file://rootfs-additions/summit-secure/ \
    "

S = "${UNPACKDIR}"

PACKAGES =+ "${PN}-mount-data ${PN}-bluetooth"

FILES:${PN} += " \
    ${systemd_system_unitdir} \
    ${libdir}/tmpfiles.d \
    /data \
    "
FILES:${PN}-mount-data = " \
    ${sysconfdir}/init.d/mount-data \
    ${systemd_system_unitdir}/mount_data.service \
    "
FILES:${PN}-bluetooth = " \
    ${sysconfdir}/init.d/var-lib-bluetooth \
    ${systemd_system_unitdir}/var-lib-bluetooth.mount \
    "

RDEPENDS:${PN} = " \
    keyutils \
    fscryptctl \
    libdevmapper \
    e2fsprogs-mke2fs \
    "

RDEPENDS:${PN}:append:imx8mp-summitsom = " \
    keyctl-caam \
    "

SYSTEMD_PACKAGES = "${PN} ${PN}-mount-data ${PN}-bluetooth"
SYSTEMD_SERVICE:${PN}-mount-data = "mount_data.service"
SYSTEMD_SERVICE:${PN}-bluetooth = "var-lib-bluetooth.mount"
SYSTEMD_SERVICE:${PN} = " \
    var-log-journal.mount \
    "
SYSTEMD_AUTO_ENABLE = "enable"

INITSCRIPT_PACKAGES = "${PN}-mount-data ${PN}-bluetooth"
INITSCRIPT_NAME:${PN}-mount-data = "mount-data"
INITSCRIPT_PARAMS:${PN}-mount-data = "defaults 16"
INITSCRIPT_NAME:${PN}-bluetooth = "var-lib-bluetooth"
INITSCRIPT_PARAMS:${PN}-bluetooth = "defaults 17"

do_fetch[cleandirs] += "${S}/rootfs-additions"

do_install () {
    cp -r --preserve=links,timestamps -t "${D}" \
        ${S}/rootfs-additions/summit-secure/*
    find "${D}" -type f -name .empty -delete

    if ! ${@bb.utils.contains('DISTRO_FEATURES', 'systemd', 'true', 'false', d)}; then
        rm -rf ${D}${sysconfdir}/systemd ${D}${libdir}/systemd ${D}${libdir}/tmpfiles.d
    fi
}
