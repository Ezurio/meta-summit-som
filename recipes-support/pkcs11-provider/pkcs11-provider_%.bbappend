FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

# RDEPENDS is not consumed for native (no package manager/rootfs step), so
# uri2pem.py needs python3-asn1crypto staged into the native sysroot via DEPENDS.
DEPENDS:append:class-native = " python3-asn1crypto-native"

SRC_URI += " \
    file://0001-uri2pem-exit-non-zero-on-verification-failure.patch \
    file://0002-fix-install-path.patch \
    "

do_install:append() {
    install -m 755 -D -t "${D}${bindir}" "${S}/tools/uri2pem.py"
}

PACKAGES =+ "${PN}-uri2pem"

FILES:${PN} += " \
    ${libdir}/ossl-modules/pkcs11.so \
    "

FILES:${PN}-uri2pem = " \
    ${bindir}/uri2pem.py \
    "

RDEPENDS:${PN}-uri2pem += "${PN} python3 python3-asn1crypto"
