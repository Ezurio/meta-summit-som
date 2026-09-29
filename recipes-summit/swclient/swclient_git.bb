SUMMARY = "swupdate IPC client Python module"

LICENSE = "Ezurio"
NO_GENERIC_LICENSE[Ezurio] = "LICENSE.ezurio"
LIC_FILES_CHKSUM = "\
    file://LICENSE.ezurio;md5=fd3dd0630b215465b6f50540642d5b93 \
    "

inherit setuptools3 summit-platform-version

DEPENDS += "swupdate"

SRC_URI = "${SUMMIT_EXTERNAL_GIT_URI}/lrd-userspace-examples.git;${SUMMIT_EXTERNAL_GIT_SUFFIX}"
SRC_URI:summit-internal = "${SUMMIT_INTERNAL_GIT_URI}/cp_linux-lrd-userspace-examples.git;${SUMMIT_INTERNAL_GIT_SUFFIX}"

S = "${WORKDIR}/git/swclient"

RDEPENDS:${PN} = "python3 swupdate"
