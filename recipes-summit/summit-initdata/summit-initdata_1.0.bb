SUMMARY = "Summit Init Configurations"

LICENSE = "Ezurio-Clause"
LIC_FILES_CHKSUM = "file://LICENSE.ezurio;md5=fd3dd0630b215465b6f50540642d5b93"

inherit allarch systemd update-rc.d

SRC_URI = " \
    file://LICENSE.ezurio \
    file://rootfs-additions/common/ \
    "

S = "${UNPACKDIR}"

FILES:${PN} += " \
    ${systemd_system_unitdir} \
    ${libdir}/NetworkManager/system-connections \
    ${libdir}/tmpfiles.d \
    ${datadir}/colourbars.jpg \
    /perm \
    "

PACKAGES =+ "${PN}-mountboot ${PN}-fwenv"
FILES:${PN}-mountboot = " \
    ${sysconfdir}/init.d/mountboot \
    ${systemd_system_unitdir}/mount_boot.service \
    "
FILES:${PN}-fwenv = " \
    ${sysconfdir}/init.d/fwenv \
    ${systemd_system_unitdir}/fw_env.service \
    "

SYSTEMD_PACKAGES = "${PN} ${PN}-mountboot ${PN}-fwenv"
SYSTEMD_SERVICE:${PN}-mountboot = "mount_boot.service"
SYSTEMD_SERVICE:${PN}-fwenv = "fw_env.service"

INITSCRIPT_PACKAGES = "${PN}-mountboot ${PN}-fwenv"
INITSCRIPT_NAME:${PN}-mountboot = "mountboot"
INITSCRIPT_PARAMS:${PN}-mountboot = "defaults 14"
INITSCRIPT_NAME:${PN}-fwenv = "fwenv"
INITSCRIPT_PARAMS:${PN}-fwenv = "defaults 15"

RDEPENDS:${PN} = "\
    libubootenv-bin \
    util-linux-blkid \
    util-linux-lsblk \
    iptables \
    iptables-modules \
    u-boot-dumpimage \
    ${PREFERRED_PROVIDER_virtual/bootloader}-env \
    "

RDEPENDS:${PN} += "${PN}-mountboot ${PN}-fwenv"

do_fetch[cleandirs] += "${S}/rootfs-additions"

do_install () {
    cp -r --preserve=links,timestamps -t "${D}" ${S}/rootfs-additions/common/*
    find "${D}" -type f -name .empty -delete
    find "${D}${libdir}/NetworkManager/system-connections" -type f \
        -exec chmod 600 {} \;

    if ${@bb.utils.contains('DISTRO_FEATURES', 'systemd', 'true', 'false', d)}; then
        install -d "${D}${sysconfdir}/systemd/system"
        ln -sf /dev/null \
            "${D}${sysconfdir}/systemd/system/systemd-machine-id-commit.service"
    else
        rm -rf ${D}${sysconfdir}/systemd ${D}${libdir}/systemd ${D}${libdir}/tmpfiles.d
    fi
}

SYSTEMD_AUTO_ENABLE = "enable"
