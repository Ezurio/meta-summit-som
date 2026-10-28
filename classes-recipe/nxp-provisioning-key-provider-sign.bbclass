# NXP ELE provisioning-data signing via a Cloud-HSM key provider.
#
# The provisioning recipe generates a signed key-exchange message during
# do_install. Replace the file-based SPSDK signer with the backend's PKCS#11
# signer while leaving the OEM key-agreement and fleet encryption inputs local.

inherit summit-key-provider

DEPENDS:append = " ${SUMMIT_KEY_PROVIDER_DEPENDS} python3-spsdk-pkcs11-native"

SUMMIT_PROV_IMX_SIGNER = "${@';'.join(( \
    'type=pkcs11', \
    'so_path=' + d.getVar('HSM_PKCS11_LIBRARY'), \
    'token_label=' + summit_key_provider_token_label(d.getVar('HSM_AHAB_KEY_ID')), \
    'key_label=' + summit_key_provider_key_label(d.getVar('HSM_AHAB_KEY_ID')), \
    'user_pin=unused'))}"

do_install[network] = "1"
do_install:prepend() {
    if [ "${SECURE_BOOT}" = "1" ]; then
        summit_key_provider_setup_env

        if [ -z "${HSM_PKCS11_LIBRARY}" ] || \
           [ ! -e "${HSM_PKCS11_LIBRARY}" ]; then
            bbfatal "The ${CLOUD_HSM_BACKEND} backend did not provide a usable PKCS#11 library"
        fi
    fi
}
