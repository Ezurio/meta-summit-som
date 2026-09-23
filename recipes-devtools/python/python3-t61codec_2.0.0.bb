SUMMARY = "T.61 codec for Python"
HOMEPAGE = "https://github.com/exhuma/t61codec"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=cc5ee59e5774aa449f71b7d02433ddf3"

SRC_URI[sha256sum] = "21c238fbf897b32e5fe9450b71193a1d223abb591dd84b5da8fbc50a2c9277db"

inherit pypi python_poetry_core

BBCLASSEXTEND = "native nativesdk"