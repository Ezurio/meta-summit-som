# NXP HAB4 + FIT image signing via a Cloud-HSM key provider.
#
# Stages SIG_DATA_PATH to a workdir copy with a PKCS#11 PEM wrapper for FIT
# signing. HAB4 CSF and image signing use SPSDK's direct PKCS#11 provider.

inherit uboot-key-provider-common

UBOOT_KEY_PROVIDER_KEY_IDS:append = " ${HSM_CSF_KEY_ID} ${HSM_IMG_KEY_ID} ${HSM_FIT_KEY_ID}"

def summit_key_provider_depends(d):
    """Return provider dependencies when a signing key is configured."""
    key_vars = ('HSM_CSF_KEY_ID', 'HSM_IMG_KEY_ID', 'HSM_FIT_KEY_ID')
    dependencies = d.getVar('SUMMIT_KEY_PROVIDER_DEPENDS') or ''
    return (' ' + dependencies) if any(d.getVar(v) for v in key_vars) else ''

DEPENDS:append = "${@summit_key_provider_depends(d)}"
DEPENDS:append = " python3-spsdk-pkcs11-native"

# Redirect SIG_DATA_PATH to the staging copy; UBOOT_SIGN_KEYDIR points into it.
KEY_PROVIDER_SIG_DATA_ORIG := "${SIG_DATA_PATH}"

def summit_key_provider_hab_key_id(d):
    """Return the first configured HAB or FIT provider key ID."""
    return next((d.getVar(v) for v in
                 ('HSM_CSF_KEY_ID', 'HSM_IMG_KEY_ID', 'HSM_FIT_KEY_ID')
                 if d.getVar(v)), '')

def summit_key_provider_sig_data_path(d):
    """Use staged signing data when any provider key is configured."""
    if d.getVar('KEY_PROVIDER_SIG_DATA_ORIG') and d.getVar('KEY_PROVIDER_HAB_KEY_ID'):
        return d.getVar('KEY_PROVIDER_SIG_STAGING')
    return d.getVar('KEY_PROVIDER_SIG_DATA_ORIG') or ''

def summit_key_provider_sign_keydir(d):
    """Return the signing-key directory matching the selected data tree."""
    if d.getVar('KEY_PROVIDER_SIG_DATA_ORIG') and d.getVar('KEY_PROVIDER_HAB_KEY_ID'):
        return d.getVar('KEY_PROVIDER_SIG_STAGING') + '/keys'
    return d.getVar('KEY_PROVIDER_SIGN_KEYDIR_ORIG') or ''

KEY_PROVIDER_HAB_KEY_ID = "${@summit_key_provider_hab_key_id(d)}"
SIG_DATA_PATH = "${@summit_key_provider_sig_data_path(d)}"
UBOOT_SIGN_KEYDIR = "${@summit_key_provider_sign_keydir(d)}"

python () {
    enabled = bool(d.getVar('KEY_PROVIDER_HAB_KEY_ID'))
    for task in ('do_compile', 'do_uboot_assemble_fitimage'):
        d.setVarFlag(task, 'network', '1' if enabled else '0')
}

do_compile:prepend() {
    # Stage signing data: create ${KEY_PROVIDER_SIG_STAGING} with the crts/ from
    # the original SIG_DATA_PATH, then replace only the roles supplied by the
    # selected provider. This preserves local keys for unconfigured roles.
    KEY_PROVIDER_SIG_STAGE="${KEY_PROVIDER_SIG_STAGING}"
    if [ -n "${KEY_PROVIDER_HAB_KEY_ID}" ] && \
       [ -d "${KEY_PROVIDER_SIG_DATA_ORIG}/crts" ]; then
        summit_key_provider_setup_env
        rm -rf "$KEY_PROVIDER_SIG_STAGE"
        mkdir -p "$KEY_PROVIDER_SIG_STAGE"
        cp -a "${KEY_PROVIDER_SIG_DATA_ORIG}/." "$KEY_PROVIDER_SIG_STAGE/"

        # FIT signing key — generate a wrapper when the backend supplies one;
        # otherwise keep the original dev.key/dev.crt from SIG_DATA_PATH.
        if [ -n "${UBOOT_SIGN_KEYNAME}" ]; then
            FIT_TOKEN="${@summit_key_provider_token_label(d.getVar('HSM_FIT_KEY_ID'))}"
            if [ -n "$FIT_TOKEN" ]; then
                rm -f "$KEY_PROVIDER_SIG_STAGE/keys/${UBOOT_SIGN_KEYNAME}.key" \
                    "$KEY_PROVIDER_SIG_STAGE/keys/${UBOOT_SIGN_KEYNAME}.crt"
                if ! summit_key_provider_materialize_key "$FIT_TOKEN" \
                    "$KEY_PROVIDER_SIG_STAGE/keys/${UBOOT_SIGN_KEYNAME}.key"; then
                    bbfatal "${CLOUD_HSM_BACKEND} key materialization failed for the FIT key"
                fi
                summit_key_provider_materialize_cert \
                      "$KEY_PROVIDER_SIG_STAGE/keys/${UBOOT_SIGN_KEYNAME}.key" \
                      "$KEY_PROVIDER_SIG_STAGE/keys/${UBOOT_SIGN_KEYNAME}.crt" \
                    "${UBOOT_SIGN_KEYNAME}"
            fi
        fi
    fi
}
