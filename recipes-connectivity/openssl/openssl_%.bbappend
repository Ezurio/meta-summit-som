do_install:append:class-native() {
    echo "" >> "${D}${sysconfdir}/ssl/openssl.cnf"
    echo ".include ${sysconfdir}/ssl/openssl.cnf.d" >> "${D}${sysconfdir}/ssl/openssl.cnf"
    install -d "${D}${sysconfdir}/ssl/openssl.cnf.d"
}
