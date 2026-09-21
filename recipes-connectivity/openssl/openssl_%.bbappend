do_install:append:summitsom:class-native() {
    echo "" >> "${D}${sysconfdir}/ssl/openssl.cnf"
    echo ".include ${sysconfdir}/ssl/openssl.cnf.d" >> "${D}${sysconfdir}/ssl/openssl.cnf"
    install -d "${D}${sysconfdir}/ssl/openssl.cnf.d"
}

PACKAGECONFIG:summitsom = "tls1 tls1_1 legacy"
