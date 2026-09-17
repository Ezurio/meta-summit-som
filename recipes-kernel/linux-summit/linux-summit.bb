
LICENSE = "GPL-2.0-only"
LIC_FILES_CHKSUM = "file://COPYING;md5=6bc538ed5bd9a7fc9398086aedcd7e46"

inherit summit-platform-version kernel-yocto kernel
inherit ${@oe.utils.ifelse( \
    '${CLOUD_HSM_BACKEND}', \
    'fitimage-key-provider-sign', \
    '')}

SRC_URI = "${SUMMIT_EXTERNAL_GIT_URI}/wb-kernel.git;${SUMMIT_EXTERNAL_GIT_SUFFIX}"
SRC_URI:summit-internal = "${SUMMIT_INTERNAL_GIT_URI}/cp_linux-wb45n-kernel.git;${SUMMIT_INTERNAL_GIT_SUFFIX}"

KERNEL_DTC_FLAGS += "${@' -@' if d.getVar('KERNEL_DEVICETREE').find('.dtbo') else ''}"

KERNEL_VERSION_SANITY_SKIP = "1"

DEPENDS:append:summitsom:mx8m-generic-bsp = " firmware-imx"
do_compile:prepend:summitsom:mx8m-generic-bsp () {
    cp -af -t "${S}" "${STAGING_LIBDIR}/firmware"
}
