SUMMARY = "Summit Provisioning Service"

LICENSE = "Ezurio"
NO_GENERIC_LICENSE[Ezurio] = "LICENSE.ezurio"
LIC_FILES_CHKSUM = "file://LICENSE.ezurio;md5=11ef601ae07d69cfcd7387a33b764027"

inherit allarch systemd deploy update-rc.d

SRC_URI = " \
    file://LICENSE.ezurio \
    file://summit-prov.service \
    file://summit-prov.init \
    file://summit-prov.sh \
    file://gen_core_x509_cert.sh \
    "

S = "${UNPACKDIR}"

FILES:${PN} += "\
    ${systemd_system_unitdir} \
    ${sysconfdir}/init.d \
    ${sbindir} \
    "

INITSCRIPT_NAME = "summit-prov"
INITSCRIPT_PARAMS = "defaults 90 10"

RDEPENDS:${PN} = "\
    openssl \
    opensc \
    optee-client \
    optee-os-ta \
    summit-initdata \
    keyutils \
    "

RDEPENDS:${PN}:append:k3 = "\
    kernel-module-am6xx-summit-prov \
    "

DEPENDS += " \
    openssl-native \
    "

SYSTEMD_SERVICE:${PN} = "summit-prov.service"
SYSTEMD_AUTO_ENABLE = "enable"

do_install:append () {
    install -D -m 0755 -t "${D}${sbindir}" \
        "${S}/summit-prov.sh"

    install -D -m 0755 "${S}/summit-prov.init" "${D}${sysconfdir}/init.d/${INITSCRIPT_NAME}"

    if ${@bb.utils.contains('DISTRO_FEATURES', 'systemd', 'true', 'false', d)}; then
        install -D -m 644 -t "${D}${systemd_system_unitdir}" "${S}/summit-prov.service"
    fi

    # Create a placeholder file for the encrypted provisioning data
    touch "${S}/prov_data.tar.gz_sign_enc.bin"
}

do_install:append:summit-secure () {
    sed -i -E -e "s/^(After=.*|Requires=.*)/\1 mount_data.service/" \
        "${D}${systemd_system_unitdir}/summit-prov.service"
}

do_install:append () {
    # Assume SMEK and SMPK are in the keys directory
    smek_path="${UBOOT_SIGN_KEYDIR}/smek.key"
    [ -f "${smek_path}" ] || \
        bbfatal "No SMEK key found in the keys directory"
    smpk_path="${UBOOT_SIGN_KEYDIR}/smpk.key"
    [ -f "${smpk_path}" ] || \
        bbfatal "No SMPK key found in the keys directory"
    prov_data_path="${UBOOT_SIGN_KEYDIR}/prov_data"
    [ -d "${prov_data_path}" ] || \
        bbfatal "No provisioning data directory found in the keys directory"
    keystore_source_path="${prov_data_path}/keystore"
    [ -d "${keystore_source_path}" ] || \
        bbfatal "No keystore directory found in the prov_data directory"

    # Create prov_data.tar.gz
    tar -C "${prov_data_path}" -czf "${S}/prov_data.tar.gz" .

    # Gate on SECURE_BOOT, not on the machine name. This used to test for an
    # "am6*-carbon-hs" MACHINE, but those were removed when TI dropped separate
    # HS machine configs, so that branch became unreachable.
    case "${SECURE_BOOT}" in
        1)
            # HS-SE part: sign and encrypt the provisioning data using SMPK and
            # SMEK.  The secure world verifies the certificate against the SMPK
            # hash fused into the device, so no key material ships in the rootfs.
            "${S}/gen_core_x509_cert.sh" \
                -b "${S}/prov_data.tar.gz" \
                -k "${smpk_path}" \
                -a 2 \
                -n \
                -y ENCRYPT \
                -e "${smek_path}" \
                -o "${S}/cert_prov_data.tar.gz.bin"
            cat "${S}/cert_prov_data.tar.gz.bin" "${S}/prov_data.tar.gz-ENC" > \
                "${S}/prov_data.tar.gz_sign_enc.bin"
            rm -f "${S}/cert_prov_data.tar.gz.bin" "${S}/prov_data.tar.gz-ENC"
            ;;
        *)
            # Development path (SECURE_BOOT unset/0): encrypt the prov_data.tar.gz using
            # SMEK and inject the SMEK and IV into the summit-prov.sh script using
            # sed (for decryption during provisioning).
            #
            # This embeds the key in cleartext in /usr/sbin/summit-prov.sh on the
            # rootfs and must not be used for a production device.
            KEY=$(xxd -p -c 0 "${smek_path}")
            IV=$(openssl rand -hex 16)
            openssl enc -aes-256-cbc \
                -in "${S}/prov_data.tar.gz" \
                -out "${S}/prov_data.tar.gz_sign_enc.bin" \
                -K "${KEY}" \
                -iv "${IV}"

            # Update the decryption key and IV variables for summit-prov.sh using sed
            sed -i -r \
                -e "s/^(DECRYPT_KEY=).*/\1\"${KEY}\"/" \
                -e "s/^(DECRYPT_IV=).*/\1\"${IV}\"/" \
                "${D}${sbindir}/summit-prov.sh"
            ;;
    esac
    rm -f "${S}/prov_data.tar.gz"

    # Verify the encrypted provisioning data file is not greater than 1MB
    if [ "$(stat -c%s "${S}/prov_data.tar.gz_sign_enc.bin")" -gt 1048576 ]; then
        bbfatal "Generated encrypted provisioning data file exceeds 1MB size limit"
    fi
}

do_deploy () {
    install -m 0644 -D -t "${DEPLOYDIR}" "${S}/prov_data.tar.gz_sign_enc.bin"
}

addtask deploy after do_install
