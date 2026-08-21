SUMMARY = "Summit SOM Rescue USB Boot initramfs Helper Image"
DESCRIPTION = "Summit SOM Rescue Manufacturing Provisioning USB Boot initramfs Helper Image"

## WARNING: This recipe is not intended to be built directly. 
# It is used as a helper for the summitsom-rescue-initramfs-main image.

LICENSE = "Ezurio-Clause"

inherit core-image

export IMAGE_BASENAME = "${PN}"

REQUIRED_DISTRO_FEATURES += "summitsom-rescue-initramfs"

# Required for use as INITRAMFS_IMAGE
INITRAMFS_FSTYPES = "cpio.zst"
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
    kernel-modules \
    ca-certificates \
    iproute2 \
    optee-client \
    dhcpcd \
    summit-update \
    summit-usbgadget \
    busybox-syslog \
    "