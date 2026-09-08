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

RESCUE_BOOTLOADER_FILES = "flash-rescue.bin"
RESCUE_BOOTLOADER_FILES:ti-soc = "tiboot3.bin ${SPL_BINARYNAME} u-boot.${UBOOT_SUFFIX}"
RESCUE_HOST_FILE = "imx-rescue.uuu"
RESCUE_HOST_FILE:ti-soc = "ti-rescue.sh"
RESCUE_ARCHIVE_EXTRA_FILES = ""
RESCUE_ARCHIVE_EXTRA_FILES:ti-soc = "70-ezurio-am6xx-dfu.rules rescue-manifest rescue-manifest.sha256"

addtask create_archive after do_image_complete before do_build

do_create_archive[depends] += "\
    virtual/kernel:do_deploy \
    virtual/bootloader:do_deploy \
    ${PN}:do_deploy_fit \
    summit-image-tools:do_deploy \
"

prepare_rescue_archive_files() {
    cp -fL "${DEPLOY_DIR_IMAGE}/flash.bin" "${DEPLOY_DIR_IMAGE}/flash-rescue.bin"
}

prepare_rescue_archive_files:ti-soc() {
    cat > "${DEPLOY_DIR_IMAGE}/rescue-manifest" <<EOF
machine=${MACHINE}
security=HS-FS
fit=fitImage-rescue
bootloaders=${RESCUE_BOOTLOADER_FILES}
host-tool=${RESCUE_HOST_FILE}
udev-rules=70-ezurio-am6xx-dfu.rules
EOF

    (cd "${DEPLOY_DIR_IMAGE}" && \
    sha256sum ${@' '.join(f'"{f}"' for f in d.getVar('RESCUE_BOOTLOADER_FILES').split())} \
    "fitImage-rescue" "${RESCUE_HOST_FILE}" \
    "70-ezurio-am6xx-dfu.rules" "rescue-manifest" \
        > rescue-manifest.sha256)
}

do_create_archive() {
    cp -fL "${DEPLOY_DIR_IMAGE}/${FIT_DEPLOY_FITIMAGE_NAME}" "${DEPLOY_DIR_IMAGE}/fitImage-rescue"
    prepare_rescue_archive_files

    if echo "${IMAGE_VERSION_SUFFIX}" | grep -xEq '\-[0-9]+\.[0-9]+\.[0-9]+\.[0-9]+' ; then
        #shellcheck disable=SC2086
        tar --transform='s,.*/,,' -cvjhf "${DEPLOY_DIR_IMAGE}/${ARCHIVE_NAME}.tar.bz2" \
            ${@' '.join('${DEPLOY_DIR_IMAGE}/' + f for f in d.getVar('RESCUE_BOOTLOADER_FILES').split())} \
            ${DEPLOY_DIR_IMAGE}/fitImage-rescue \
            ${DEPLOY_DIR_IMAGE}/${RESCUE_HOST_FILE} \
            ${@' '.join('${DEPLOY_DIR_IMAGE}/' + f for f in d.getVar('RESCUE_ARCHIVE_EXTRA_FILES').split())}
    fi
}
