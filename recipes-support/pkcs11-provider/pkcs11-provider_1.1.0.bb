SUMMARY = "An OpenSSL provider that allows direct interfacing with pkcs11 drivers"
DESCRIPTION = "\
This is an OpenSSL 3.x provider to access Hardware or Software Tokens using \
the PKCS#11 Cryptographic Token Interface\
\
This code targets version 3.1 of the interface but should be backwards \
compatible to previous versions as well.\
"
HOMEPAGE = "https://github.com/latchset/pkcs11-provider"
SECTION = "libs"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://COPYING;md5=b53b787444a60266932bd270d1cf2d45"

SRCREV = "acb7086e44849d019956233348046c4f07c0670b"
SRC_URI = " \
    git://github.com/latchset/${BPN}.git;branch=main;protocol=https \
    file://0001-uri2pem-exit-non-zero-on-verification-failure.patch \
    file://0002-fix-install-path.patch \
    "

S = "${WORKDIR}/git"

inherit meson pkgconfig

DEPENDS = " \
    openssl \
    p11-kit \
    "

# RDEPENDS is not consumed for native (no package manager/rootfs step), so
# uri2pem.py needs python3-asn1crypto staged into the native sysroot via DEPENDS.
DEPENDS:append:class-native = " python3-asn1crypto-native"

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

BBCLASSEXTEND = "native nativesdk"
