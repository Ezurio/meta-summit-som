# Common Cloud-HSM signing infrastructure shared between NXP, TI, and
# fitimage bbclasses. Backend-specific key materialization is supplied by
# summit-key-provider.

inherit summit-key-provider

KEY_PROVIDER_SIG_STAGING = "${UNPACKDIR}/key-provider-sig-data"
KEY_PROVIDER_SIGN_KEYDIR_ORIG := "${UBOOT_SIGN_KEYDIR}"

do_uboot_assemble_fitimage:prepend() {
    if [ -n "${@'1' if (d.getVar('UBOOT_KEY_PROVIDER_KEY_IDS') or '').split() else ''}" ]; then
        summit_key_provider_setup_env
    fi
}
