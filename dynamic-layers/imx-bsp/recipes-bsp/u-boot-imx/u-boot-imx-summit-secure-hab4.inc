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
export SPSDK_HAB_SRK_TABLE ?= "${SIG_DATA_PATH}/crts/SRK_1_2_3_4_table.bin"
export SPSDK_HAB_CSF_CERT ?= "${SIG_DATA_PATH}/crts/CSF1_1_sha256_2048_65537_v3_usr_crt.pem"
export SPSDK_HAB_IMG_CERT ?= "${SIG_DATA_PATH}/crts/IMG1_1_sha256_2048_65537_v3_usr_crt.pem"
export SPSDK_HAB_CSF_SIGNER ?= "type=file;file_path=${SIG_DATA_PATH}/keys/CSF1_1_sha256_2048_65537_v3_usr_key.pem;password-file=${SIG_DATA_PATH}/keys/key_pass.txt;password-index=0"
export SPSDK_HAB_IMG_SIGNER ?= "type=file;file_path=${SIG_DATA_PATH}/keys/IMG1_1_sha256_2048_65537_v3_usr_key.pem;password-file=${SIG_DATA_PATH}/keys/key_pass.txt;password-index=1"

def summit_hab4_spsdk_signer_or_orig(d, key_var, orig_var):
    """Use an HSM signer for a configured role, otherwise keep its local signer."""
    key_id = d.getVar(key_var)
    if d.getVar('CLOUD_HSM_BACKEND') and key_id:
        return summit_key_provider_spsdk_signer(d, key_id)
    return d.getVar(orig_var) or ''

# HAB4 signing uses SPSDK's direct PKCS#11 provider. FIT signing continues to
# use PEM wrappers because mkimage requires a PEM key and certificate.
SPSDK_HAB_CSF_SIGNER_ORIG := "${SPSDK_HAB_CSF_SIGNER}"
SPSDK_HAB_IMG_SIGNER_ORIG := "${SPSDK_HAB_IMG_SIGNER}"
SPSDK_HAB_CSF_SIGNER = "${@summit_hab4_spsdk_signer_or_orig(d, 'HSM_CSF_KEY_ID', 'SPSDK_HAB_CSF_SIGNER_ORIG')}"
SPSDK_HAB_IMG_SIGNER = "${@summit_hab4_spsdk_signer_or_orig(d, 'HSM_IMG_KEY_ID', 'SPSDK_HAB_IMG_SIGNER_ORIG')}"

do_configure:append() {
    # Enable HAB so SPL authenticates the FIT at runtime and the hab_status /
    # hab_auth_img commands are available. With CONFIG_IMX_HAB set, binman signs
    # the SPL and FIT sections at build time via the SPSDK HAB4 entry. The CST
    # binman entry remains available in U-Boot for upstream compatibility, but
    # this Yocto integration intentionally always selects SPSDK.
    echo "CONFIG_IMX_HAB=y" >> "${B}/.config"
    echo "# CONFIG_IMX_HAB_CST is not set" >> "${B}/.config"
}
