FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_URI += " \
    file://0001-mtk-gen4-wlan-quiet-expected-log-noise.patch \
    file://0002-mtk-gen4-wlan-disable-all-log.patch \
    file://0003-btmtksdio-subsys-reset-driver-own.patch \
    "
