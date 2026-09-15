SUMMARY = "PKCS#11 support for Python"
DESCRIPTION = "A high-level, idiomatic interface to the PKCS#11 (Cryptoki) API \
for Python. Supports RSA, DSA, ECDSA, AES and more via any PKCS#11 library."
HOMEPAGE = "https://python-pkcs11.readthedocs.io/"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=f68bda54505b4002e6ec86e08125ef79"

PYPI_PACKAGE = "python_pkcs11"

SRC_URI[sha256sum] = "8f49bcb072bca3d74837547dd77145e065ed333e5e8642e539f8b1aca7ce1725"

DEPENDS += "python3-cython-native python3-setuptools-scm-native"

RDEPENDS:${PN} += "python3-asn1crypto"

inherit pypi python_setuptools_build_meta native

BBCLASSEXTEND = "native nativesdk"