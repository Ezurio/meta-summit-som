SUMMARY = "Provides cryptographic recipes and primitives to python developers"
HOMEPAGE = "https://cryptography.io/"
SECTION = "devel/python"
LICENSE = "Apache-2.0 | BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=8c3617db4fb6fae01f1d253ab91511e4 \
                    file://LICENSE.APACHE;md5=4e168cce331e5c827d4c2b68a6200e1b \
                    file://LICENSE.BSD;md5=5ae30ba4123bc4f2fa49aa0b0dce887b \
                   "
LDSHARED += "-pthread"

SRC_URI[sha256sum] = "266f4ee051abb2f725b74ef8072b521ce1feacf685a3364fa6a6b45548db791a"

SRC_URI += "file://check-memfree.py \
            file://run-ptest \
            file://0001-pyproject-and-cargo-compat.patch \
           "

require python3-cryptography-48.0.1-crates.inc

inherit pypi python_maturin cargo-update-recipe-crates pkgconfig

PACKAGECONFIG ??= ""
PACKAGECONFIG[legacy-openssl] = ",,,openssl-ossl-module-legacy"

export CRYPTOGRAPHY_BUILD_OPENSSL_NO_LEGACY = "${@bb.utils.contains('PACKAGECONFIG', 'legacy-openssl', '0', '1', d)}"

DEPENDS += " \
    python3-cffi-native \
    openssl \
"

RDEPENDS:${PN} += " \
    python3-cffi \
"

RDEPENDS:${PN}:append:class-target = " \
    python3-numbers \
    python3-threading \
"

inherit ptest

do_install_ptest() {
    install -D ${WORKDIR}/check-memfree.py ${D}${PTEST_PATH}/
    install -d ${D}${PTEST_PATH}/tests
    cp -rf ${S}/tests/* ${D}${PTEST_PATH}/tests/
    rm -rf ${D}${PTEST_PATH}/tests/bench/test_x509.py
    install -d ${D}${PTEST_PATH}/tests/hazmat
    cp -rf ${S}/tests/hazmat/* ${D}${PTEST_PATH}/tests/hazmat/
    cp -r ${S}/pyproject.toml ${D}${PTEST_PATH}/
}

FILES:${PN}-dbg += " \
    ${PYTHON_SITEPACKAGES_DIR}/${SRCNAME}/hazmat/bindings/.debug \
"

CVE_PRODUCT = "cryptography.io:cryptography pyca:cryptography"

BBCLASSEXTEND = "native nativesdk"
