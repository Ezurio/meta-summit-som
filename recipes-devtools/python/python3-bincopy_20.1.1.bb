SUMMARY = "Mangling of various file formats that conveys binary information (Motorola S-Record, Intel HEX and binary files)"
HOMEPAGE = "https://github.com/eerimoq/bincopy"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=d9aa4ec07de78abae21c490c9ffe61bd"

SRC_URI[sha256sum] = "e94a498b9a4abe76703c3772aad466f1563b4fc9809da7965f11bc77094093b9"

inherit pypi setuptools3

RDEPENDS:${PN} += "\
    ${PYTHON_PN}-humanfriendly \
    ${PYTHON_PN}-argparse-addons \
    ${PYTHON_PN}-pyelftools \
    "

BBCLASSEXTEND = "native nativesdk"
