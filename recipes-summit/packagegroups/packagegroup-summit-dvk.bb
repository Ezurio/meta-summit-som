SUMMARY = "Summit SOM DVK"
SECTION = "net/misc"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

inherit packagegroup

RDEPENDS:${PN} = " \
    summit-set-mode \
    python3 \
    python3-dbus-fast \
    ${@bb.utils.contains('MACHINE_FEATURES', 'alsa', 'kernel-module-tac5x1x', '', d)} \
    ${@bb.utils.contains('MACHINE_FEATURES', 'camera', 'packagegroup-summit-camera', '', d)} \
    "

RDEPENDS:${PN}:append:imx8mp-summitsom = " \
    imx8mp-summitsom-dvk-support \
    "

RDEPENDS:${PN}:append:imx91-nitrogen-osm-s = " \
    summit-rcm-rust \
    "

RDEPENDS:${PN}:append:am62l-carbon = " \
    summit-rcm-rust \
    "

RDEPENDS:${PN}:append:am62l-carbon-emmc = " \
    summit-rcm-rust \
    "
