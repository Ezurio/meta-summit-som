SUMMARY = "Summit SOM USB Boot Main initramfs Image"

LICENSE = "Ezurio-Clause"

inherit image summit-kernel-fitimage

export IMAGE_BASENAME = "${PN}"
IMAGE_NAME_SUFFIX ?= ""

PACKAGE_INSTALL = ""
IMAGE_FEATURES = ""
IMAGE_INSTALL = ""

DEPENDS += "summit-image-tools"

# No rootfs/dm-verity on this initramfs image, so there's no boot script to embed.
FIT_UBOOT_ENV = ""

ARCHIVE_NAME ?= "${IMAGE_BASENAME}-${MACHINE}-summit${IMAGE_VERSION_SUFFIX}"
ARCHIVE_DIR_NAME ?= "${DISTRO}"


RESCUE_FILES:imx-generic-bsp = "flash.bin kernel.itb imx-ram.uuu"
RESCUE_FILES:k3 = "tiboot3.bin ${SPL_BINARYNAME} u-boot.${UBOOT_SUFFIX} kernel.itb ti-ram.sh 70-ezurio-usb.rules"

addtask create_archive after do_image_complete before do_build

do_create_archive[depends] += "\
    virtual/kernel:do_deploy \
    virtual/bootloader:do_deploy \
    ${PN}:do_deploy_fit \
    summit-image-tools:do_deploy \
"

prepare_rescue_archive_files:imx-generic-bsp() {
    cat > "${DEPLOY_DIR_IMAGE}/${ARCHIVE_DIR_NAME}/manifest" <<EOF
machine=${MACHINE}
arch=imx
fit=kernel.itb
bootloaders=flash.bin
EOF
}

prepare_rescue_archive_files:k3() {
    cat > "${DEPLOY_DIR_IMAGE}/${ARCHIVE_DIR_NAME}/manifest" <<EOF
machine=${MACHINE}
arch=k3
security=HS-FS
fit=kernel.itb
bootloaders=tiboot3.bin ${SPL_BINARYNAME} u-boot.${UBOOT_SUFFIX}
host-tool=ti-ram.sh
udev-rules=70-ezurio-usb.rules
EOF
}

do_create_archive() {
    archive_dir="${DEPLOY_DIR_IMAGE}/${ARCHIVE_DIR_NAME}"
    rm -rf "${archive_dir}"
    install -d "${archive_dir}"
    for file in ${RESCUE_FILES}; do
        cp -fL "${DEPLOY_DIR_IMAGE}/${file}" "${archive_dir}/${file}"
    done

    prepare_rescue_archive_files()

    (cd "${archive_dir}" && sha256sum ${RESCUE_FILES} >> manifest.sha256)

    if echo "${IMAGE_VERSION_SUFFIX}" | grep -xEq '\-[0-9]+\.[0-9]+\.[0-9]+\.[0-9]+' ; then
        tar -cvjhf "${DEPLOY_DIR_IMAGE}/${ARCHIVE_NAME}.tar.bz2" \
            --transform="s,^${ARCHIVE_DIR_NAME}$,${ARCHIVE_NAME}," \
            -C "${DEPLOY_DIR_IMAGE}" "${ARCHIVE_DIR_NAME}"
    fi
}
