SUMMARY = "Summit U-Boot for Ezurio boards"
LICENSE = "GPL-2.0-or-later"
LIC_FILES_CHKSUM = "file://Licenses/README;md5=2ca5f2c35c8cc335f0a19756634782f1"

inherit summit-platform-version use-imx-security-controller-firmware

require recipes-bsp/u-boot/u-boot.inc
require recipes-bsp/u-boot-summit/u-boot-summit-env.inc

SRC_URI = "${SUMMIT_EXTERNAL_GIT_URI}/u-boot-som.git;${SUMMIT_EXTERNAL_GIT_SUFFIX}"
SRC_URI:summit-internal = "${SUMMIT_INTERNAL_GIT_URI}/cp_linux-u-boot-som60.git;${SUMMIT_INTERNAL_GIT_SUFFIX}"

SRC_URI:append:summitsom-rescue-initramfs = " \
    file://uboot-usb.cfg \
    file://uboot-mfg.cfg \
"

SRC_URI:append:summitsom-mfg = " \
    file://uboot-mfg.cfg \
"

B = "${UNPACKDIR}/build"

PROVIDES += "u-boot"

DEPENDS += "\
    flex-native \
    bison-native \
    python3-setuptools-native \
    python3-pyelftools-native \
    ${IMX_EXTRA_FIRMWARE} \
    imx-atf \
    ${@bb.utils.contains('MACHINE_FEATURES', 'optee', 'optee-os', '', d)} \
    "

include ${@'u-boot-imx-summit-secure.inc' if 'summit-secure' in d.getVar('OVERRIDES').split(':') else ''}

do_compile[depends] += " \
    ${@' '.join('%s:do_deploy' % r for r in '${IMX_EXTRA_FIRMWARE}'.split() )} \
    imx-atf:do_deploy \
    "

ATF_MACHINE_NAME ?= "${@bb.utils.contains('MACHINE_FEATURES', 'optee', "bl31-${ATF_PLATFORM}.bin-optee", "bl31-${ATF_PLATFORM}.bin", d)}"

EXTRA_OEMAKE += " \
    ${@bb.utils.contains('MACHINE_FEATURES', 'optee', "TEE=${STAGING_LIBDIR}/firmware/tee-pager_v2.bin", '', d)} \
    BL31=${DEPLOY_DIR_IMAGE}/${ATF_MACHINE_NAME} \
    BINMAN_INDIRS=${DEPLOY_DIR_IMAGE} \
    BINMAN_VERBOSE=3 \
    KEY_PATH=${UBOOT_SIGN_KEYDIR}/${UBOOT_SIGN_KEYNAME}.key \
    "

do_compile:prepend:mx9-generic-bsp() {
    if ${@bb.utils.contains('MACHINE_FEATURES', 'optee', 'true', 'false', d)}; then
        ln -sf "${STAGING_LIBDIR}/firmware/tee-pager_v2.bin" "${B}/tee.bin"
    fi
    ln -sf "${DEPLOY_DIR_IMAGE}/${ATF_MACHINE_NAME}" "${B}/bl31.bin"
    ln -sf "${DEPLOY_DIR_IMAGE}/${SECO_FIRMWARE_NAME}" "${B}/${SECO_FIRMWARE_NAME}"
}

do_compile:prepend:mx95-generic-bsp() {
    ln -sf "${DEPLOY_DIR_IMAGE}/${SYSTEM_MANAGER_FIRMWARE_NAME}.bin" "${B}/m33_image.bin"
}

COMPATIBLE_MACHINE = "(imx-generic-bsp)"
