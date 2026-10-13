LICENSE = "GPL-2.0-or-later"
LIC_FILES_CHKSUM = "file://Licenses/README;md5=2ca5f2c35c8cc335f0a19756634782f1"

inherit summit-platform-version pkgconfig python3native

require recipes-bsp/u-boot/u-boot.inc

SRC_URI = "${SUMMIT_EXTERNAL_GIT_URI}/u-boot-som.git;${SUMMIT_EXTERNAL_GIT_SUFFIX}"
SRC_URI:summit-internal = "${SUMMIT_INTERNAL_GIT_URI}/cp_linux-u-boot-som60.git;${SUMMIT_INTERNAL_GIT_SUFFIX}"
# The defconfigs disable OP-TEE; the optee machine feature enables it.
SRC_URI:append = "${@bb.utils.contains('MACHINE_FEATURES', 'optee', ' file://optee.cfg', '', d)}"

S = "${WORKDIR}/git"
B = "${WORKDIR}/build"

COMPATIBLE_MACHINE = "(summitsom-genio)"

DEPENDS += " \
    flex-native bison-native python3-setuptools-native \
    python3-pyelftools-native \
    ${@bb.utils.contains('MACHINE_FEATURES', 'optee', 'virtual/optee-os', '', d)} \
"

EXTRA_OEMAKE:append = " BINMAN_INDIRS='${STAGING_DIR_HOST}/firmware ${DEPLOY_DIR_IMAGE} ${UBOOT_SIGN_KEYDIR}'"
EXTRA_OEMAKE:append = " KEY_PATH=${UBOOT_SIGN_KEYDIR}/${UBOOT_SIGN_KEYNAME}.key"
EXTRA_OEMAKE:append = "${@bb.utils.contains('MACHINE_FEATURES', 'optee', ' TEE=${STAGING_DIR_TARGET}${nonarch_base_libdir}/firmware/tee-pager_v2.bin', '', d)}"

do_compile[depends] += "virtual/bl2:do_deploy"
