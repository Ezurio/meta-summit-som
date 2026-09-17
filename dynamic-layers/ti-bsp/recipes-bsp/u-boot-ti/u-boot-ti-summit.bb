require recipes-bsp/u-boot/u-boot-ti.inc

inherit summit-platform-version

SUMMARY = "Summit U-Boot for TI devices"

UBOOT_GIT_URI = "${SUMMIT_EXTERNAL_GIT_URI}/u-boot-som.git"
UBOOT_GIT_URI:summit-internal = "${SUMMIT_INTERNAL_GIT_URI}/cp_linux-u-boot-som60.git"

UBOOT_GIT_PROTOCOL = "${SUMMIT_EXTERNAL_GIT_PROTOCOL}"
UBOOT_GIT_PROTOCOL:summit-internal = "${SUMMIT_INTERNAL_GIT_PROTOCOL}"

UBOOT_GIT_BRANCH = "${SUMMIT_PLATFORM_BRANCH}"

export KEY_PATH = "${UBOOT_SIGN_KEYDIR}/${UBOOT_SIGN_KEYNAME}.key"

ENV_INCLUDE = ""
ENV_INCLUDE:k3 = "recipes-bsp/u-boot-summit/u-boot-summit-env.inc"

require ${ENV_INCLUDE}

inherit ${@oe.utils.ifelse( \
    '${CLOUD_HSM_BACKEND}', \
    'ti-uboot-key-provider-sign', \
    '')}

do_deploy:append:k3r5() {
    if [ "${SECURE_BOOT}" = "1" ]; then
        ln -sf tiboot3-*-hs-carbon.bin "${DEPLOYDIR}/tiboot3.bin"
    fi
}

COMPATIBLE_MACHINE = "(ti-soc)"
