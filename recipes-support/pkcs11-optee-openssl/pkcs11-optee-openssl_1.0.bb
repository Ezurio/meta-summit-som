SUMMARY = "OpenSSL PKCS#11 provider configuration for the OP-TEE PKCS#11 token"
DESCRIPTION = "Installs an openssl.cnf.d drop-in that activates pkcs11-provider \
    with OP-TEE's PKCS#11 TA (libckteec.so) as the backend module."
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = "file://pkcs11-optee.cnf"

S = "${UNPACKDIR}"

do_configure[noexec] = "1"
do_compile[noexec] = "1"

do_install() {
    install -d "${D}${sysconfdir}/ssl/openssl.cnf.d"
    sed -e "s|@LIBDIR@|${libdir}|g" \
        "${S}/pkcs11-optee.cnf" > "${D}${sysconfdir}/ssl/openssl.cnf.d/pkcs11-optee.cnf"
}

FILES:${PN} = "${sysconfdir}/ssl/openssl.cnf.d/pkcs11-optee.cnf"

RDEPENDS:${PN} = "openssl pkcs11-provider optee-client"
