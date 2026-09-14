SUMMARY = "Summit SOM Diagnostic and debugging tools"
SECTION = "net/misc"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit packagegroup

RDEPENDS:${PN} = " \
    iperf2 \
    iperf3 \
    htop \
    tcpdump \
    can-utils \
    stress-ng \
    mc-mint \
    i2c-tools \
    spitools \
    mdio-tools \
    libinput \
    libinput-bin \
    evtest \
    linuxptp \
    con2fbmap \
    ncurses-terminfo \
    util-linux-dmesg \
    ${@bb.utils.contains('COMBINED_FEATURES', 'usbhost', 'usbutils', '', d)} \
    ${@bb.utils.contains('MACHINE_FEATURES', 'pci', 'pciutils', '', d)} \
    ${@bb.utils.contains('COMBINED_FEATURES', 'alsa', 'alsa-utils-speaker-test mpg123', '', d)} \
    ${@bb.utils.contains('DISTRO_FEATURES', 'systemd', 'systemd-analyze', '', d)} \
    ${@bb.utils.contains('DISTRO_FEATURES', 'opengl', 'fbida', '', d)} \
    "
