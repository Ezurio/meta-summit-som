SUMMARY = "SLY - Sly Lex Yacc"
HOMEPAGE = "https://github.com/dabeaz/sly"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=40c72247cf23e6d5397482dae055914a"

SRC_URI[sha256sum] = "251d42015e8507158aec2164f06035df4a82b0314ce6450f457d7125e7649024"

inherit pypi python_setuptools_build_meta

BBCLASSEXTEND = "native nativesdk"
