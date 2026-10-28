# Common Cloud-HSM signing setup shared between the NXP, TI, and FIT signing
# classes. The keys in SIG_KEY_PATH are PKCS#11 wrappers from
# summit-signing-keys-native, so signing needs the provider environment.

inherit summit-key-provider

DEPENDS:append = " ${SUMMIT_KEY_PROVIDER_DEPENDS}"

do_uboot_assemble_fitimage[network] = "1"
do_uboot_assemble_fitimage:prepend() {
    summit_key_provider_setup_env
}
