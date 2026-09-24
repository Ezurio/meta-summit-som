python () {
    image = d.getVar('IMAGE_BASENAME', True)
    type = d.getVar('IMAGE_ROOTFS_VERITY_TYPE', True)
    d.setVarFlag("SWUPDATE_IMAGES_FSTYPES", image, "." + type)
}

inherit swupdate-image
inherit ${@oe.utils.ifelse( \
    d.getVar('HSM_UPDATE_KEY_ID'), \
    'swupdate-key-provider-sign', \
    '')}

SRC_URI += "file://erase_data_emmc.sh file://erase_data.sh file://copy_partitions.sh"
SRC_URI:append:imx8mp-summitsom = " file://update_support.sh"

DEPENDS += "summit-image-tools"

ARCHIVE_WILDCARD += "${SWUDEPLOYDIR}/${IMAGE_NAME}${IMAGE_NAME_SUFFIX}.swu \
    ${DEPLOY_DIR_IMAGE}/mksdcard.sh"

do_create_archive[depends] += "${PN}:do_swuimage"

SWUPDATE_SIGNING:summit-secure ?= "CMS"
SWUPDATE_PRIVATE_KEY:summit-secure = "${@oe.utils.ifelse( \
    d.getVar('HSM_UPDATE_KEY_ID'), \
    (d.getVar('KEY_PROVIDER_UPDATE_KEYDIR') or '') + '/update_signing.key', \
    (d.getVar('UBOOT_SIGN_KEYDIR') or '') + '/update_signing.key')}"
SWUPDATE_CMS_KEY:summit-secure = "${@oe.utils.ifelse( \
    d.getVar('HSM_UPDATE_KEY_ID'), \
    (d.getVar('KEY_PROVIDER_UPDATE_KEYDIR') or '') + '/update_signing.key', \
    (d.getVar('UBOOT_SIGN_KEYDIR') or '') + '/update_signing.key')}"
SWUPDATE_CMS_CERT:summit-secure = "${@oe.utils.ifelse( \
    d.getVar('HSM_UPDATE_KEY_ID'), \
    d.getVar('SWUPDATE_UPDATE_SIGNING_CERT'), \
    (d.getVar('UBOOT_SIGN_KEYDIR') or '') + '/update_signing.crt')}"
