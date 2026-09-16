SUMMARY = "Signature Provider plugin for SPSDK using PKCS#11 interface"
DESCRIPTION = "NXP SPSDK plugin that provides PKCS#11-based signing via the \
standard SignatureProvider entry point interface."
HOMEPAGE = "https://github.com/nxp-mcuxpresso/spsdk_plugins/tree/master/pkcs11"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=fe2a425fb1f291c670f58b9e3771878e"

PYPI_PACKAGE = "spsdk_pkcs11"

SRC_URI[sha256sum] = "fc7b61eed173a549fe84bb8c40b18660bc884ebccd46fe8ac83604ac6d0f9d6c"

DEPENDS += " \
    python3-pkcs11-native \
    python3-spsdk-native \
    "

inherit pypi python_setuptools_build_meta native
