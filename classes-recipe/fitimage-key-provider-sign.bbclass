# Cloud-HSM FIT signing for recipes using kernel-fitimage or custom-fit-gen.

inherit uboot-key-provider-common

DEPENDS:append = "${@oe.utils.ifelse( \
    d.getVar('HSM_FIT_KEY_ID'), \
    ' ${SUMMIT_KEY_PROVIDER_DEPENDS}', \
    '')}"

# Stage FIT signing wrappers in UNPACKDIR so we never overwrite any existing
# keys in the original UBOOT_SIGN_KEYDIR.
KEY_PROVIDER_FIT_KEYDIR = "${UNPACKDIR}/key-provider-fit-keys"
UBOOT_SIGN_KEYDIR = "${@oe.utils.ifelse( \
    d.getVar('HSM_FIT_KEY_ID'), \
    d.getVar('KEY_PROVIDER_FIT_KEYDIR'), \
    d.getVar('KEY_PROVIDER_SIGN_KEYDIR_ORIG'))}"

_fitimage_key_provider_setup() {
    # Place a PEM wrapper and certificate only when the selected backend
    # supplies the FIT role. Other provider roles must leave local FIT signing
    # unchanged.
    if [ -n "${HSM_FIT_KEY_ID}" ]; then
        summit_key_provider_setup_env
        mkdir -p "${UBOOT_SIGN_KEYDIR}"
        FIT_TOKEN="${@summit_key_provider_token_label(d.getVar('HSM_FIT_KEY_ID'))}"
        if ! summit_key_provider_materialize_key "$FIT_TOKEN" \
            "${UBOOT_SIGN_KEYDIR}/${UBOOT_SIGN_KEYNAME}.key"; then
            bbfatal "${CLOUD_HSM_BACKEND} key materialization failed for the FIT key"
        fi
        summit_key_provider_materialize_cert \
            "${UBOOT_SIGN_KEYDIR}/${UBOOT_SIGN_KEYNAME}.key" \
            "${UBOOT_SIGN_KEYDIR}/${UBOOT_SIGN_KEYNAME}.crt" \
            "${UBOOT_SIGN_KEYNAME}"

        if [ -n "${UBOOT_SIGN_IMG_KEYNAME}" ] && \
           [ "${UBOOT_SIGN_IMG_KEYNAME}" != "${UBOOT_SIGN_KEYNAME}" ]; then
            if ! summit_key_provider_materialize_key "$FIT_TOKEN" \
                "${UBOOT_SIGN_KEYDIR}/${UBOOT_SIGN_IMG_KEYNAME}.key"; then
                bbfatal "${CLOUD_HSM_BACKEND} key materialization failed for the FIT IMG key"
            fi
            summit_key_provider_materialize_cert \
                "${UBOOT_SIGN_KEYDIR}/${UBOOT_SIGN_IMG_KEYNAME}.key" \
                "${UBOOT_SIGN_KEYDIR}/${UBOOT_SIGN_IMG_KEYNAME}.crt" \
                "${UBOOT_SIGN_IMG_KEYNAME}"
        fi
    fi
}

# For kernel-fitimage: replace local key generation with PEM wrapper placement
do_kernel_generate_rsa_keys:prepend() {
    _fitimage_key_provider_setup
}
# Skip local key generation only when the selected provider materializes the
# FIT key. Do not assign FIT_GENERATE_KEYS for other roles, so the recipe's
# original local-key behavior remains unchanged.
python () {
    enabled = bool(d.getVar('HSM_FIT_KEY_ID'))
    if enabled:
        d.setVar('FIT_GENERATE_KEYS', '0')
    for task in ('do_kernel_generate_rsa_keys', 'do_assemble_fitimage',
                 'do_assemble_fitimage_initramfs', 'do_compile',
                 'do_compile_fit'):
        d.setVarFlag(task, 'network', '1' if enabled else '0')
}

do_assemble_fitimage:prepend() {
    _fitimage_key_provider_setup
}

do_assemble_fitimage_initramfs:prepend() {
    _fitimage_key_provider_setup
}

# For custom-fit-gen users (e.g. summit-mcu-demos) where signing happens in do_compile
do_compile:prepend() {
    _fitimage_key_provider_setup
}

do_compile_fit[prefuncs] += "${@oe.utils.ifelse( \
    '${HSM_FIT_KEY_ID}', \
    '_fitimage_key_provider_setup', \
    '')}"
do_compile_fit[depends] += "${@oe.utils.ifelse( \
    '${HSM_FIT_KEY_ID}', \
    '${SUMMIT_KEY_PROVIDER_TASK_DEPS}', \
    '')}"
