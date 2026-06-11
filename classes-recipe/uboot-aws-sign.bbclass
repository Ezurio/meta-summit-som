# Image signing with AWS KMS is enabled by populating AWS_KMS_KEY_ARN
DEPENDS += "aws-kms-pkcs11-native"

KEY_PATH = "${STAGING_DATADIR_NATIVE}/aws-kms-pkcs11/dev_pkcs11.pem"

do_compile[network] = "1"
do_uboot_assemble_fitimage[network] = "1"

do_compile:prepend() {
    OPENSSL_CNF="${STAGING_DIR_NATIVE}/usr/lib/ssl-3/openssl.cnf"
    if ! grep -q "pkcs11_sect" "$OPENSSL_CNF"; then
        sed -i '/^default = default_sect$/a pkcs11 = pkcs11_sect' "$OPENSSL_CNF"
        sed -i 's/^# activate = 1$/activate = 1/' "$OPENSSL_CNF"
        cat >> "$OPENSSL_CNF" <<OPENSSL_CONF_EOF

[pkcs11_sect]
module = ${STAGING_LIBDIR_NATIVE}/ossl-modules/pkcs11.so
pkcs11-module-path = ${STAGING_LIBDIR_NATIVE}/pkcs11/aws_kms_pkcs11.so
activate = 1
OPENSSL_CONF_EOF
    fi

    # aws_kms_pkcs11.so is loaded via dlopen() by the pkcs11 provider and has no
    # RPATH, so its AWS SDK dependencies must be discoverable via LD_LIBRARY_PATH.
    export AWS_KMS_PKCS11_CONFIG=${STAGING_DATADIR_NATIVE}/aws-kms-pkcs11/aws-kms-pkcs11-config.json
    export LD_LIBRARY_PATH=${STAGING_LIBDIR_NATIVE}:${LD_LIBRARY_PATH}
}

do_uboot_assemble_fitimage:prepend() {
    export AWS_KMS_PKCS11_CONFIG=${STAGING_DATADIR_NATIVE}/aws-kms-pkcs11/aws-kms-pkcs11-config.json
    export LD_LIBRARY_PATH=${STAGING_LIBDIR_NATIVE}:${LD_LIBRARY_PATH}
}
