SUMMARY = "Summit SOM Manufacturing USB Boot initramfs Helper Image"
DESCRIPTION = "Summit SOM Manufacturing USB Boot initramfs Helper Image"

## WARNING: This recipe is not intended to be built directly. 
# It is used as a helper for the summitsom-mfg-initramfs-main image.

LICENSE = "Ezurio-Clause"

inherit core-image serial-autologin-root

export IMAGE_BASENAME = "${PN}"

REQUIRED_DISTRO_FEATURES += "summitsom-mfg-initramfs"

# Required for use as INITRAMFS_IMAGE
INITRAMFS_FSTYPES = "cpio.zst"
INITRAMFS_MAXSIZE ??= "262144"
IMAGE_FSTYPES = "${INITRAMFS_FSTYPES}"
IMAGE_NAME_SUFFIX ?= ""

# Do not pollute the initramfs with unneeded rootfs features
IMAGE_FEATURES = "\
    allow-empty-password \
    allow-root-login \
    empty-root-password \
    serial-autologin-root \
    "

# Minimal base packages
IMAGE_INSTALL += "\
    packagegroup-summit-basic \
    packagegroup-summit-diag \
    busybox-syslog \
    ${@bb.utils.contains('COMBINED_FEATURES', 'wifi', 'packagegroup-summit-radio-stack-mfg', '', d)} \
    ${@bb.utils.contains('COMBINED_FEATURES', 'alsa', 'kernel-module-tac5x1x', '', d)} \
    "