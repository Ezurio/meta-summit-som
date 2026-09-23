SUMMARY = "A toolset for deeply merging Python dictionaries"
HOMEPAGE = "https://github.com/toumorokoshi/deepmerge"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=5461efe2d19ce359c7d72d7be3c05e1c"

SRC_URI[sha256sum] = "35b39a4cb92cf328d6eca61cbbf65f68a37c2ceb3085f0f853cbb2e52a59fc23"

DEPENDS += "python3-setuptools-scm-native"

inherit pypi python_setuptools_build_meta

BBCLASSEXTEND = "native nativesdk"