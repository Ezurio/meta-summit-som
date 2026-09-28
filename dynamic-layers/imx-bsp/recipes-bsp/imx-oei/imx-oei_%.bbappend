FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_URI:append:summitsom = "\
    file://NITROGEN_IMX95_LPDDR5_6400MTS_timing.c \
    file://NITROGEN_IMX95_LPDDR5_6400MTS_ecc_timing.c \
    "

python do_patch:append:summitsom () {
    bb.utils.copyfile(
        d.expand("${WORKDIR}/NITROGEN_IMX95_LPDDR5_6400MTS_timing.c"), 
        d.expand("${S}/boards/mx95lp5/ddr/NITROGEN_IMX95_LPDDR5_6400MTS_timing.c"), 
        True)
    bb.utils.copyfile(
        d.expand("${WORKDIR}/NITROGEN_IMX95_LPDDR5_6400MTS_ecc_timing.c"), 
        d.expand("${S}/boards/mx95lp5/ddr/NITROGEN_IMX95_LPDDR5_6400MTS_ecc_timing.c"), 
        True)
}
