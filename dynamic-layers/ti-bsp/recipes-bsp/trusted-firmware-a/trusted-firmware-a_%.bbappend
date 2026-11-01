FILESEXTRAPATHS:prepend:summitsom := "${THISDIR}/files:"

SRC_URI:append:am62lxx:summitsom = " \
    file://am62lx_carbon_2g_lp4_50_800.h \
    "

EXTRA_OEMAKE:append:am62lxx:summitsom = "\
    DDR_CFG_HEADER=${UNPACKDIR}/am62lx_carbon_2g_lp4_50_800.h \
    AM62L_DDR_RAM_SIZE=0x80000000ULL \
    "