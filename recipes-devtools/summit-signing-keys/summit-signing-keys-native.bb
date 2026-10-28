SUMMARY = "PKCS#11 key wrappers for Cloud HSM signing"
DESCRIPTION = "Generates the PEM key wrappers, and the matching FIT \
    certificate, that SIG_KEY_PATH points to when a Cloud HSM backend is \
    selected."
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

inherit native
inherit ${@oe.utils.ifelse('${CLOUD_HSM_BACKEND}', 'summit-key-provider', '')}

python () {
    if not d.getVar('CLOUD_HSM_BACKEND'):
        raise bb.parse.SkipRecipe('no Cloud HSM backend is selected')
}

DEPENDS = "${SUMMIT_KEY_PROVIDER_TOOL_DEPENDS} openssl-native"

UBOOT_SIGN_KEYNAME ??= "dev"
UBOOT_SIGN_IMG_KEYNAME ??= ""

do_configure[noexec] = "1"
do_compile[noexec] = "1"
do_install[network] = "1"

summit_signing_keys_wrap() {
    summit_key_provider_materialize_key "$1" "$2" || \
        bbfatal "${CLOUD_HSM_BACKEND} key materialization failed for the $3 key; check the Cloud HSM credentials"
}

do_install() {
    summit_key_provider_check_roles
    summit_key_provider_setup_env

    keydir="${D}${datadir}/summit-signing-keys"
    install -d "$keydir"

    for name in ${UBOOT_SIGN_KEYNAME} ${UBOOT_SIGN_IMG_KEYNAME}; do
        [ ! -e "$keydir/$name.key" ] || continue
        summit_signing_keys_wrap \
            "${@summit_key_provider_token_label(d.getVar('HSM_FIT_KEY_ID'))}" \
            "$keydir/$name.key" FIT
        summit_key_provider_materialize_cert \
            "$keydir/$name.key" "$keydir/$name.crt" "$name"
    done

    case " ${CLOUD_HSM_REQUIRED_ROLES} " in
    *" KEY "*)
        summit_signing_keys_wrap \
            "${@summit_key_provider_token_label(d.getVar('HSM_KEY_ID'))}" \
            "$keydir/smpk.key" SMPK
        ;;
    esac

    summit_signing_keys_wrap \
        "${@summit_key_provider_token_label(d.getVar('HSM_UPDATE_KEY_ID'))}" \
        "$keydir/update_signing.key" update
}

FILES:${PN} += "${datadir}/summit-signing-keys"
