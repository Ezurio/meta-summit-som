SUMMARY = "Summit SOM Rescue USB Boot initramfs Image"
DESCRIPTION = "Summit SOM Rescue Manufacturing Provisioning USB Boot initramfs Image"

LICENSE = "Ezurio-Clause"

inherit image summit-kernel-fitimage

export IMAGE_BASENAME = "${PN}"
IMAGE_NAME_SUFFIX ?= ""

REQUIRED_DISTRO_FEATURES += "summitsom-rescue-initramfs"

PACKAGE_INSTALL = ""
IMAGE_FEATURES = ""
IMAGE_INSTALL = ""

DEPENDS += "summit-image-tools"

# No rootfs/dm-verity on this initramfs image, so there's no boot script to embed.
FIT_UBOOT_ENV = ""

# Rescue provisioning only needs the base devicetree, not board overlays.
FIT_EXTERNAL_DTB_OVERLAYS = "0"

ARCHIVE_NAME ?= "${IMAGE_BASENAME}-${MACHINE}-summit${IMAGE_VERSION_SUFFIX}"

addtask create_archive after do_image_complete before do_build

do_create_archive[depends] += "\
    virtual/kernel:do_deploy \
    virtual/bootloader:do_deploy \
    ${PN}:do_deploy_fit \
    summit-image-tools:do_deploy \
"

do_create_archive() {
    cp -fL "${DEPLOY_DIR_IMAGE}/flash.bin" "${DEPLOY_DIR_IMAGE}/flash-rescue.bin"
    cp -fL "${DEPLOY_DIR_IMAGE}/${FIT_DEPLOY_FITIMAGE_NAME}" "${DEPLOY_DIR_IMAGE}/fitImage-rescue"

    if echo "${IMAGE_VERSION_SUFFIX}" | grep -xEq '\-[0-9]+\.[0-9]+\.[0-9]+\.[0-9]+' ; then
        #shellcheck disable=SC2086
        tar --transform='s,.*/,,' -cvjhf "${DEPLOY_DIR_IMAGE}/${ARCHIVE_NAME}.tar.bz2" \
            ${DEPLOY_DIR_IMAGE}/flash-rescue.bin \
            ${DEPLOY_DIR_IMAGE}/fitImage-rescue \
            ${DEPLOY_DIR_IMAGE}/imx-rescue.uuu
    fi
}
