# TI provisioning-data signing via a Cloud-HSM key provider.
#
# Preserve the local SMEK, provisioning data, and other key material in a
# recipe-local staging directory, then replace only SMPK with a provider key
# wrapper.

inherit summit-key-provider

DEPENDS:append = "${@' ' + d.getVar('SUMMIT_KEY_PROVIDER_DEPENDS') if d.getVar('HSM_KEY_ID') else ''}"

KEY_PROVIDER_PROV_KEYDIR_ORIG := "${UBOOT_SIGN_KEYDIR}"
KEY_PROVIDER_PROV_STAGING = "${UNPACKDIR}/key-provider-prov-keys"
UBOOT_SIGN_KEYDIR = "${@d.getVar('KEY_PROVIDER_PROV_STAGING') if d.getVar('HSM_KEY_ID') else d.getVar('KEY_PROVIDER_PROV_KEYDIR_ORIG')}"

do_install:prepend() {
    if [ -n "${HSM_KEY_ID}" ]; then
        summit_key_provider_setup_env

        if [ ! -d "${KEY_PROVIDER_PROV_KEYDIR_ORIG}" ]; then
            bbfatal "Local provisioning keys directory not found: ${KEY_PROVIDER_PROV_KEYDIR_ORIG}"
        fi

        rm -rf "${KEY_PROVIDER_PROV_STAGING}"
        mkdir -p "${KEY_PROVIDER_PROV_STAGING}"
        cp -a "${KEY_PROVIDER_PROV_KEYDIR_ORIG}/." \
            "${KEY_PROVIDER_PROV_STAGING}/"

        # Remove the copied file first so a source-tree symlink cannot cause
        # materialization to overwrite another local key.
        rm -f "${KEY_PROVIDER_PROV_STAGING}/smpk.key"
        SMPK_TOKEN="${@summit_key_provider_token_label(d.getVar('HSM_KEY_ID'))}"
        if ! summit_key_provider_materialize_key "$SMPK_TOKEN" \
            "${KEY_PROVIDER_PROV_STAGING}/smpk.key"; then
            bbfatal "${CLOUD_HSM_BACKEND} key materialization failed for the TI SMPK"
        fi
    fi
}

do_install[depends] += "${@d.getVar('SUMMIT_KEY_PROVIDER_TASK_DEPS') if d.getVar('HSM_KEY_ID') else ''}"

python () {
    d.setVarFlag('do_install', 'network',
                 '1' if d.getVar('HSM_KEY_ID') else '0')
}
