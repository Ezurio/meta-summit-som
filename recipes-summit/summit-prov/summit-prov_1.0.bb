SUMMARY = "Summit Provisioning Service"

LICENSE = "Ezurio"
NO_GENERIC_LICENSE[Ezurio] = "LICENSE.ezurio"
LIC_FILES_CHKSUM = "file://LICENSE.ezurio;md5=fd3dd0630b215465b6f50540642d5b93"

inherit allarch systemd deploy
inherit ${@oe.utils.ifelse( \
    '${CLOUD_HSM_BACKEND}', \
    bb.utils.contains('MACHINEOVERRIDES', 'k3', 'ti-provisioning-key-provider-sign', '', d), \
    '')}

SRC_URI = " \
    file://LICENSE.ezurio \
    file://summit-prov.service \
    file://summit-prov.sh \
    file://gen_core_x509_cert.sh \
    "

S = "${WORKDIR}"

FILES:${PN} += "\
    ${systemd_system_unitdir} \
    ${sbindir} \
    "

RDEPENDS:${PN} = "\
    openssl \
    opensc \
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

do_install:append:k3 () {
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

    case "${MACHINE}" in
        am6*-carbon-hs)
            # Secure target build, sign and encrypt the provisioning data using SMPK and
            # SMEK
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
            # If not a secure target build, encrypt the prov_data.tar.gz using SMEK and
            # inject the SMEK and IV into the summit-prov.sh script using sed (for
            # decryption during provisioning)
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
