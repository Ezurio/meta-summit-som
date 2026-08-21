SUMMARY = "PKCS#11 provider using AWS KMS as backend (native)"
HOMEPAGE = "https://github.com/JackOfMostTrades/aws-kms-pkcs11"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=a6baf08cd8be1559ca9cb84c1fe0509b"

inherit pkgconfig

SRC_URI = "\
    git://github.com/JackOfMostTrades/aws-kms-pkcs11.git;protocol=https;branch=master \
    file://0001-aws_kms_pkcs11-add-config-file-path-env-variable.patch \
    file://0002-aws_kms_pkcs11-fix-support-for-CKM_RSA_PKCS_PSS.patch \
    file://0003-fix-attempt-using-atexit.patch \
    file://0004-aws_kms_pkcs11-on-delete-session-if-non-null.patch \
    file://0005-aws_kms_pkcs11-support-kms_key_arn-in-config.patch \
    file://aws-kms-pkcs11-openssl.cnf \
    "

SRCREV = "98ee45012319cd1a52781a78e85319230fb12c84"

DEPENDS += "aws-sdk-cpp openssl json-c p11-kit"

EXTRA_OEMAKE += "\
    AWS_SDK_PATH='${STAGING_DIR_HOST}/${prefix}' \
    AWS_SDK_LIB_PATH='${STAGING_LIBDIR}' \ 
    LIBRARY_PATH='${STAGING_LIBDIR}' \
    "

do_install() {
    install -D -m 0644 -t "${D}${libdir}/pkcs11" "${B}/aws_kms_pkcs11.so"

    install -d "${D}${sysconfdir}/ssl/openssl.cnf.d"
    sed -e "s|@LIBDIR@|${libdir}|g" \
        "${UNPACKDIR}/aws-kms-pkcs11-openssl.cnf" \
        > "${D}${sysconfdir}/ssl/openssl.cnf.d/aws-kms-pkcs11.cnf"
}

PACKAGES =+ "${PN}-openssl"
FILES:${PN}-openssl += "${sysconfdir}/ssl/openssl.cnf.d/aws-kms-pkcs11.cnf"
RDEPENDS:${PN}-openssl += "${PN} pkcs11-provider"

BBCLASSEXTEND = "native nativesdk"
