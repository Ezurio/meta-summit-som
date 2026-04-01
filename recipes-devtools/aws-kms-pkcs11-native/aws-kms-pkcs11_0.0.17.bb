SUMMARY = "PKCS#11 provider using AWS KMS as backend (native)"
HOMEPAGE = "https://github.com/JackOfMostTrades/aws-kms-pkcs11"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=a6baf08cd8be1559ca9cb84c1fe0509b"

SRC_URI = "git://github.com/JackOfMostTrades/aws-kms-pkcs11.git;protocol=https;branch=master"
SRCREV = "v${PV}"

FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SRC_URI += "\
    file://0001-aws_kms_pkcs11-add-config-file-path-env-variable.patch \
    file://0002-aws_kms_pkcs11-fix-support-for-CKM_RSA_PKCS_PSS.patch \
    file://0003-fix-attempt-using-atexit.patch \
    file://0004-aws_kms_pkcs11-on-delete-session-if-non-null.patch \
    file://0005-aws_kms_pkcs11-support-kms_key_arn-in-config.patch \
    file://aws-kms-pkcs11-config.json \
    "

S = "${WORKDIR}/git"

inherit native

DEPENDS = " \
    aws-sdk-cpp-native \
    openssl-native \
    json-c-native \
    p11-kit-native \
    pkcs11-provider-native \
    python3-asn1crypto-native \
    "

do_configure() {
    if [ -z "${AWS_KMS_KEY_ARN}" ]; then
        bbfatal "Required variable AWS_KMS_KEY_ARN not set."
    fi
}

EXTRA_OEMAKE += "\
    AWS_SDK_PATH='${STAGING_DIR_NATIVE}/usr' \
    AWS_SDK_LIB_PATH='${STAGING_LIBDIR_NATIVE}' \
    PKCS11_INC='-I${STAGING_INCDIR_NATIVE}/p11-kit-1/p11-kit' \
    PKCS11_MOD_PATH='${STAGING_LIBDIR_NATIVE}/pkcs11' \
    JSON_C_INC='-I${STAGING_INCDIR_NATIVE}/json-c' \
    LIBRARY_PATH='${STAGING_LIBDIR_NATIVE}' \
    "

# Extract the key ID (UUID) from the full ARN (arn:aws:kms:<region>:<account>:key/<key-id>)
AWS_KMS_KEY_ID = "${@(d.getVar('AWS_KMS_KEY_ARN') or '').rsplit('/', 1)[-1]}"

# PKCS#11 slot label: first 32 characters of the key ID
AWS_KMS_SLOT_LABEL = "${@(d.getVar('AWS_KMS_KEY_ID') or '')[:32]}"

# Path to the key file relative to the native sysroot, used for signing U-Boot. This is passed to
# the Makefile via EXTRA_OEMAKE.
PKCS11_KEY_PATH ?= "${datadir}/aws-kms-pkcs11/dev_pkcs11.pem"

do_install() {
    install -d "${D}${libdir}/pkcs11"
    cp -P "${B}/aws_kms_pkcs11.so" "${D}${libdir}/pkcs11/"

    install -m 0644 -D -t "${D}${datadir}/aws-kms-pkcs11" \
        "${WORKDIR}/aws-kms-pkcs11-config.json"
    sed -i "s|@@KMS_KEY_ARN@@|${AWS_KMS_KEY_ARN}|g" \
        "${D}${datadir}/aws-kms-pkcs11/aws-kms-pkcs11-config.json"

    install -d "$(dirname "${D}${PKCS11_KEY_PATH}")"
    nativepython3 "${STAGING_BINDIR_NATIVE}/uri2pem.py" \
        --bypass \
        --out "${D}${PKCS11_KEY_PATH}" \
        "pkcs11:token=${AWS_KMS_SLOT_LABEL};type=private"
}

FILES:${PN} = " \
    ${libdir}/pkcs11/aws_kms_pkcs11.so \
    ${datadir}/aws-kms-pkcs11/aws-kms-pkcs11-config.json \
    ${PKCS11_KEY_PATH} \
    "

BBCLASSEXTEND = "native nativesdk"
