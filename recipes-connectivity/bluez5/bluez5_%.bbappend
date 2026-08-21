
FILESEXTRAPATHS:prepend:summitsom := "${THISDIR}/${PN}:"

SRC_URI:append:summitsom = "\
    file://1001-bluetooth-get_conn_info-auto_connect-disconnect_reason.patch \
    "

do_install:append:summitsom () {
   install -D -m 0644 "${S}/src/main.conf" "${D}${sysconfdir}/bluetooth/main.conf"

   if "${@bb.utils.contains('DISTRO_FEATURES', 'systemd', 'true', '', d)}"; then
       sed -i 's/ConfigurationDirectoryMode=0555/ConfigurationDirectoryMode=0755/g' \
            "${D}/usr/lib/systemd/system/bluetooth.service"
   fi
}

PACKAGES:prepend:summitsom = "${PN}-deprecated "

FILES:${PN}-deprecated = "\
    ${bindir}/hciattach \
    ${bindir}/hciconfig \
    ${bindir}/hcitool \
    ${bindir}/hcidump \
    ${bindir}/rfcomm \
    ${bindir}/sdptool \
    ${bindir}/ciptool \
    "
