# native.bbclass clears MACHINEOVERRIDES, so a machine override never matches here.
do_install:append:class-native() {
    echo "" >> "${D}${sysconfdir}/ssl/openssl.cnf"
    echo ".include ${sysconfdir}/ssl/openssl.cnf.d" >> "${D}${sysconfdir}/ssl/openssl.cnf"
    install -d "${D}${sysconfdir}/ssl/openssl.cnf.d"
}

PACKAGECONFIG:append:class-target:summitsom = " tls1 tls1_1 legacy"
