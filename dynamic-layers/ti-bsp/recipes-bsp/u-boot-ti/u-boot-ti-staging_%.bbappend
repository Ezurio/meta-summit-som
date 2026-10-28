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

SRC_URI:append:k3:summitsom-rescue = " \
    file://uboot-rescue_a53.cfg \
"

SRC_URI:append:k3r5:summitsom-rescue = " \
    file://uboot-rescue_r5.cfg \
"

SRC_URI:append:summitsom-usb-boot:am62lxx = " \
    file://uboot-mfg.cfg \
"

# Binman signs tiboot3, tispl and u-boot.img (custMpk) with the SMPK.
# U-Boot 2026.01 reads SIGNING_KEY; older trees read KEY_PATH.
export SIGNING_KEY = "${SIG_KEY_PATH}/smpk.key"
export KEY_PATH = "${SIG_KEY_PATH}/smpk.key"
# The AM62L binman also signs the u-boot.img FIT with custMpk and puts
# its public key in SPL; binman finds smpk.crt through BINMAN_INDIRS.
# This later make argument overrides the first BINMAN_INDIRS in EXTRA_OEMAKE.
EXTRA_OEMAKE:append = " 'BINMAN_INDIRS=${BINMAN_INDIRS} ${SIG_KEY_PATH}'"

inherit ${@oe.utils.ifelse( \
    '${CLOUD_HSM_BACKEND}', \
    'ti-uboot-key-provider-sign', \
    '')}

require recipes-bsp/u-boot-summit/u-boot-summit-env.inc
