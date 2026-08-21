FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

PACKAGECONFIG:append = " ubihealthd-service"
SRC_URI += "file://ubihealthd.init"

FILES:mtd-utils-ubifs += "${sysconfdir}/init.d/ubihealthd"

INITSCRIPT_PACKAGES = "mtd-utils-ubifs"
INITSCRIPT_NAME:mtd-utils-ubifs = "ubihealthd"
INITSCRIPT_PARAMS:mtd-utils-ubifs = "defaults 16"

do_install:append() {
    install -D -m 0755 "${UNPACKDIR}/ubihealthd.init" \
        "${D}${sysconfdir}/init.d/ubihealthd"
}