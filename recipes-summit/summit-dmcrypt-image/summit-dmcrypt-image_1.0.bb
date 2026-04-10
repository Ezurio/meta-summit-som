SUMMARY = "Summit dm-crypt image creation tool"
DESCRIPTION = "Offline dm-crypt image creation tool (C). \
Encrypts a binary image using AES-XTS-plain64 (or AES-CBC-ESSIV) \
without requiring losetup, dmsetup, or root access. \
Produces output byte-identical to the Linux kernel dm-crypt target."

LICENSE = "Ezurio"
NO_GENERIC_LICENSE[Ezurio] = "LICENSE.ezurio"
LIC_FILES_CHKSUM = "file://LICENSE.ezurio;md5=11ef601ae07d69cfcd7387a33b764027"

DEPENDS = "openssl"

SRC_URI = " \
    file://LICENSE.ezurio \
    file://dmcrypt_image.c \
    file://Makefile \
    "

do_configure[noexec] = "1"

S = "${UNPACKDIR}"

do_install () {
    install -D -m 0755 -t "${D}${bindir}" "${B}/dmcrypt_image"
}

BBCLASSEXTEND = "native nativesdk"
