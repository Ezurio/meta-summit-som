FILESEXTRAPATHS:prepend:summitsom := "${THISDIR}/${PN}:"

SRC_URI:append:summitsom = " \
    file://0001-carbon-am62l-uboot.patch \
    "

SRC_URI:append:k3:summitsom-usb-boot = " \
    file://uboot-usb_a53.cfg \
"

SRC_URI:append:k3r5:summitsom-usb-boot = " \
    file://uboot-usb_r5.cfg \
"

SRC_URI:append:summitsom-usb-boot:am62lxx = " \
    file://uboot-mfg.cfg \
"

export KEY_PATH = "${UBOOT_SIGN_KEYDIR}/${UBOOT_SIGN_KEYNAME}.key"

require recipes-bsp/u-boot-summit/u-boot-summit-env.inc
