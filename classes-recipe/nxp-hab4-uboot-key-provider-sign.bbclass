# NXP HAB4 + FIT image signing via a Cloud-HSM key provider.
#
# Stages SIG_DATA_PATH to a workdir copy with PKCS#11 key wrappers for CST
# (CSF/IMG keys) and optionally for FIT signing.

inherit uboot-key-provider-common

UBOOT_KEY_PROVIDER_KEY_IDS:append = " ${HSM_CSF_KEY_ID} ${HSM_IMG_KEY_ID} ${HSM_FIT_KEY_ID}"
DEPENDS:append = "${@oe.utils.ifelse( \
    '${HSM_CSF_KEY_ID}${HSM_IMG_KEY_ID}${HSM_FIT_KEY_ID}', \
    ' ${SUMMIT_KEY_PROVIDER_DEPENDS}', \
    '')}"

# Redirect SIG_DATA_PATH to the staging copy; UBOOT_SIGN_KEYDIR points into it.
KEY_PROVIDER_SIG_DATA_ORIG := "${SIG_DATA_PATH}"
KEY_PROVIDER_HAB_KEY_ID = "${@oe.utils.ifelse( \
    '${HSM_CSF_KEY_ID}', '${HSM_CSF_KEY_ID}', \
    oe.utils.ifelse('${HSM_IMG_KEY_ID}', '${HSM_IMG_KEY_ID}', '${HSM_FIT_KEY_ID}'))}"
SIG_DATA_PATH = "${@oe.utils.ifelse( \
    '${KEY_PROVIDER_SIG_DATA_ORIG}${KEY_PROVIDER_HAB_KEY_ID}', \
    '${KEY_PROVIDER_SIG_STAGING}', \
    '${KEY_PROVIDER_SIG_DATA_ORIG}')}"
UBOOT_SIGN_KEYDIR = "${@oe.utils.ifelse( \
    '${KEY_PROVIDER_SIG_DATA_ORIG}${KEY_PROVIDER_HAB_KEY_ID}', \
    '${KEY_PROVIDER_SIG_STAGING}/keys', \
    '${KEY_PROVIDER_SIGN_KEYDIR_ORIG}')}"

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

        # CST locates private keys relative to certs by swapping
        # crts/ → keys/ and _crt.pem → _key.pem.  Generate only the two
        # wrappers it will actually look for (derived from CSF_KEY/IMG_KEY).
        if [ -n "${HSM_CSF_KEY_ID}" ]; then
            CSF_KEY_BASE=$(basename "${CSF_KEY}" | sed 's/_crt\.pem$/_key.pem/')
            rm -f "$KEY_PROVIDER_SIG_STAGE/keys/$CSF_KEY_BASE"
            if ! summit_key_provider_materialize_key \
                "${@summit_key_provider_token_label(d.getVar('HSM_CSF_KEY_ID'))}" \
                "$KEY_PROVIDER_SIG_STAGE/keys/$CSF_KEY_BASE"; then
                bbfatal "${CLOUD_HSM_BACKEND} key materialization failed for the HAB CSF key"
            fi
        fi

        if [ -n "${HSM_IMG_KEY_ID}" ]; then
            IMG_KEY_BASE=$(basename "${IMG_KEY}" | sed 's/_crt\.pem$/_key.pem/')
            rm -f "$KEY_PROVIDER_SIG_STAGE/keys/$IMG_KEY_BASE"
            if ! summit_key_provider_materialize_key \
                "${@summit_key_provider_token_label(d.getVar('HSM_IMG_KEY_ID'))}" \
                "$KEY_PROVIDER_SIG_STAGE/keys/$IMG_KEY_BASE"; then
                bbfatal "${CLOUD_HSM_BACKEND} key materialization failed for the HAB IMG key"
            fi
        fi

        # CST reads the CSF and IMG passphrases from keys/key_pass.txt. Preserve
        # the copied passphrases while either role remains local; PKCS#11
        # wrappers ignore them. When both roles are remote, only the two
        # placeholder lines need to exist.
        if [ -n "${HSM_CSF_KEY_ID}" ] && [ -n "${HSM_IMG_KEY_ID}" ]; then
            printf '\n\n' > "$KEY_PROVIDER_SIG_STAGE/keys/key_pass.txt"
        fi

        # FIT signing key — generate a wrapper when the backend supplies one;
        # otherwise copy the original dev.key/dev.crt from SIG_DATA_PATH.
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
