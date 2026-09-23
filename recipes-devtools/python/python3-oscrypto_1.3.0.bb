SUMMARY = "Cryptographic primitives to complete and/or supplement the oscilloscope of asn1crypto"
HOMEPAGE = "https://github.com/wbond/oscrypto"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=b5cda97fbd7959ad47a952651a87051a"

SRC_URI[sha256sum] = "6f5fef59cb5b3708321db7cca56aed8ad7e662853351e7991fcf60ec606d47a4"

inherit pypi setuptools3

RDEPENDS:${PN} += "${PYTHON_PN}-asn1crypto"

BBCLASSEXTEND = "native nativesdk"
