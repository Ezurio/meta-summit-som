FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SRC_URI:append:summitsom:mx8mm-generic-bsp = " \
    file://0001-imx8mm-uart4-a53.patch \
    "

EXTRA_OEMAKE:append:summitsom:mx8mm-generic-bsp = " \
    BL32_BASE=0x56000000 \
    "

EXTRA_OEMAKE_EXTRA_BUILD:append:summitsom:mx8mm-generic-bsp = " \
    BL32_BASE=0x56000000 \
    "
