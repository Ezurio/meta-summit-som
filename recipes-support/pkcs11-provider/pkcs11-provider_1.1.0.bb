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
SRC_URI = "git://github.com/latchset/${BPN}.git;branch=main;protocol=https"

S = "${WORKDIR}/git"

inherit meson pkgconfig

DEPENDS = " \
    openssl \
    p11-kit \
    "

do_install:append() {
    install -m 755 -D "${S}/tools/uri2pem.py" "${D}${bindir}/uri2pem.py"
}

PACKAGES =+ "${PN}-uri2pem"

FILES:${PN} += " \
    ${libdir}/ossl-modules/pkcs11.so \
    "

FILES:${PN}-uri2pem = " \
    ${bindir}/uri2pem.py \
    "

RDEPENDS:${PN}-uri2pem += "${PN}"

BBCLASSEXTEND = "native nativesdk"
