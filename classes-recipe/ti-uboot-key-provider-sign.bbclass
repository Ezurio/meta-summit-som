# TI boot image and FIT signing via a Cloud-HSM key provider. Binman and
# mkimage sign with the PKCS#11 wrappers in SIG_KEY_PATH.

inherit uboot-key-provider-common

do_compile[network] = "1"
do_compile:prepend() {
    summit_key_provider_setup_env
}
