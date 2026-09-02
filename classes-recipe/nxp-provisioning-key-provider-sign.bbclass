# NXP ELE provisioning-data signing via a Cloud-HSM key provider.
#
# The provisioning recipe generates a signed key-exchange message during
# do_install. Replace the file-based SPSDK signer with the backend's PKCS#11
# signer while leaving the OEM key-agreement and fleet encryption inputs local.

inherit summit-key-provider

DEPENDS:append = "${@bb.utils.contains( \
    'HSM_AHAB_KEY_ID', \
    d.getVar('HSM_AHAB_KEY_ID'), \
    ' ' + d.getVar('SUMMIT_KEY_PROVIDER_DEPENDS') + \
    ' python3-spsdk-pkcs11-native', \
    '', \
    d)}"

SUMMIT_PROV_IMX_SIGNER = "${@';'.join(( \
    'type=pkcs11', \
    'so_path=' + d.getVar('HSM_PKCS11_LIBRARY'), \
    'token_label=' + d.getVar('HSM_AHAB_KEY_ID').rsplit('/', 1)[-1][:32], \
    'key_label=' + d.getVar('HSM_AHAB_KEY_ID').rsplit('/', 1)[-1], \
    'user_pin=unused')) if d.getVar('HSM_AHAB_KEY_ID') else ''}"

python () {
    enabled = bool(d.getVar('HSM_AHAB_KEY_ID'))
    d.setVarFlag('do_install', 'network', '1' if enabled else '0')
}

do_install:prepend() {
    if [ -n "${HSM_AHAB_KEY_ID}" ]; then
        summit_key_provider_setup_env

        if [ -z "${HSM_PKCS11_LIBRARY}" ] || \
           [ ! -e "${HSM_PKCS11_LIBRARY}" ]; then
            bbfatal "The ${CLOUD_HSM_BACKEND} backend did not provide a usable PKCS#11 library"
        fi

        if [ -z "${SUMMIT_PROV_IMX_SIGNER}" ]; then
            bbfatal "The ${CLOUD_HSM_BACKEND} backend did not provide an AHAB signer"
        fi
    fi
}

do_install[depends] += "${@bb.utils.contains( \
    'HSM_AHAB_KEY_ID', \
    d.getVar('HSM_AHAB_KEY_ID'), \
    d.getVar('SUMMIT_KEY_PROVIDER_TASK_DEPS'), \
    '', \
    d)}"