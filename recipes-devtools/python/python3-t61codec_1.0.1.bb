SUMMARY = "T.61 codec for Python"
HOMEPAGE = "https://github.com/exhuma/t61codec"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=cc5ee59e5774aa449f71b7d02433ddf3"

SRC_URI[sha256sum] = "4d854f40017ef39e699291a57a9891c74630facb370b94238284eb9a475ae23f"

inherit pypi setuptools3

BBCLASSEXTEND = "native nativesdk"
