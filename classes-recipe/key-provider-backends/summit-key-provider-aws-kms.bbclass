# AWS KMS implementation of the summit-key-provider interface.

HSM_KEY_ID = "${AWS_KMS_KEY_ARN}"
HSM_CSF_KEY_ID = "${@d.getVar('AWS_KMS_CSF_KEY_ARN') or d.getVar('AWS_KMS_KEY_ARN')}"
HSM_IMG_KEY_ID = "${@d.getVar('AWS_KMS_IMG_KEY_ARN') or d.getVar('AWS_KMS_KEY_ARN')}"
HSM_FIT_KEY_ID = "${AWS_KMS_FIT_KEY_ARN}"
HSM_AHAB_KEY_ID = "${@d.getVar('AWS_KMS_AHAB_KEY_ARN') or d.getVar('AWS_KMS_KEY_ARN')}"
HSM_UPDATE_KEY_ID = "${@d.getVar('AWS_KMS_UPDATE_KEY_ARN') or ''}"

SUMMIT_KEY_PROVIDER_DEPENDS = "\
    aws-kms-pkcs11-native \
    aws-kms-pkcs11-config-native \
    pkcs11-provider-native \
    "
SUMMIT_KEY_PROVIDER_TASK_DEPS = "\
    aws-kms-pkcs11-native:do_populate_sysroot \
    aws-kms-pkcs11-config-native:do_populate_sysroot \
    pkcs11-provider-native:do_populate_sysroot \
    "
HSM_PKCS11_LIBRARY = "${STAGING_LIBDIR_NATIVE}/pkcs11/aws_kms_pkcs11.so"

summit_key_provider_setup_env() {
    export AWS_KMS_PKCS11_CONFIG="${STAGING_DATADIR_NATIVE}/aws-kms-pkcs11/aws-kms-pkcs11-config.json"
    export LD_LIBRARY_PATH="${STAGING_LIBDIR_NATIVE}:${LD_LIBRARY_PATH}"
    export OPENSSL_CONF="${STAGING_DIR_NATIVE}${sysconfdir}/ssl/openssl.cnf"
}

# CK_TOKEN_INFO.label is exactly 32 bytes. aws-kms-pkcs11 uses the key UUID as
# the token label and truncates it to that limit.
def summit_key_provider_token_label(key_id):
    return (key_id or '').rsplit('/', 1)[-1][:32]

# CKA_LABEL contains the complete AWS KMS key UUID.
def summit_key_provider_key_label(key_id):
    return (key_id or '').rsplit('/', 1)[-1]

summit_key_provider_materialize_key() {
    nativepython3 "${STAGING_BINDIR_NATIVE}/uri2pem.py" \
        --bypass --verify \
        --out "$2" \
        "pkcs11:token=$1;type=private"
}

summit_key_provider_materialize_cert() {
    "${STAGING_DIR_NATIVE}/usr/bin/openssl" req -new -x509 \
        -key "$1" \
        -out "$2" \
        -days "${CLOUD_HSM_CERT_DAYS}" -nodes \
        -subj "/CN=$3"
}
