python () {
    image = d.getVar('IMAGE_BASENAME', True)
    type = d.getVar('IMAGE_ROOTFS_VERITY_TYPE', True)
    d.setVarFlag("SWUPDATE_IMAGES_FSTYPES", image, "." + type)
}

inherit swupdate-image

SRC_URI += "file://erase_data_emmc.sh"
SRC_URI:append:imx8mp-summitsom = " file://update_support.sh"

DEPENDS += "summit-image-tools"

SWUPDATE_IMAGES += "${IMAGE_ROOTFS_VERITY_NAME}.scr.bin"

ARCHIVE_WILDCARD += "${SWUDEPLOYDIR}/${IMAGE_NAME}${IMAGE_NAME_SUFFIX}.swu \
    ${DEPLOY_DIR_IMAGE}/mksdcard.sh"

do_create_archive[depends] += "${PN}:do_swuimage"

SWUPDATE_SIGNING:summit-secure ?= "CMS"
SWUPDATE_PRIVATE_KEY:summit-secure = "${UBOOT_SIGN_KEYDIR}/update_signing.key"
SWUPDATE_CMS_KEY:summit-secure = "${UBOOT_SIGN_KEYDIR}/update_signing.key"
SWUPDATE_CMS_CERT:summit-secure = "${UBOOT_SIGN_KEYDIR}/update_signing.crt"
