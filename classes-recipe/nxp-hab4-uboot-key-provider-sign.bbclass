# NXP HAB4 + FIT image signing via a Cloud-HSM key provider.
#
# HAB4 CSF and image signing use SPSDK's direct PKCS#11 provider; mkimage signs
# the FIT with the PKCS#11 wrapper in SIG_KEY_PATH.

inherit uboot-key-provider-common

DEPENDS:append = " python3-spsdk-pkcs11-native"

SPSDK_HAB_CSF_SIGNER = "${@summit_key_provider_spsdk_signer(d, d.getVar('HSM_CSF_KEY_ID'))}"
SPSDK_HAB_IMG_SIGNER = "${@summit_key_provider_spsdk_signer(d, d.getVar('HSM_IMG_KEY_ID'))}"

do_compile[network] = "1"
do_compile:prepend() {
    summit_key_provider_setup_env
}
