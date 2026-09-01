SUMMARY = "NXP Code Signing Tool (CST)"
DESCRIPTION = "NXP i.MX Code Signing Tool (CST), supporting HABv4 and AHAB image \
signing. Built from source with dynamic OpenSSL linking to support provider-based \
key loading (e.g. PKCS#11 for AWS KMS signing)."
HOMEPAGE = "https://www.nxp.com/webapp/Download?colCode=IMX_CST_TOOL_NEW"

# Per NXP's Software Content Register, CST's outgoing license is BSD-3-Clause.
LICENSE = "BSD-3-Clause & Apache-2.0 & MIT & LGPL-2.1"
LIC_FILES_CHKSUM = "\
    file://../COPYING;md5=dc56c17219895403ffc9aea66e228c8c \
    file://../licenses/LICENSE.openssl;md5=3441526b1df5cc01d812c7dfc218cea6 \
    file://../licenses/LICENSE.oqsprovider;md5=ab9b4308908ace39992d3080dd26824a \
    file://../licenses/LICENSE.liboqs;md5=4b93ef2da47496727a4e8a59f443844e \
    file://../licenses/LICENSE.json-c;md5=de54b60fbbc35123ba193fea8ee216f2 \
    file://../licenses/LICENSE.libp11;md5=fad9b3332be894bab9bc501572864b29 \
    file://../licenses/LICENSE.hidapi;md5=6378e0b956311ba1f71ea2cc29d00484 \
    file://../licenses/LICENSE.libusb;md5=fbc093901857fcd118f065f900982c24 \
"

CST_URI ?= ""
CST_URI_SUM ?= ""

CST_URI:summit-internal = "https://${RFPROS_FILESHARE_AUTH}files.devops.rfpros.com/tools/nxp/cst/${PV}/IMX_CST_TOOL_NEW.tgz"
CST_URI_SUM:summit-internal = "60ffc243daa5e4e2ccfac8a9b74aec6d21446122a453fcb896dc52881d2a779d"

SRC_URI = " \
    ${CST_URI} \
    file://0001-cmake-add-option-for-dynamic-OpenSSL-linking.patch \
    file://0002-openssl-load-config-into-private-library-context.patch \
"
SRC_URI[sha256sum] = "${CST_URI_SUM}"

inherit cmake native

# The release tarball extracts to "cst-${PV}"; source is in src/ subdirectory.
S = "${UNPACKDIR}/cst-${PV}/src"

DEPENDS = "openssl-native json-c-native bison-native flex-native"

# Upstream code has unused-result warnings that trip -Werror from Yocto's CFLAGS.
CFLAGS += "-Wno-error=unused-result"

# Build only the tools we need, with dynamic OpenSSL and no optional backends.
EXTRA_OECMAKE = "\
    -DCST_OPENSSL_SHARED=ON \
    -DCST_WITH_PKCS11=OFF \
    -DCST_WITH_PQC=OFF \
    -DBUILD_HAB_LOG_PARSER=OFF \
    -DBUILD_CST=ON \
    -DBUILD_SRKTOOL=ON \
    -DBUILD_XHAB_PKI_TREE=ON \
    -DBUILD_AHAB_SIGNED_MESSAGE=ON \
    -Djson_c_DIR=${STAGING_LIBDIR_NATIVE}/.. \
"
