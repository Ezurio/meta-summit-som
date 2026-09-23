SUMMARY = "Pure Python X.690 implementation"
HOMEPAGE = "https://exhuma.github.io/x690/"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE.txt;md5=32e321618dd99d9983a92a83fa679f55"

# Package version is 1.0.0post1 on PyPI
PV = "1.0.0.post1"
PYPI_PACKAGE = "x690"

SRC_URI[sha256sum] = "3a2a8a4e479079188aa6e847814981286b8f1c19569898fa3fe573ee0ce05349"

SRC_URI += "file://0001-setup.cfg-exclude-the-test-suite-from-the-installed-.patch"

inherit pypi python_setuptools_build_meta

RDEPENDS:${PN} += "${PYTHON_PN}-t61codec"

BBCLASSEXTEND = "native nativesdk"
