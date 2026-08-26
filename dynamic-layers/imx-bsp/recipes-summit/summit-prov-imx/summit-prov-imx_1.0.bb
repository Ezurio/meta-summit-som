SUMMARY = "Summit ELE provisioning helper for NXP i.MX93"
DESCRIPTION = "Installs the ELE provisioning helper and offline-generated, non-secret provisioning artifacts used to derive a persistent OEM import master key, import a wrapped volatile fleet key, and decrypt an AES-GCM provisioning payload."

LICENSE = "Ezurio"
NO_GENERIC_LICENSE[Ezurio] = "LICENSE.ezurio"
LIC_FILES_CHKSUM = "file://LICENSE.ezurio;md5=fd3dd0630b215465b6f50540642d5b93"

DEPENDS = "imx-secure-enclave openssl-native python3-cryptography-native python3-pyyaml-native python3-spsdk-native"
RDEPENDS:${PN} = " \
    busybox \
    coreutils \
    imx-secure-enclave \
    opensc \
    openssl \
    optee-client \
    optee-os \
    summit-initdata \
    summit-initdata-secure-mount-data \
    "

PACKAGE_ARCH = "${MACHINE_ARCH}"
COMPATIBLE_MACHINE = "(mx93-generic-bsp)"

inherit python3native systemd

require conf/machine/include/summit-spsdk-family.inc

SUMMIT_PROV_IMX_NXP_PROD_KA_PUBLIC:mx93-generic-bsp = "imx93-nxp-prod-ka-public.bin.b64"

SRC_URI = " \
    file://LICENSE.ezurio \
    file://summit-prov-imx.c \
    file://encrypt_prov_data.py \
    file://generate_ele_artifacts.py \
    file://${SUMMIT_PROV_IMX_NXP_PROD_KA_PUBLIC} \
    file://summit-prov-imx.sh \
    file://summit-prov-imx.service \
    "

S = "${UNPACKDIR}"

SUMMIT_PROV_IMX_DATADIR = "${datadir}/summit-prov-imx"
# Avoid SPSDK's host-clock default so signed-message output is reproducible.
SUMMIT_PROV_IMX_ISSUE_DATE = "2026-08"

SYSTEMD_SERVICE:${PN} = "summit-prov-imx.service"
SYSTEMD_AUTO_ENABLE = "enable"

SUMMIT_PROV_IMX_PUBLIC_INPUTS = " \
    key-exchange.bin \
    oem-ka-public.bin \
    key-import-tlv.bin \
    "
SUMMIT_PROV_IMX_CRYPTO_INPUTS = " \
    oem-ka-private.pem \
    fleet-aes-256.bin \
    "

do_install[file-checksums] += "${@' '.join('%s/%s:True' % (d.expand('${SIG_DATA_PATH}/summit-prov-imx'), name) for name in d.getVar('SUMMIT_PROV_IMX_CRYPTO_INPUTS').split())}"
do_install[file-checksums] += "${SIG_DATA_PATH}/spsdk_ahab.yaml:True"

python __anonymous () {
    prov_data_dir = d.expand("${SIG_DATA_PATH}/prov_data")
    checksums = []
    if os.path.isdir(prov_data_dir):
        for root, _, files in os.walk(prov_data_dir):
            checksums.extend(
                "%s:True" % os.path.join(root, name) for name in sorted(files)
            )
    if checksums:
        d.appendVarFlag("do_install", "file-checksums", " " + " ".join(checksums))
}

FILES:${PN} += "${SUMMIT_PROV_IMX_DATADIR}"

do_compile () {
    ${CC} ${CPPFLAGS} ${CFLAGS} -Wall -Wextra -Werror -DPSA_COMPLIANT \
        "${S}/summit-prov-imx.c" \
        ${LDFLAGS} -lele_hsm \
        -o "${B}/summit-prov-imx"
}

do_install () {
    prov_data_dir="${SIG_DATA_PATH}/prov_data"
    crypto_dir="${SIG_DATA_PATH}/summit-prov-imx"
    [ -d "$prov_data_dir" ] || \
        bbfatal "i.MX provisioning data directory not found: $prov_data_dir"
    [ -d "$prov_data_dir/keystore" ] || \
        bbfatal "No keystore directory found in $prov_data_dir"
    [ -d "$crypto_dir" ] || \
        bbfatal "i.MX provisioning crypto directory not found: $crypto_dir"
    [ -f "${SIG_DATA_PATH}/spsdk_ahab.yaml" ] || \
        bbfatal "SPSDK AHAB configuration not found: ${SIG_DATA_PATH}/spsdk_ahab.yaml"

    for artifact in ${SUMMIT_PROV_IMX_CRYPTO_INPUTS}; do
        [ -f "$crypto_dir/$artifact" ] || \
            bbfatal "Missing i.MX provisioning crypto input: $crypto_dir/$artifact"
    done

    # Reject derived intermediates. The generator creates these temporarily in
    # ${B}, consumes them while wrapping the fleet key, and removes them.
    for secret in oem_import_mk_sk.bin oem_import_wrap_sk.bin \
                  oem_import_cmac_sk.bin shared_secret.bin; do
        [ ! -e "$crypto_dir/$secret" ] || \
            bbfatal "Unneeded secret provisioning material must not be present in $crypto_dir: $secret"
    done

    [ "$(stat -c %s "$crypto_dir/fleet-aes-256.bin")" -eq 32 ] || \
        bbfatal "fleet-aes-256.bin must be exactly 32 bytes"

    rm -rf "${B}/generated-ele" "${B}/ele-generation-work"
    ${PYTHON} "${S}/generate_ele_artifacts.py" \
        --sig-data-path "${SIG_DATA_PATH}" \
        --crypto-dir "$crypto_dir" \
        --nxp-prod-ka-public "${S}/${SUMMIT_PROV_IMX_NXP_PROD_KA_PUBLIC}" \
        --output-dir "${B}/generated-ele" \
        --work-dir "${B}/ele-generation-work" \
        --family "${SPSDK_FAMILY}" \
        --issue-date "${SUMMIT_PROV_IMX_ISSUE_DATE}"
    rmdir "${B}/ele-generation-work"

    [ "$(stat -c %s "${B}/generated-ele/key-exchange.bin")" -eq 576 ] || \
        bbfatal "Generated key-exchange.bin must be 576 bytes"
    [ "$(stat -c %s "${B}/generated-ele/oem-ka-public.bin")" -eq 64 ] || \
        bbfatal "Generated oem-ka-public.bin must be 64 bytes"
    [ "$(stat -c %s "${B}/generated-ele/key-import-tlv.bin")" -eq 141 ] || \
        bbfatal "Generated key-import-tlv.bin must be 141 bytes"

    # Build a deterministic archive from the product's prov_data directory, then
    # encrypt it with a fresh 96-bit GCM nonce. The fleet key itself is never
    # installed into the target image.
    tar --sort=name --mtime=@0 --owner=0 --group=0 --numeric-owner \
        -C "$prov_data_dir" -czf "${B}/prov-data.tar.gz" .
    ${PYTHON} "${S}/encrypt_prov_data.py" \
        "$crypto_dir/fleet-aes-256.bin" \
        "${B}/prov-data.tar.gz" \
        "${B}/prov-data.tar.gz.aes-gcm"
    rm -f "${B}/prov-data.tar.gz"

    [ "$(stat -c %s "${B}/prov-data.tar.gz.aes-gcm")" -le 1048604 ] || \
        bbfatal "Encrypted provisioning payload exceeds the 1 MiB plaintext limit"

    install -D -m 0755 "${B}/summit-prov-imx" \
        "${D}${sbindir}/summit-prov-imx"
    install -D -m 0755 "${S}/summit-prov-imx.sh" \
        "${D}${sbindir}/summit-prov-imx.sh"
    install -D -m 0644 "${S}/summit-prov-imx.service" \
        "${D}${systemd_system_unitdir}/summit-prov-imx.service"

    install -d -m 0755 "${D}${SUMMIT_PROV_IMX_DATADIR}"
    for artifact in ${SUMMIT_PROV_IMX_PUBLIC_INPUTS}; do
        install -m 0644 "${B}/generated-ele/$artifact" \
            "${D}${SUMMIT_PROV_IMX_DATADIR}/$artifact"
    done
    install -m 0644 "${B}/prov-data.tar.gz.aes-gcm" \
        "${D}${SUMMIT_PROV_IMX_DATADIR}/prov-data.tar.gz.aes-gcm"

    cd "${D}${SUMMIT_PROV_IMX_DATADIR}" || \
        bbfatal "Unable to enter installed provisioning data directory"
    openssl dgst -sha256 -r key-exchange.bin oem-ka-public.bin \
        key-import-tlv.bin prov-data.tar.gz.aes-gcm > \
        "${D}${SUMMIT_PROV_IMX_DATADIR}/SHA256SUMS"
}
