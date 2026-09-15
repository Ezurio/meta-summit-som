DESCRIPTION = "Microsoft Authentication Library (MSAL) for Python makes it easy to authenticate to Microsoft Entra ID."
HOMEPAGE = "https://github.com/AzureAD/microsoft-authentication-library-for-python"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=96e6ceb9fdbb835a67d769806fc98f9b"


SRC_URI[sha256sum] = "4f10ff1257bacfd1781f22e85bd2b8d43ad1b49f3b6aafd7906671cadedd464"

inherit pypi setuptools3

RDEPENDS:${PN} += " \
    ${PYTHON_PN}-requests \
    ${PYTHON_PN}-cryptography \
    ${PYTHON_PN}-pyjwt \
    "

BBCLASSEXTEND = "native nativesdk"