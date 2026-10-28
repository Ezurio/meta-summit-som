# NXP AHAB + FIT image signing via a Cloud-HSM key provider.
#
# Calls nxpimage directly with a PKCS#11 signature provider config string.
# SPSDK discovers the spsdk-pkcs11 plugin via entry points and delegates
# signing through python-pkcs11 to the backend's PKCS#11 library.
#
# mkimage signs the FIT with the PKCS#11 wrapper in SIG_KEY_PATH.

inherit uboot-key-provider-common

DEPENDS:append = " python3-spsdk-pkcs11-native"

do_compile[network] = "1"
do_deploy[network] = "1"
do_compile:prepend() {
    summit_key_provider_setup_env
}

# Sign with a PKCS#11 signature provider config string when dispatched by the
# AHAB recipe include.
do_sign_boot_image_key_provider() {
    if [ ! -e "${SIG_SUPPORT_PATH}/spsdk_ahab.yaml" ]; then
        bbfatal "SPSDK config not found at '${SIG_SUPPORT_PATH}/spsdk_ahab.yaml'. " \
                "Ensure SIG_SUPPORT_PATH points at a PKI tree containing spsdk_ahab.yaml."
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
        -e "/srk_array/,/^[^ #]/{s|- \([^/][^ ]*\.pem\)|- ${SIG_SUPPORT_PATH}/crts/\1|}" \
        "${SIG_SUPPORT_PATH}/spsdk_ahab.yaml" > "${AHAB_SIGN_YAML}"

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
