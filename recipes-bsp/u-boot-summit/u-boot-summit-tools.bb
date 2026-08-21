SUMMARY = "Summit U-Boot Tools for Ezurio boards"
LICENSE = "GPL-2.0-or-later"
LIC_FILES_CHKSUM = "file://Licenses/README;md5=2ca5f2c35c8cc335f0a19756634782f1"
SECTION = "bootloaders"

require recipes-bsp/u-boot/u-boot-tools.inc

inherit pkgconfig summit-platform-version

SRC_URI = "${SUMMIT_EXTERNAL_GIT_URI}/u-boot-som.git;${SUMMIT_EXTERNAL_GIT_SUFFIX}"
SRC_URI:summit-internal = "${SUMMIT_INTERNAL_GIT_URI}/cp_linux-u-boot-som60.git;${SUMMIT_INTERNAL_GIT_SUFFIX}"

SRC_URI:append = "${@' file://0002-rsa-sign-load-openssl-config-for-provider-support.patch' if any(d.getVar(v) for v in ('AWS_KMS_KEY_ARN','AWS_KMS_CSF_KEY_ARN','AWS_KMS_IMG_KEY_ARN','AWS_KMS_FIT_KEY_ARN')) else ''}"
FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

B = "${UNPACKDIR}/build"

DEPENDS += "flex-native bison-native python3-setuptools-native" 

PROVIDES:append:class-target = " u-boot-tools"
PROVIDES:append:class-native = " u-boot-tools-native"
PROVIDES:append:class-nativesdk = " nativesdk-u-boot-tools"

PROVIDES:append:class-target = " ${MLPREFIX}u-boot-dumpimage"
PROVIDES:append:class-native = " u-boot-dumpimage-native"

# Split dumpimage out of ${PN}-mkimage into its own package
PACKAGES += "${PN}-dumpimage"
FILES:${PN}-mkimage:remove = "${bindir}/uboot-dumpimage ${bindir}/dumpimage"
FILES:${PN}-dumpimage = "${bindir}/uboot-dumpimage ${bindir}/dumpimage"
RPROVIDES:${PN}-dumpimage += " ${MLPREFIX}u-boot-dumpimage"
RDEPENDS:${PN} += "${PN}-dumpimage"
