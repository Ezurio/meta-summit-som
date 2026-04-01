require recipes-bsp/u-boot/u-boot-ti.inc

inherit summit-platform-version

SUMMARY = "Summit U-Boot for TI devices"

UBOOT_GIT_URI = "${SUMMIT_EXTERNAL_GIT_URI}/u-boot-som.git"
UBOOT_GIT_URI:summit-internal = "${SUMMIT_INTERNAL_GIT_URI}/cp_linux-u-boot-som60.git"

UBOOT_GIT_PROTOCOL = "${SUMMIT_EXTERNAL_GIT_PROTOCOL}"
UBOOT_GIT_PROTOCOL:summit-internal = "${SUMMIT_INTERNAL_GIT_PROTOCOL}"

UBOOT_GIT_BRANCH = "${SUMMIT_PLATFORM_BRANCH}"

ENV_INCLUDE = ""
ENV_INCLUDE:k3 = "recipes-bsp/u-boot-summit/u-boot-summit-env.inc"

require ${ENV_INCLUDE}

# Image signing with AWS KMS is enabled by populating AWS_KMS_KEY_ARN
AWS_KMS_KEY_ARN ?= ""

ENABLE_AWS_KMS_SIGNING = "${@'1' if d.getVar('AWS_KMS_KEY_ARN') else '0'}"

DEPENDS:append = " \
    ${@'aws-kms-pkcs11-native' if d.getVar('ENABLE_AWS_KMS_SIGNING') == '1' else ''} \
    "

KEY_PATH = "${@'${STAGING_DATADIR_NATIVE}/aws-kms-pkcs11/dev_pkcs11.pem' if d.getVar('ENABLE_AWS_KMS_SIGNING') == '1' else '${UBOOT_SIGN_KEYDIR}/${UBOOT_SIGN_KEYNAME}.key'}"

EXTRA_OEMAKE:append:k3 = " KEY_PATH=${KEY_PATH}"
EXTRA_OEMAKE:append:k3r5 = " KEY_PATH=${KEY_PATH}"

do_compile[network] = "${ENABLE_AWS_KMS_SIGNING}"
do_uboot_assemble_fitimage[network] = "${ENABLE_AWS_KMS_SIGNING}"

do_compile:prepend() {
    if [ "${ENABLE_AWS_KMS_SIGNING}" = "1" ]; then
        export AWS_KMS_PKCS11_CONFIG="${STAGING_DATADIR_NATIVE}/aws-kms-pkcs11/aws-kms-pkcs11-config.json"

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
        export LD_LIBRARY_PATH="${STAGING_LIBDIR_NATIVE}:${LD_LIBRARY_PATH}"
    fi
}

do_uboot_assemble_fitimage:prepend() {
    if [ "${ENABLE_AWS_KMS_SIGNING}" = "1" ]; then
        export AWS_KMS_PKCS11_CONFIG="${STAGING_DATADIR_NATIVE}/aws-kms-pkcs11/aws-kms-pkcs11-config.json"
        export LD_LIBRARY_PATH="${STAGING_LIBDIR_NATIVE}:${LD_LIBRARY_PATH}"
    fi
}

COMPATIBLE_MACHINE = "(ti-soc)"
