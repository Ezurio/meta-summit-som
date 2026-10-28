DEPENDS:append = " \
    python3-spsdk-native \
    python3-spsdk-pkcs11-native \
    "

SRC_URI:append = "${@oe.utils.ifelse( \
    '${CLOUD_HSM_BACKEND}', \
    ' file://0002-rsa-sign-load-openssl-config-for-provider-support.patch', \
    '')}"

# HAB4 (imx8m): SPSDK CSF/IMG signing plus FIT signing.
inherit ${@oe.utils.ifelse( \
    '${CLOUD_HSM_BACKEND}', \
    'nxp-hab4-uboot-key-provider-sign', \
    '')}

# The SPSDK binman entry uses explicit paths rather than CST's implicit
# crts/->keys/ discovery.  `password-file` is resolved by spsdk_hab4_sign.py
# at build time, keeping the key passphrase out of BitBake metadata and logs.
# With a Cloud HSM, nxp-hab4-uboot-key-provider-sign sets PKCS#11 signers.
export SPSDK_HAB_SRK_TABLE ?= "${SIG_SUPPORT_PATH}/crts/SRK_1_2_3_4_table.bin"
export SPSDK_HAB_CSF_CERT ?= "${SIG_SUPPORT_PATH}/crts/CSF1_1_sha256_2048_65537_v3_usr_crt.pem"
export SPSDK_HAB_IMG_CERT ?= "${SIG_SUPPORT_PATH}/crts/IMG1_1_sha256_2048_65537_v3_usr_crt.pem"
export SPSDK_HAB_CSF_SIGNER ?= "type=file;file_path=${SIG_KEY_PATH}/CSF1_1_sha256_2048_65537_v3_usr_key.pem;password-file=${SIG_KEY_PATH}/key_pass.txt;password-index=0"
export SPSDK_HAB_IMG_SIGNER ?= "type=file;file_path=${SIG_KEY_PATH}/IMG1_1_sha256_2048_65537_v3_usr_key.pem;password-file=${SIG_KEY_PATH}/key_pass.txt;password-index=1"

do_configure:append() {
    # Enable HAB so SPL authenticates the FIT at runtime and the hab_status /
    # hab_auth_img commands are available. With CONFIG_IMX_HAB set, binman signs
    # the SPL and FIT sections at build time via the SPSDK HAB4 entry. The CST
    # binman entry remains available in U-Boot for upstream compatibility, but
    # this Yocto integration intentionally always selects SPSDK.
    echo "CONFIG_IMX_HAB=y" >> "${B}/.config"
    echo "CONFIG_SUMMIT_AUTO_CLOSE=y" >> "${B}/.config"
    echo "# CONFIG_IMX_HAB_CST is not set" >> "${B}/.config"
}
