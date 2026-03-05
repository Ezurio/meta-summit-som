FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_URI:append:summitsom = "\
    file://Ezurio_logo-White_Red.png \
    file://weston-init-require-display.patch \
"

FILES:${PN}:append:summitsom = " \
    ${datadir}/backgrounds/Ezurio_logo-White_Red.png \
"

do_install:append:summitsom() {
    install -D -m 0644 -t "${D}${datadir}/backgrounds" \
        "${WORKDIR}/Ezurio_logo-White_Red.png"

    sed -i -E -e '/^#?\[shell\]/ s/^#//' \
        -e "/\[shell\]/a\background-image=${datadir}/backgrounds/Ezurio_logo-White_Red.png\nbackground-type=centered\nbackground-color=0xFF000000" \
        "${D}${sysconfdir}/xdg/weston/weston.ini"
}
