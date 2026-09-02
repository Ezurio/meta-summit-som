# TI FIT-only image signing via a Cloud-HSM key provider.
#
# Generates a PKCS#11 key wrapper and self-signed certificate in a staging
# directory so mkimage signs through the selected provider.

inherit uboot-key-provider-common

UBOOT_KEY_PROVIDER_KEY_IDS:append = " ${HSM_FIT_KEY_ID}"
DEPENDS:append = "${@' ' + d.getVar('SUMMIT_KEY_PROVIDER_DEPENDS') if d.getVar('HSM_FIT_KEY_ID') else ''}"

# Redirect UBOOT_SIGN_KEYDIR to the materialized staging directory.
UBOOT_SIGN_KEYDIR = "${@d.getVar('KEY_PROVIDER_SIG_STAGING') + '/keys' if d.getVar('HSM_FIT_KEY_ID') else d.getVar('KEY_PROVIDER_SIGN_KEYDIR_ORIG')}"

python () {
    enabled = bool(d.getVar('HSM_FIT_KEY_ID'))
    for task in ('do_compile', 'do_uboot_assemble_fitimage'):
        d.setVarFlag(task, 'network', '1' if enabled else '0')
}

do_compile:prepend() {
    if [ -n "${HSM_FIT_KEY_ID}" ] && [ -n "${UBOOT_SIGN_KEYNAME}" ]; then
        summit_key_provider_setup_env
        FIT_TOKEN="${@summit_key_provider_token_label(d.getVar('HSM_FIT_KEY_ID'))}"
        if [ -n "$FIT_TOKEN" ]; then
            mkdir -p "${KEY_PROVIDER_SIG_STAGING}/keys"
            if ! summit_key_provider_materialize_key "$FIT_TOKEN" \
                "${KEY_PROVIDER_SIG_STAGING}/keys/${UBOOT_SIGN_KEYNAME}.key"; then
                bbfatal "${CLOUD_HSM_BACKEND} key materialization failed for the FIT key"
            fi
            summit_key_provider_materialize_cert \
                "${KEY_PROVIDER_SIG_STAGING}/keys/${UBOOT_SIGN_KEYNAME}.key" \
                "${KEY_PROVIDER_SIG_STAGING}/keys/${UBOOT_SIGN_KEYNAME}.crt" \
                "${UBOOT_SIGN_KEYNAME}"
        fi
    fi
}
