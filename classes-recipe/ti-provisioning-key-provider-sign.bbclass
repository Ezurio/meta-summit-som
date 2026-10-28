# TI provisioning-data signing via a Cloud-HSM key provider. The SMPK in
# SIG_KEY_PATH is a PKCS#11 wrapper; SMEK and prov_data stay in SIG_SUPPORT_PATH.

inherit summit-key-provider

DEPENDS:append = " ${SUMMIT_KEY_PROVIDER_DEPENDS}"

do_install[network] = "1"
do_install:prepend() {
    summit_key_provider_setup_env
}
