FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_URI:append:summitsom = " \
    file://dts \
    ${KERNEL_CONFIG_SUMMIT} \
    "
