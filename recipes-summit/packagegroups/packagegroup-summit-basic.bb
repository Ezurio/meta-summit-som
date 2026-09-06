SUMMARY = "Summit SOM Basic Components"
SECTION = "net/misc"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit packagegroup

RDEPENDS:${PN} = " \
    kernel-modules  \
    ca-certificates \
    tzdata-core \
    tzdata-posix \
    iproute2 \
    chrony \
    chronyc \
    gptfdisk \
    optee-client \
    libgpiod \
    libgpiod-tools \
    util-linux-hwclock \
    pkcs11-provider \
    pkcs11-optee-openssl \
    summit-automount \
    summit-initdata \
    summit-update \
    summit-usbgadget \
    ${@bb.utils.contains('COMBINED_FEATURES', 'wifi', 'packagegroup-summit-radio-stack-som', '', d)} \
    ${@bb.utils.contains('COMBINED_FEATURES', 'alsa', 'alsa-utils-alsamixer alsa-utils-aplay', '', d)} \
    ${@bb.utils.contains('DISTRO_FEATURES', 'systemd', 'less systemd-extra-utils', '', d)} \
    ${@bb.utils.contains('MACHINE_FEATURES', 'nand', 'mtd-utils mtd-utils-ubifs', '', d)} \
    "

RDEPENDS:${PN}:append:summit-secure = " \
    summit-initdata-secure \
    "

RDEPENDS:${PN}:append:k3 = " \
    iproute2-devlink \
    summit-prov \
    "

RDEPENDS:${PN}:remove:summitsom-mfg = " \
    tzdata-core \
    tzdata-posix \
    chrony \
    chronyc \
    gptfdisk \
    pkcs11-provider \
    pkcs11-optee-openssl \
    summit-prov \
    "