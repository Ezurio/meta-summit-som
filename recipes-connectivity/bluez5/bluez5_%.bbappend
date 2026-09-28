
FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_URI:append = "\
    file://1001-bluetooth-get_conn_info-auto_connect-disconnect_reason.patch \
    file://1002-btattach-Use-cfsetspeed-instead-of-c_cflag-baud-OR.patch \
    file://1003-use-libedit-instead-of-readline.patch \
    "

PACKAGECONFIG[readline] = "--with-readline=readline,,readline,"
PACKAGECONFIG[libedit] = "--with-readline=libedit,,libedit,"
PACKAGECONFIG_CONFARGS:append = " ${@bb.utils.contains_any('PACKAGECONFIG', 'readline libedit', '--enable-client', '--disable-client', d)}"

PACKAGECONFIG:remove:summitsom = "readline"
PACKAGECONFIG:append:summitsom = " libedit"

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
