require conf/machine/include/summit-spsdk-family.inc

SRC_URI:append:mx95-generic-bsp = " \
    file://0001-imx8image-Set-signature-block-version-1-for-AHAB-V2.patch \
    "

SRC_URI:append = "${@oe.utils.ifelse( \
    '${CLOUD_HSM_BACKEND}', \
    ' file://0002-rsa-sign-load-openssl-config-for-provider-support.patch', \
    '')}"

# AHAB (imx9x): SPSDK PKCS#11 signer + FIT signing via nxpimage
inherit ${@oe.utils.ifelse( \
    '${CLOUD_HSM_BACKEND}', \
    'nxp-ahab-uboot-key-provider-sign', \
    '')}

DEPENDS:append = " \
    python3-spsdk-native \
    "

UBOOT_AHAB_AUTO_LOCK ?= "0"

do_configure:append() {
    echo "CONFIG_AHAB_BOOT=y" >> "${B}/.config"
    echo "CONFIG_SUMMIT_AUTO_CLOSE=y" >> "${B}/.config"

    if [ "${UBOOT_AHAB_AUTO_LOCK}" = "1" ]; then
        echo "CONFIG_SUMMIT_AHAB_AUTO_LOCK=y" >> "${B}/.config"
    fi
}

# Signs the imx-boot flash.bin via nxpimage for AHAB platforms (mx9/mx8ulp/mx8qm).
# mx8m (HAB) images are signed by the binman/SPSDK adapter at build time instead.
#
# The signer line in spsdk_ahab.yaml is rewritten to a SPSDK SignatureProvider
# config string (type=file;file_path=...;password=...) so nxpimage resolves the
# key through SPSDK's plugin system. The key-provider class calls this helper
# when its backend does not supply the AHAB role.
do_sign_boot_image_local() {
    if [ ! -e "${SIG_DATA_PATH}/spsdk_ahab.yaml" ]; then
        bbfatal "SPSDK config not found at '${SIG_DATA_PATH}/spsdk_ahab.yaml'. " \
                "Ensure SIG_DATA_PATH is set and points at a PKI tree containing spsdk_ahab.yaml."
    fi

    if [ ! -e "${B}/flash.bin" ]; then
        bbfatal "imx-boot flash.bin is not available to sign"
    fi

    # Build the signer config string for the file-based signature provider.
    KEY_PASS_TXT="${SIG_DATA_PATH}/keys/key_pass.txt"
    if [ -f "${KEY_PASS_TXT}" ]; then
        SIGNER_SED="s|^ *signer: *\(.*\)|signer: type=file;file_path=${SIG_DATA_PATH}/keys/\1;password=${KEY_PASS_TXT}|"
    else
        SIGNER_SED="s|^ *signer: *\(.*\)|signer: type=file;file_path=${SIG_DATA_PATH}/keys/\1|"
    fi

    # Prepare the signing YAML: set family, rewrite signer to config string,
    # and absolutize certificate paths in the SRK array.
    AHAB_SIGN_YAML="${B}/spsdk_ahab_sign.yaml"
    sed -e "s|^ *\(family:\).*|\1 ${SPSDK_FAMILY}|" \
        -e "${SIGNER_SED}" \
        -e "/srk_array/,/^[^ #]/{s|- \([^/][^ ]*\.pem\)|- ${SIG_DATA_PATH}/crts/\1|}" \
        "${SIG_DATA_PATH}/spsdk_ahab.yaml" > "${AHAB_SIGN_YAML}"

    bbnote "AHAB signing flash.bin for ${SPSDK_FAMILY} via nxpimage"

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

do_sign_boot_image() {
    if [ -n "${HSM_AHAB_KEY_ID}" ]; then
        do_sign_boot_image_key_provider
    else
        do_sign_boot_image_local
    fi
}

# Shared helper: deploy signed-flash.bin, rename original, and create tagged file.
_deploy_signed_flash_bin() {
    install -D -m 0644 -t "${DEPLOYDIR}/" "${B}/signed-flash.bin"
    mv "${DEPLOYDIR}/flash.bin" "${DEPLOYDIR}/unsigned-flash.bin"
    ln -sf "signed-flash.bin" "${DEPLOYDIR}/flash.bin"
    # As per https://github.com/Freescale/meta-freescale/commit/161f1b3e69a3cf011a50e9b742fb8c46d61e41e8, create a tagged file.
    cp -L "${DEPLOYDIR}/flash.bin" "${DEPLOYDIR}/flash.bin.tagged"
    stat -L -cUUUBURNXXOEUZX7+A-XY5601QQWWZ%sEND \
            "${DEPLOYDIR}/flash.bin.tagged" \
            >> "${DEPLOYDIR}/flash.bin.tagged"
    bbnote "AHAB: flash.bin signed successfully for ${SPSDK_FAMILY}"
}

do_deploy:append() {
    do_sign_boot_image
}
