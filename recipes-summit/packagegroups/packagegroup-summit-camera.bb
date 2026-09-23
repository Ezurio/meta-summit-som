SUMMARY = "Summit SOM Camera support tools"
SECTION = "net/misc"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

inherit packagegroup

RDEPENDS:${PN} = " \
    v4l-utils \
    gstreamer1.0 \
    gstreamer1.0-plugins-base \
    gstreamer1.0-plugins-good \
    gstreamer1.0-plugins-bad \
    gstreamer1.0-plugins-base-meta \
    gstreamer1.0-plugins-good-meta \
    gstreamer1.0-plugins-bad-meta \
    ${@bb.utils.contains('LICENSE_FLAGS_ACCEPTED', 'commercial', 'gstreamer1.0-libav', '', d)} \
    summit-camera-setup \
    libcamera \
    kernel-module-pivariety \
    "
