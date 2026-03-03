SUMMARY = "Summit Init Configurations"

LICENSE = "Ezurio"
NO_GENERIC_LICENSE[Ezurio] = "LICENSE.ezurio"
LIC_FILES_CHKSUM = "file://LICENSE.ezurio;md5=fd3dd0630b215465b6f50540642d5b93"

inherit allarch systemd

do_fetch[cleandirs] += "${S}/rootfs-additions"

SRC_URI = " \
    file://LICENSE.ezurio \
    file://rootfs-additions/common/ \
    "

SRC_URI:append:summit-secure = " \
    file://rootfs-additions/summit-secure/ \
    "

SRC_URI:append:imx8mp-summitsom = " \
    file://rootfs-additions/imx8mp-summitsom/ \
    "

S = "${WORKDIR}"

FILES:${PN} += " \
    ${sbindir} \
    ${libdir} \
    ${nonarch_libdir} \
    ${systemd_system_unitdir} \
    ${sysconfdir} \
    ${datadir} \
    /perm /data \
    "

RDEPENDS:${PN} = "\
    libubootenv-bin \
    util-linux-blkid \
    util-linux-lsblk \
    iptables \
    ${PREFERRED_PROVIDER_virtual/bootloader}-env \
    "

RDEPENDS:${PN}:append:summit-secure = "\
    keyutils \
    libdevmapper \
    lvm2-udevrules \
    e2fsprogs-mke2fs \
    "

RDEPENDS:${PN}:append:imx8mp-summitsom:summit-secure = " keyctl-caam"

CUSTOM_DIRS = "${S}/rootfs-additions/common/*"
CUSTOM_DIRS:append:summit-secure = " ${S}/rootfs-additions/summit-secure/*"
CUSTOM_DIRS:append:imx8mp-summitsom = " ${S}/rootfs-additions/imx8mp-summitsom/*"

do_install () {
    cp -r --preserve=links,timestamps -t "${D}" ${CUSTOM_DIRS}
    find "${D}" -type f -name .empty -delete
    find "${D}${libdir}/NetworkManager/system-connections" -type f \
        -exec chmod 600 {} \;
    install -d "${D}${sysconfdir}/systemd/system"
    ln -sf /dev/null \
        "${D}${sysconfdir}/systemd/system/systemd-machine-id-commit.service"
}

SYSTEMD_SERVICE:${PN} = "mount_boot.service fw_env.service"
SYSTEMD_AUTO_ENABLE = "enable"

SYSTEMD_SERVICE:${PN}:append:summit-secure = "\
    mount_data.service \
    var-lib-bluetooth.mount \
    var-log-journal.mount \
    "
