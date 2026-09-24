SUMMARY = "Summit Auto Mount"

LICENSE = "Ezurio"
NO_GENERIC_LICENSE[Ezurio] = "LICENSE.ezurio"
LIC_FILES_CHKSUM = "file://LICENSE.ezurio;md5=fd3dd0630b215465b6f50540642d5b93"

inherit allarch

SRC_URI = " \
    file://LICENSE.ezurio \
    file://90-usbmount.rules \
    file://91-mmcmount.rules \
    file://usb-mount.sh \
    file://01-private-mount.conf \
    "

MMC_USER ?= ""
USB_USER ?= ""

do_configure[noexec] = "1"
do_compile[noexec] = "1"

ALLOW_EMPTY:${PN}-dev = "0"
ALLOW_EMPTY:${PN}-dbg = "0"

S = "${WORKDIR}"

FILES:${PN} += " \
    ${sysconfdir}/udev/rules.d \
    ${sysconfdir}/default \
    ${systemd_system_unitdir} \
    "

do_install () {
    install -D -m 0755 -t "${D}${bindir}" "${S}/usb-mount.sh"
    install -D -m 0644 -t "${D}${sysconfdir}/udev/rules.d" \
        "${S}/90-usbmount.rules" "${S}/91-mmcmount.rules"
    install -D -m 0644 -t ${D}${systemd_system_unitdir}/systemd-udevd.service.d \
        ${S}/01-private-mount.conf

    install -d "${D}${sysconfdir}/default"
    {
        echo "MOUNT_USER_MMC=${MMC_USER}"
        echo "MOUNT_USER_USB=${USB_USER}"
    } > "${D}${sysconfdir}/default/usb-mount"
}