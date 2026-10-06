SUMMARY = "Summit ELE provisioning helper for NXP i.MX93"
DESCRIPTION = "Installs the ELE provisioning helper and offline-generated, non-secret provisioning artifacts used to derive a persistent OEM import master key, import a wrapped volatile fleet key, and decrypt an AES-GCM provisioning payload. Builds without SECURE_BOOT instead install an AES-CBC payload decrypted with a per-build key stored in the image."

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
inherit ${@oe.utils.ifelse( \
    '${CLOUD_HSM_BACKEND}', \
    'nxp-provisioning-key-provider-sign', \
    '')}

require conf/machine/include/summit-spsdk-family.inc

SRC_URI = " \
    file://LICENSE.ezurio \
    file://summit-prov-imx.c \
    file://encrypt_prov_data.py \
    file://generate_ele_artifacts.py \
    file://summit-prov-imx.sh \
    file://summit-prov-imx.service \
    "

S = "${UNPACKDIR}"

SUMMIT_PROV_IMX_DATADIR = "${datadir}/summit-prov-imx"
# Avoid SPSDK's host-clock default so signed-message output is reproducible.
SUMMIT_PROV_IMX_ISSUE_DATE = "2026-08"
SUMMIT_PROV_IMX_SIGNER ?= ""

SYSTEMD_SERVICE:${PN} = "summit-prov-imx.service"
SYSTEMD_AUTO_ENABLE = "enable"

SUMMIT_PROV_IMX_PUBLIC_INPUTS = " \
    key-exchange.bin \
    oem-ka-public.bin \
    key-import-tlv.bin \
    "
SUMMIT_PROV_IMX_CRYPTO_INPUTS = " \
    nxp-prod-ka-public.bin \
    oem-ka-private.pem \
    fleet-aes-256.bin \
    "

python __anonymous () {
    # Only secure builds use ELE and need the crypto and AHAB signing inputs.
    if d.getVar("SECURE_BOOT") == "1":
        crypto_dir = d.expand("${SIG_DATA_PATH}/summit-prov-imx")
        checksums = ["%s/%s:True" % (crypto_dir, name)
                     for name in d.getVar("SUMMIT_PROV_IMX_CRYPTO_INPUTS").split()]
        checksums.append(d.expand("${SIG_DATA_PATH}/spsdk_ahab.yaml:True"))
        d.appendVarFlag("do_install", "file-checksums", " " + " ".join(checksums))

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

    # Build a deterministic archive from the product's prov_data directory.
    tar --sort=name --mtime=@0 --owner=0 --group=0 --numeric-owner \
        -C "$prov_data_dir" -czf "${B}/prov-data.tar.gz" .
    # ELE decrypts the payload in one operation, limited by the kernel driver's
    # 128 KiB shared-memory buffer. The same limit applies to non-secure builds
    # so a prov_data that works there also works in a secure build.
    [ "$(stat -c %s "${B}/prov-data.tar.gz")" -le 65520 ] || \
        bbfatal "Provisioning archive exceeds the 65520-byte ELE limit"

    install -D -m 0755 "${B}/summit-prov-imx" \
        "${D}${sbindir}/summit-prov-imx"
    install -D -m 0755 "${S}/summit-prov-imx.sh" \
        "${D}${sbindir}/summit-prov-imx.sh"
    install -D -m 0644 "${S}/summit-prov-imx.service" \
        "${D}${systemd_system_unitdir}/summit-prov-imx.service"
    install -d -m 0755 "${D}${SUMMIT_PROV_IMX_DATADIR}"

    case "${SECURE_BOOT}" in
    1)
        summit_prov_imx_install_secure
        ;;
    *)
        summit_prov_imx_install_nonsecure
        ;;
    esac
    rm -f "${B}/prov-data.tar.gz"
}

# Wrap the fleet key for ELE and encrypt the archive with AES-256-GCM. Only
# public or wrapped material is installed; the fleet key never enters the image.
summit_prov_imx_install_secure () {
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
    SIGNER_ARGS=""
    if [ -n "${SUMMIT_PROV_IMX_SIGNER}" ]; then
        SIGNER_ARGS="--signer ${SUMMIT_PROV_IMX_SIGNER}"
    fi
    ${PYTHON} "${S}/generate_ele_artifacts.py" \
        --sig-data-path "${SIG_DATA_PATH}" \
        --crypto-dir "$crypto_dir" \
        --nxp-prod-ka-public "$crypto_dir/nxp-prod-ka-public.bin" \
        --output-dir "${B}/generated-ele" \
        --work-dir "${B}/ele-generation-work" \
        --family "${SPSDK_FAMILY}" \
        --issue-date "${SUMMIT_PROV_IMX_ISSUE_DATE}" \
        ${SIGNER_ARGS}
    rmdir "${B}/ele-generation-work"

    [ "$(stat -c %s "${B}/generated-ele/key-exchange.bin")" -eq 576 ] || \
        bbfatal "Generated key-exchange.bin must be 576 bytes"
    [ "$(stat -c %s "${B}/generated-ele/oem-ka-public.bin")" -eq 64 ] || \
        bbfatal "Generated oem-ka-public.bin must be 64 bytes"
    [ "$(stat -c %s "${B}/generated-ele/key-import-tlv.bin")" -eq 141 ] || \
        bbfatal "Generated key-import-tlv.bin must be 141 bytes"

    ${PYTHON} "${S}/encrypt_prov_data.py" \
        "$crypto_dir/fleet-aes-256.bin" \
        "${B}/prov-data.tar.gz" \
        "${B}/prov-data.tar.gz.aes-gcm"

    for artifact in ${SUMMIT_PROV_IMX_PUBLIC_INPUTS}; do
        install -m 0644 "${B}/generated-ele/$artifact" \
            "${D}${SUMMIT_PROV_IMX_DATADIR}/$artifact"
    done
    install -m 0644 "${B}/prov-data.tar.gz.aes-gcm" \
        "${D}${SUMMIT_PROV_IMX_DATADIR}/prov-data.tar.gz.aes-gcm"

    cd "${D}${SUMMIT_PROV_IMX_DATADIR}" || \
        bbfatal "Unable to enter installed provisioning data directory"
    openssl dgst -sha256 -r ${SUMMIT_PROV_IMX_PUBLIC_INPUTS} \
        prov-data.tar.gz.aes-gcm > "${D}${SUMMIT_PROV_IMX_DATADIR}/SHA256SUMS"
}

# Without secure boot, encrypt the archive with a random per-build AES-256-CBC
# key that is embedded in the wrapper script. The key is in cleartext in the
# rootfs, so this does not protect prov_data and is not for production. A
# per-build key is used so that no customer key is ever exposed this way.
summit_prov_imx_install_nonsecure () {
    key=$(openssl rand -hex 32)
    iv=$(openssl rand -hex 16)
    openssl enc -aes-256-cbc -K "$key" -iv "$iv" \
        -in "${B}/prov-data.tar.gz" \
        -out "${B}/prov-data.tar.gz.aes-cbc"

    sed -i -E \
        -e "s/^(DECRYPT_KEY=).*/\1\"$key\"/" \
        -e "s/^(DECRYPT_IV=).*/\1\"$iv\"/" \
        "${D}${sbindir}/summit-prov-imx.sh"
    chmod 0700 "${D}${sbindir}/summit-prov-imx.sh"

    install -m 0644 "${B}/prov-data.tar.gz.aes-cbc" \
        "${D}${SUMMIT_PROV_IMX_DATADIR}/prov-data.tar.gz.aes-cbc"

    cd "${D}${SUMMIT_PROV_IMX_DATADIR}" || \
        bbfatal "Unable to enter installed provisioning data directory"
    openssl dgst -sha256 -r prov-data.tar.gz.aes-cbc > \
        "${D}${SUMMIT_PROV_IMX_DATADIR}/SHA256SUMS"
}
