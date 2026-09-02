python () {
    image = d.getVar('IMAGE_BASENAME', True)
    type = d.getVar('IMAGE_ROOTFS_VERITY_TYPE', True)
    d.setVarFlag("SWUPDATE_IMAGES_FSTYPES", image, "." + type)
}

inherit swupdate-image

SRC_URI += "file://erase_data_emmc.sh file://erase_data.sh file://copy_partitions.sh"
SRC_URI:append:imx8mp-summitsom = " file://update_support.sh"

DEPENDS += "summit-image-tools"

ARCHIVE_WILDCARD += "${SWUDEPLOYDIR}/${IMAGE_NAME}${IMAGE_NAME_SUFFIX}.swu \
    ${DEPLOY_DIR_IMAGE}/mksdcard.sh"

do_create_archive[depends] += "${PN}:do_swuimage"

SWUPDATE_SIGNING:summit-secure ?= "CMS"
SWUPDATE_PRIVATE_KEY:summit-secure = "${@bb.utils.contains( \
    'HSM_FIT_KEY_ID', d.getVar('HSM_FIT_KEY_ID'), \
    '${UBOOT_SIGN_KEYDIR}/${UBOOT_SIGN_KEYNAME}.key', \
    '${UBOOT_SIGN_KEYDIR}/update_signing.key', d)}"
SWUPDATE_CMS_KEY:summit-secure = "${@bb.utils.contains( \
    'HSM_FIT_KEY_ID', d.getVar('HSM_FIT_KEY_ID'), \
    '${UBOOT_SIGN_KEYDIR}/${UBOOT_SIGN_KEYNAME}.key', \
    '${UBOOT_SIGN_KEYDIR}/update_signing.key', d)}"
SWUPDATE_CMS_CERT:summit-secure = "${@bb.utils.contains( \
    'HSM_FIT_KEY_ID', d.getVar('HSM_FIT_KEY_ID'), \
    '${UBOOT_SIGN_KEYDIR}/${UBOOT_SIGN_KEYNAME}.crt', \
    '${UBOOT_SIGN_KEYDIR}/update_signing.crt', d)}"
