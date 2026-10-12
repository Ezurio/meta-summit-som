SUMMARY = "Summit SOM USB Boot Main initramfs Image"

LICENSE = "Ezurio-Clause"

inherit image summit-kernel-fitimage

export IMAGE_BASENAME = "${PN}"
IMAGE_NAME_SUFFIX ?= ""

PACKAGE_INSTALL = ""
IMAGE_FEATURES = ""
IMAGE_INSTALL = ""


python () {
    if 'summitsom-initramfs' not in d.getVar('OVERRIDES').split(':'):
        raise bb.parse.SkipRecipe("needs OVERRIDES to include 'summitsom-initramfs' (got '%s')"
                                  % d.getVar('OVERRIDES'))
}

# No rootfs/dm-verity on this initramfs image, so there's no boot script to embed.
FIT_UBOOT_ENV = ""

ARCHIVE_NAME ?= "${INITRAMFS_IMAGE}-${MACHINE}-summit${IMAGE_VERSION_SUFFIX}"
ARCHIVE_DIR_NAME ?= "${INITRAMFS_IMAGE}"
ARCHIVE_PATH = "${DEPLOY_DIR_IMAGE}/${ARCHIVE_DIR_NAME}"

RESCUE_FILES:imx-generic-bsp = "flash.bin kernel.itb imx-ram.uuu"
RESCUE_FILES:k3 = "tiboot3.bin ${SPL_BINARYNAME} u-boot.${UBOOT_SUFFIX} kernel.itb ti-ram.sh 70-ezurio-usb.rules"
RESCUE_FILES:mtk = "da-spl.bin da-fit.bin kernel.itb mtk-ram.sh 70-ezurio-usb.rules"

addtask create_archive after do_image_complete before do_build

do_create_archive[depends] += "\
    virtual/kernel:do_deploy \
    virtual/bootloader:do_deploy \
    ${PN}:do_deploy_fit \
    summit-image-tools:do_deploy \
"

do_create_archive:prepend:imx-generic-bsp() {
    cat > "${ARCHIVE_PATH}/manifest" <<EOF
machine=${MACHINE}
arch=imx
fit=kernel.itb
bootloaders=flash.bin
EOF
}

do_create_archive:prepend:k3() {
    cat > "${ARCHIVE_PATH}/manifest" <<EOF
machine=${MACHINE}
arch=k3
security=HS-FS
fit=kernel.itb
bootloaders=tiboot3.bin ${SPL_BINARYNAME} u-boot.${UBOOT_SUFFIX}
host-tool=ti-ram.sh
udev-rules=70-ezurio-usb.rules
EOF
}

do_create_archive:prepend:mtk() {
    cat > "${ARCHIVE_PATH}/manifest" <<EOF
machine=${MACHINE}
arch=mtk
fit=kernel.itb
bootloaders=da-spl.bin da-fit.bin
host-tool=mtk-ram.sh
udev-rules=70-ezurio-usb.rules
EOF
}

do_create_archive:prepend() {
    rm -rf "${ARCHIVE_PATH}"
    install -d "${ARCHIVE_PATH}"
    for file in ${RESCUE_FILES}; do
        cp -fL -t "${ARCHIVE_PATH}" "${DEPLOY_DIR_IMAGE}/${file}"
    done
}

do_create_archive() {
    (cd "${ARCHIVE_PATH}" && sha256sum ${RESCUE_FILES} >> manifest.sha256)

    #if echo "${IMAGE_VERSION_SUFFIX}" | grep -xEq '\-[0-9]+\.[0-9]+\.[0-9]+\.[0-9]+' ; then
        tar -cvjhf "${DEPLOY_DIR_IMAGE}/${ARCHIVE_NAME}.tar.bz2" \
            --transform="s,^${ARCHIVE_DIR_NAME},${ARCHIVE_NAME}," \
            -C "${DEPLOY_DIR_IMAGE}" "${ARCHIVE_DIR_NAME}"
    #fi
}
