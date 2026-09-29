FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

inherit update-rc.d

SRC_URI:append = " file://S41dhcpcd"

INITSCRIPT_NAME = "dhcpcd"
INITSCRIPT_PARAMS = "defaults 41"

do_install:append() {
    install -D -m 0755 "${UNPACKDIR}/S41dhcpcd" \
        "${D}${sysconfdir}/init.d/dhcpcd"
}

FILES:${PN} += "${sysconfdir}/init.d"