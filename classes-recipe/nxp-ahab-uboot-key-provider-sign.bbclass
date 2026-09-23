# NXP AHAB + FIT image signing via a Cloud-HSM key provider.
#
# Calls nxpimage directly with a PKCS#11 signature provider config string.
# SPSDK discovers the spsdk-pkcs11 plugin via entry points and delegates
# signing through python-pkcs11 to the backend's PKCS#11 library.
#
# For FIT signing, the standard PKCS#11 PEM wrapper mechanism (via OpenSSL
# pkcs11-provider) is used for mkimage.

inherit uboot-key-provider-common

UBOOT_KEY_PROVIDER_KEY_IDS:append = " ${HSM_FIT_KEY_ID}"

# Provider packages are needed for either remote role; the SPSDK PKCS#11
# plugin is needed only for AHAB container signing.
DEPENDS:append = "${@oe.utils.ifelse( \
    d.getVar('HSM_FIT_KEY_ID') or d.getVar('HSM_AHAB_KEY_ID'), \
    ' ${SUMMIT_KEY_PROVIDER_DEPENDS}', \
    '')}"
DEPENDS:append = "${@oe.utils.ifelse( \
    d.getVar('HSM_AHAB_KEY_ID'), \
    ' python3-spsdk-pkcs11-native', \
    '')}"

# Redirect UBOOT_SIGN_KEYDIR to staging copy for FIT key wrappers.
KEY_PROVIDER_SIG_DATA_ORIG := "${SIG_DATA_PATH}"
UBOOT_SIGN_KEYDIR = "${@oe.utils.ifelse( \
    d.getVar('KEY_PROVIDER_SIG_DATA_ORIG') + (d.getVar('HSM_FIT_KEY_ID') or ''), \
    d.getVar('KEY_PROVIDER_SIG_STAGING') + '/keys', \
    d.getVar('KEY_PROVIDER_SIGN_KEYDIR_ORIG'))}"

# Allow network access only for tasks whose signing role uses the provider.
python () {
    fit_enabled = bool(d.getVar('HSM_FIT_KEY_ID'))
    d.setVarFlag('do_compile', 'network', '1' if fit_enabled else '0')
    d.setVarFlag('do_uboot_assemble_fitimage', 'network',
                 '1' if fit_enabled else '0')
    d.setVarFlag('do_deploy', 'network',
                 '1' if d.getVar('HSM_AHAB_KEY_ID') else '0')
}

do_compile:prepend() {
    # Stage FIT signing key (dev.key -> PKCS#11 wrapper) if configured.
    KEY_PROVIDER_SIG_STAGE="${KEY_PROVIDER_SIG_STAGING}"
    if [ -n "${HSM_FIT_KEY_ID}" ] && \
       [ -d "${KEY_PROVIDER_SIG_DATA_ORIG}" ]; then
        summit_key_provider_setup_env
        rm -rf "$KEY_PROVIDER_SIG_STAGE"
        mkdir -p "$KEY_PROVIDER_SIG_STAGE/keys"

        # FIT signing key -- generate a wrapper when the backend supplies one;
        # otherwise copy the original dev.key/dev.crt from SIG_DATA_PATH.
        if [ -n "${UBOOT_SIGN_KEYNAME}" ]; then
            FIT_TOKEN="${@summit_key_provider_token_label(d.getVar('HSM_FIT_KEY_ID'))}"
            if [ -n "$FIT_TOKEN" ]; then
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

# Sign with a PKCS#11 signature provider config string when dispatched by the
# AHAB recipe include.
do_sign_boot_image_key_provider() {
    if [ ! -e "${KEY_PROVIDER_SIG_DATA_ORIG}/spsdk_ahab.yaml" ]; then
        bbfatal "SPSDK config not found at '${KEY_PROVIDER_SIG_DATA_ORIG}/spsdk_ahab.yaml'. " \
                "Ensure SIG_DATA_PATH is set and points at a PKI tree containing spsdk_ahab.yaml."
    fi

    if [ ! -e "${B}/flash.bin" ]; then
        bbfatal "imx-boot flash.bin is not available to sign"
    fi

    summit_key_provider_setup_env

    AHAB_TOKEN="${@summit_key_provider_token_label(d.getVar('HSM_AHAB_KEY_ID'))}"
    AHAB_KEY_LABEL="${@summit_key_provider_key_label(d.getVar('HSM_AHAB_KEY_ID'))}"
    if [ -z "$AHAB_TOKEN" ]; then
        bbfatal "The ${CLOUD_HSM_BACKEND} backend did not provide an AHAB key"
    fi

    # Build the signer config string for SPSDK's SignatureProvider system.
    # The spsdk-pkcs11 plugin (type=pkcs11) talks to the library supplied by
    # the selected key-provider backend via python-pkcs11.
    PKCS11_SO="${HSM_PKCS11_LIBRARY}"
    if [ -z "$PKCS11_SO" ] || [ ! -e "$PKCS11_SO" ]; then
        bbfatal "The ${CLOUD_HSM_BACKEND} backend did not provide a usable PKCS#11 library"
    fi
    SIGNER_CFG="type=pkcs11;so_path=${PKCS11_SO};token_label=${AHAB_TOKEN};key_label=${AHAB_KEY_LABEL};user_pin=unused"

    # Prepare the signing YAML: set family, rewrite signer line to use the
    # PKCS#11 signature provider, and absolutize certificate paths.
    AHAB_SIGN_YAML="${B}/spsdk_ahab_key_provider.yaml"
    sed -e "s|^ *family:.*|family: ${SPSDK_FAMILY}|" \
        -e "s|^ *signer:.*|signer: ${SIGNER_CFG}|" \
        -e "/srk_array/,/^[^ #]/{s|- \([^/][^ ]*\.pem\)|- ${KEY_PROVIDER_SIG_DATA_ORIG}/crts/\1|}" \
        "${KEY_PROVIDER_SIG_DATA_ORIG}/spsdk_ahab.yaml" > "${AHAB_SIGN_YAML}"

    bbnote "AHAB signing flash.bin for ${SPSDK_FAMILY} via SPSDK PKCS#11 provider"

    # Sign all OEM AHAB containers in flash.bin
    CRYPTOGRAPHY_OPENSSL_NO_LEGACY=1 \
    nxpimage ahab sign \
        -c "${AHAB_SIGN_YAML}" \
        -b "${B}/flash.bin" \
        -o "${B}/signed-flash.bin" \
        --force

    if [ ! -e "${B}/signed-flash.bin" ]; then
        bbfatal "AHAB signing failed -- signed-flash.bin was not produced"
    fi

    rm -f "${AHAB_SIGN_YAML}"

    _deploy_signed_flash_bin
}
