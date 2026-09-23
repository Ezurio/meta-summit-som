SUMMARY = "Option groups missing in Click"
HOMEPAGE = "https://github.com/click-contrib/click-option-group"
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=0b41de2509ab6cfdc7b957c8e82956d0"

SRC_URI[sha256sum] = "f94ed2bc4cf69052e0f29592bd1e771a1789bd7bfc482dd0bc482134aff95823"

PYPI_PACKAGE = "click_option_group"

DEPENDS += "python3-hatch-vcs-native"

inherit pypi python_hatchling

RDEPENDS:${PN} += "${PYTHON_PN}-click"

BBCLASSEXTEND = "native nativesdk"
