FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_URI:append:mx95-generic-bsp:summitsom = "\
     file://0001-boards-mcimx95evk-remove-pcal6408-references.patch \
     file://0002-mx95evk-move-can1-to-a55-core.patch \
     "
