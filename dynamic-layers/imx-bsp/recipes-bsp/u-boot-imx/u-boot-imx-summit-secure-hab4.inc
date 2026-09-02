DEPENDS:append = " \
    cst-native \
    "

SRC_URI:append = "${@' file://0002-rsa-sign-load-openssl-config-for-provider-support.patch' if d.getVar('CLOUD_HSM_BACKEND') else ''}"

# HAB4 (imx8m): CSF/IMG key wrappers + FIT signing via CST/binman
inherit ${@'nxp-hab4-uboot-key-provider-sign' if d.getVar('CLOUD_HSM_BACKEND') else ''}

# binman's nxp-imx8mcst node (active when CONFIG_IMX_HAB is set) reads these
# from the environment to locate the HAB SRK table and CSF/IMG certificates for
# cst. They must be exported so every binman-invoking task (do_compile and
# do_uboot_assemble_fitimage) sees them. cst finds the matching private keys and
# key_pass.txt relative to these certs (sibling crts/ and keys/ directories).
export SRK_TABLE ?= "${SIG_DATA_PATH}/crts/SRK_1_2_3_4_table.bin"
export CSF_KEY ?= "${SIG_DATA_PATH}/crts/CSF1_1_sha256_2048_65537_v3_usr_crt.pem"
export IMG_KEY ?= "${SIG_DATA_PATH}/crts/IMG1_1_sha256_2048_65537_v3_usr_crt.pem"

do_configure:append() {
    # Enable HAB so SPL authenticates the FIT at runtime and the hab_status /
    # hab_auth_img commands are available. With CONFIG_IMX_HAB set, binman signs
    # the SPL and FIT sections at build time via its nxp-imx8mcst node using cst
    # (provided by cst-native), driven by the SRK_TABLE/CSF_KEY/IMG_KEY exports.
    echo "CONFIG_IMX_HAB=y" >> "${B}/.config"
}
