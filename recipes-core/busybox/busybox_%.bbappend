FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_URI:append:summitsom = "\
        file://ifconfig.cfg \
        file://fdisk.cfg \
        file://head.cfg \
        file://ps.cfg \
        "
