FILESEXTRAPATHS:prepend:summitsom := "${THISDIR}/files:"

SPLASH_IMAGES:summitsom = "file://ezurio-logo-img.h;outsuffix=default"

SRC_URI:append:summitsom = " \
    file://0001-double-buffering.patch \
    file://0002-psplash-custom-color.patch \
    file://0003-psplash-18bpp.patch \
    "
