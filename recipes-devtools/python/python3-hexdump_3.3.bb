SUMMARY = "Simple hex dump solution for Python 2/3"
HOMEPAGE = "https://pypi.python.org/pypi/hexdump"
LICENSE = "PD"
LIC_FILES_CHKSUM = "file://hexdump.py;beginline=28;endline=28;md5=874422e312c723b0f83c70953c4bb476"

PYPI_PACKAGE_EXT = "zip"
SRC_URI[sha256sum] = "d781a43b0c16ace3f9366aade73e8ad3a7bd5137d58f0b45ab2d3f54876f20db"

inherit pypi setuptools3

# hexdump's zip archive extracts without a top-level directory.
S = "${UNPACKDIR}"

BBCLASSEXTEND = "native nativesdk"
