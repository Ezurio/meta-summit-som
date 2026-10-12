FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_URI:append:summitsom = " \
    file://0001-ipa-initialize-stage-gain.patch \
"
