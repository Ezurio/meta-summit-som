python () {
    image = d.getVar('IMAGE_BASENAME', True)
    type = d.getVar('IMAGE_ROOTFS_VERITY_TYPE', True)
    d.setVarFlag("SWUPDATE_IMAGES_FSTYPES", image, "." + type)
}

inherit swupdate-image
# HSM_UPDATE_KEY_ID is defined by the backend this class loads, so gate on the backend.
inherit ${@oe.utils.ifelse( \
    '${CLOUD_HSM_BACKEND}', \
    'swupdate-key-provider-sign', \
    '')}

SRC_URI += "file://erase_data_emmc.sh file://erase_data.sh file://copy_partitions.sh"
SRC_URI:append:imx8mp-summitsom = " file://update_support.sh"

DEPENDS += "summit-image-tools"

ARCHIVE_WILDCARD += "${SWUDEPLOYDIR}/${IMAGE_NAME}${IMAGE_NAME_SUFFIX}.swu \
    ${DEPLOY_DIR_IMAGE}/mksdcard.sh"

do_create_archive[depends] += "${PN}:do_swuimage"

SWUPDATE_SIGNING:summit-secure ?= "CMS"
SWUPDATE_PRIVATE_KEY:summit-secure = "${SIG_KEY_PATH}/update_signing.key"
SWUPDATE_CMS_KEY:summit-secure = "${SIG_KEY_PATH}/update_signing.key"
SWUPDATE_CMS_CERT:summit-secure = "${SIG_SUPPORT_PATH}/update_signing.crt"

# Devices pin update_signing.crt, so fail early if it is missing or does not
# match the key.
swupdate_check_update_cert() {
    if [ ! -f "${SWUPDATE_CMS_KEY}" ]; then
        bbfatal "update_signing.key not found in SIG_KEY_PATH (${SIG_KEY_PATH})"
    fi
    if [ ! -f "${SWUPDATE_CMS_CERT}" ]; then
        bbfatal "update_signing.crt not found in SIG_SUPPORT_PATH (${SIG_SUPPORT_PATH}); devices pin this certificate"
    fi
    key_pub=$(openssl pkey -in "${SWUPDATE_CMS_KEY}" -pubout | openssl pkey -pubin -text_pub -noout)
    crt_pub=$(openssl x509 -in "${SWUPDATE_CMS_CERT}" -pubkey -noout | openssl pkey -pubin -text_pub -noout)
    if [ -z "$key_pub" ] || [ "$key_pub" != "$crt_pub" ]; then
        bbfatal "${SWUPDATE_CMS_CERT} does not match ${SWUPDATE_CMS_KEY}"
    fi
}
SWUPDATE_CERT_CHECK = ""
SWUPDATE_CERT_CHECK:summit-secure = "swupdate_check_update_cert"
do_swuimage[prefuncs] += "${SWUPDATE_CERT_CHECK}"
SWUPDATE_CERT_FILES = ""
SWUPDATE_CERT_FILES:summit-secure = "${SWUPDATE_CMS_CERT}:True"
do_swuimage[file-checksums] += "${SWUPDATE_CERT_FILES}"
