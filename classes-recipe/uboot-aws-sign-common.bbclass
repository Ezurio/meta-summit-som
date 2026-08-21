# Common AWS KMS signing infrastructure shared between NXP, TI, and
# fitimage bbclasses.  Provides: variable defaults, dependencies, OpenSSL
# config setup, environment exports, and helper shell functions.

AWS_KMS_FIT_KEY_ARN ?= "${AWS_KMS_KEY_ARN}"
AWS_KMS_CERT_DAYS ?= "3650"

DEPENDS += "\
    aws-kms-pkcs11-native \
    aws-kms-pkcs11-config-native \
    pkcs11-provider-native \
    "

KMS_SIG_STAGING = "${UNPACKDIR}/kms-sig-data"
KMS_SIGN_KEYDIR_ORIG := "${UBOOT_SIGN_KEYDIR}"

do_compile[network] = "1"
do_uboot_assemble_fitimage[network] = "1"
do_assemble_fitimage[network] = "1"
do_assemble_fitimage_initramfs[network] = "1"

# Helper: extract PKCS#11 token label (first 32 chars of key UUID) from an ARN.
# CK_TOKEN_INFO.label is exactly 32 bytes per the PKCS#11 spec.
def aws_kms_token_label(arn):
    key_id = (arn or '').rsplit('/', 1)[-1]
    return key_id[:32]

# Helper: extract the full key ID (UUID) from an ARN for CKA_LABEL lookups.
# aws_kms_pkcs11.so reports key CKA_LABEL as the full key ID without truncation.
def aws_kms_key_label(arn):
    return (arn or '').rsplit('/', 1)[-1]

# Export KMS environment variables.
# Designed to be called at the top of do_compile:prepend() in subclasses.
# OPENSSL_CONF points at the default native config (extended via openssl.cnf.d
# by the openssl bbappend + aws-kms-pkcs11's drop-in) - no custom cnf is built.
# CST's OPENSSL_CONF-gated config load and U-Boot's rsa-sign patch both need
# this exported explicitly since neither goes through the openssl wrapper script.
uboot_aws_kms_setup_env() {
    export AWS_KMS_PKCS11_CONFIG="${STAGING_DATADIR_NATIVE}/aws-kms-pkcs11/aws-kms-pkcs11-config.json"
    export LD_LIBRARY_PATH="${STAGING_LIBDIR_NATIVE}:${LD_LIBRARY_PATH}"
    export OPENSSL_CONF="${STAGING_DIR_NATIVE}${sysconfdir}/ssl/openssl.cnf"
}

# Generate a PKCS#11 PEM wrapper for a KMS key via uri2pem.py.
# Usage: uboot_aws_kms_gen_wrapper <token_label> <output_path>
# Returns non-zero if key validation fails.
uboot_aws_kms_gen_wrapper() {
    nativepython3 "${STAGING_BINDIR_NATIVE}/uri2pem.py" \
        --bypass --verify \
        --out "$2" \
        "pkcs11:token=$1;type=private"
}

# Generate a self-signed X.509 certificate from a PEM wrapper key.
# mkimage requires a certificate (not a raw public key) for FIT verification.
# Usage: uboot_aws_kms_gen_cert <key_path> <cert_path> <common_name>
uboot_aws_kms_gen_cert() {
    "${STAGING_DIR_NATIVE}/usr/bin/openssl" req -new -x509 \
        -key "$1" \
        -out "$2" \
        -days "${AWS_KMS_CERT_DAYS}" -nodes \
        -subj "/CN=$3"
}

do_uboot_assemble_fitimage:prepend() {
    export AWS_KMS_PKCS11_CONFIG="${STAGING_DATADIR_NATIVE}/aws-kms-pkcs11/aws-kms-pkcs11-config.json"
    export LD_LIBRARY_PATH="${STAGING_LIBDIR_NATIVE}:${LD_LIBRARY_PATH}"
    export OPENSSL_CONF="${STAGING_DIR_NATIVE}${sysconfdir}/ssl/openssl.cnf"
}
