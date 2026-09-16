# SWUpdate CMS signing via a dedicated Cloud-HSM key provider role.

inherit summit-key-provider

DEPENDS:append = "${@bb.utils.contains( \
    'HSM_UPDATE_KEY_ID', \
    d.getVar('HSM_UPDATE_KEY_ID'), \
    ' ' + d.getVar('SUMMIT_KEY_PROVIDER_DEPENDS'), \
    '', \
    d)}"

KEY_PROVIDER_UPDATE_KEYDIR = "${UNPACKDIR}/key-provider-update-keys"
SWUPDATE_UPDATE_SIGNING_CERT ?= ""

swupdate_key_provider_setup() {
    if [ -n "${HSM_UPDATE_KEY_ID}" ]; then
        summit_key_provider_setup_env

        if [ -z "${SWUPDATE_UPDATE_SIGNING_CERT}" ] || \
           [ ! -f "${SWUPDATE_UPDATE_SIGNING_CERT}" ]; then
            bbfatal "SWUPDATE_UPDATE_SIGNING_CERT must point to a stable certificate for the HSM update-signing key"
        fi

        rm -rf "${KEY_PROVIDER_UPDATE_KEYDIR}"
        mkdir -p "${KEY_PROVIDER_UPDATE_KEYDIR}"

        UPDATE_TOKEN="${@summit_key_provider_token_label(d.getVar('HSM_UPDATE_KEY_ID'))}"
        if ! summit_key_provider_materialize_key "$UPDATE_TOKEN" \
            "${KEY_PROVIDER_UPDATE_KEYDIR}/update_signing.key"; then
            bbfatal "${CLOUD_HSM_BACKEND} key materialization failed for the SWUpdate key"
        fi

        CERT_PUBLIC_KEY="${KEY_PROVIDER_UPDATE_KEYDIR}/certificate-public-key.pem"
        HSM_PUBLIC_KEY="${KEY_PROVIDER_UPDATE_KEYDIR}/hsm-public-key.pem"
        CERT_PUBLIC_KEY_DER="${KEY_PROVIDER_UPDATE_KEYDIR}/certificate-public-key.der"
        HSM_PUBLIC_KEY_DER="${KEY_PROVIDER_UPDATE_KEYDIR}/hsm-public-key.der"
        if ! openssl x509 -in "${SWUPDATE_UPDATE_SIGNING_CERT}" \
            -pubkey -noout > "$CERT_PUBLIC_KEY" || \
           ! openssl pkey -in "${KEY_PROVIDER_UPDATE_KEYDIR}/update_signing.key" \
            -pubout > "$HSM_PUBLIC_KEY" || \
           ! openssl pkey -pubin -in "$CERT_PUBLIC_KEY" -outform DER \
            -out "$CERT_PUBLIC_KEY_DER" || \
           ! openssl pkey -pubin -in "$HSM_PUBLIC_KEY" -outform DER \
            -out "$HSM_PUBLIC_KEY_DER" || \
           ! cmp -s "$CERT_PUBLIC_KEY_DER" "$HSM_PUBLIC_KEY_DER"; then
            bbfatal "SWUPDATE_UPDATE_SIGNING_CERT public key does not match HSM_UPDATE_KEY_ID"
        fi
    fi
}

do_install:prepend() {
    swupdate_key_provider_setup
}

python swupdate_key_provider_setup_swuimage() {
    bb.build.exec_func('swupdate_key_provider_setup', d)
}

do_swuimage[prefuncs] += "${@'swupdate_key_provider_setup_swuimage' \
    if d.getVar('HSM_UPDATE_KEY_ID') else ''}"

do_install[depends] += "${@bb.utils.contains( \
    'HSM_UPDATE_KEY_ID', \
    d.getVar('HSM_UPDATE_KEY_ID'), \
    d.getVar('SUMMIT_KEY_PROVIDER_TASK_DEPS'), \
    '', \
    d)}"
do_install[file-checksums] += "${@bb.utils.contains( \
    'HSM_UPDATE_KEY_ID', d.getVar('HSM_UPDATE_KEY_ID'), \
    d.getVar('SWUPDATE_UPDATE_SIGNING_CERT') + ':True', '', d)}"
do_swuimage[file-checksums] += "${@bb.utils.contains( \
    'HSM_UPDATE_KEY_ID', d.getVar('HSM_UPDATE_KEY_ID'), \
    d.getVar('SWUPDATE_UPDATE_SIGNING_CERT') + ':True', '', d)}"
python () {
    enabled = bool(d.getVar('HSM_UPDATE_KEY_ID'))
    d.setVarFlag('do_install', 'network', '1' if enabled else '0')
    d.setVarFlag('do_swuimage', 'network', '1' if enabled else '0')
}
