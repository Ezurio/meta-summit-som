SUMMARY = "PKCS#11 support for Python"
DESCRIPTION = "A high-level, idiomatic interface to the PKCS#11 (Cryptoki) API \
for Python. Supports RSA, DSA, ECDSA, AES and more via any PKCS#11 library."
HOMEPAGE = "https://python-pkcs11.readthedocs.io/"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=f68bda54505b4002e6ec86e08125ef79"

PYPI_PACKAGE = "python_pkcs11"

SRC_URI[sha256sum] = "f9e11df146ce2e6359aeb81fa84c2dd7ab9719f707cdae06ceae22d9e6a10818"
SRC_URI += "file://0001-pyproject-lower-build-requirements-for-yocto.patch"

DEPENDS += "python3-cython python3-setuptools-scm"

RDEPENDS:${PN} += "python3-asn1crypto"

inherit pypi python_setuptools_build_meta native

BBCLASSEXTEND = "native nativesdk"
