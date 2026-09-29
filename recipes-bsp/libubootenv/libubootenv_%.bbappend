FILESEXTRAPATHS:prepend:summitsom := "${THISDIR}/${PN}:"

SRC_URI:append:summitsom = " \
        file://1000-Make-libyaml-optional.patch \
        file://1002-mtd-name.patch \
        file://1003-crc32.patch \
        file://1004-redund.patch \
        "

DEPENDS:summitsom = ""

EXTRA_OECMAKE:append:summitsom = " -DNO_YML_SUPPORT=ON"
