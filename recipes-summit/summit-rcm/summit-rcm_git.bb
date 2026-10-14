SUMMARY = "Summit Remote Control Manager (RCM)"

require summit-rcm.inc

inherit systemd

SRC_URI:append = "\
    file://ca.crt \
    file://server.crt \
    file://server.key \
    "

SYSTEMD_SERVICE:${PN} = "summit-rcm.service"
SYSTEMD_AUTO_ENABLE = "enable"

USERNAME ?= "root"
PASSWORD ?= "summit"

MANAGED_SOFTWARE_DEVICES ?= ""
UNMANAGED_HARDWARE_DEVICES ?= ""

CA_CERT_CHAIN_PATH ?= "/etc/summit-rcm/ssl/ca.crt"

SUMMIT_RCM_SERIAL_PORT ?= "/dev/ttymxc0"
SUMMIT_RCM_BAUD_RATE ?= "3000000"

PACKAGECONFIG[login-sessions] = "summit_rcm/rest_api/v2/login"
PACKAGECONFIG[v2_routes] = "summit_rcm/rest_api/v2/system summit_rcm/rest_api/v2/network summit_rcm/rest_api/services,,,${PYTHON_PN}-uvicorn ${PYTHON_PN}-falcon"
PACKAGECONFIG[legacy_routes] = "summit_rcm/rest_api/legacy summit_rcm/rest_api/services,,,${PYTHON_PN}-uvicorn ${PYTHON_PN}-falcon"
PACKAGECONFIG[at_interface] = "summit_rcm/at_interface summit_rcm/at_interface/commands summit_rcm/at_interface/services,,,${PYTHON_PN}-pyserial-asyncio ${PYTHON_PN}-transitions"

RDEPENDS:${PN} += "\
    ${PYTHON_PN} \
    ${PYTHON_PN}-core \
    ${PYTHON_PN}-crypt \
    ${PYTHON_PN}-datetime \
    ${PYTHON_PN}-io \
    ${PYTHON_PN}-asyncio \
    ${PYTHON_PN}-json \
    ${PYTHON_PN}-syslog \
    ${PYTHON_PN}-threading \
    ${PYTHON_PN}-aiofiles \
    ${PYTHON_PN}-dbus-fast \
    ${PYTHON_PN}-pyroute2 \
    summit-update \
    swclient \
    tzdata-core \
    tzdata-posix \
    "

export SUMMIT_RCM_EXTRA_PACKAGES = "\
    summit_rcm/services \
    ${PACKAGECONFIG_CONFARGS} \
    "

do_install:append() {
    install -D -m 644 -t "${D}${sysconfdir}/" "${S}/summit-rcm.ini"
    install -D -m 644 -t "${D}${sysconfdir}/summit-rcm/ssl" \
        "${UNPACKDIR}/server.key" "${UNPACKDIR}/server.crt" "${UNPACKDIR}/ca.crt"

    sed -i -e '/^default_/d' "${D}${sysconfdir}/summit-rcm.ini"
    sed -i -e '/\[summit-rcm\]/a default_password: \"${PASSWORD}\"' "${D}${sysconfdir}/summit-rcm.ini"
    sed -i -e '/\[summit-rcm\]/a default_username: \"${USERNAME}\"' "${D}${sysconfdir}/summit-rcm.ini"

    sed -i -e '/^managed_software_devices/d' "${D}${sysconfdir}/summit-rcm.ini"
    sed -i -e '/\[summit-rcm\]/a managed_software_devices: ${MANAGED_SOFTWARE_DEVICES}' "${D}${sysconfdir}/summit-rcm.ini"

    sed -i -e '/^unmanaged_hardware_devices/d' "${D}${sysconfdir}/summit-rcm.ini"
    sed -i -e '/\[summit-rcm\]/a unmanaged_hardware_devices: ${UNMANAGED_HARDWARE_DEVICES}' "${D}${sysconfdir}/summit-rcm.ini"

    sed -i -e '/^enable_allow_unauthenticated_reboot_reset/d' "${D}${sysconfdir}/summit-rcm.ini"
    sed -i -e '/\[summit-rcm\]/a enable_allow_unauthenticated_reboot_reset: ${@bb.utils.contains('PACKAGECONFIG','unauthenticated-reboot-reset','True','False',d)}' "${D}${sysconfdir}/summit-rcm.ini"

    sed -i -e '/^allow_multiple_user_sessions/d' "${D}${sysconfdir}/summit-rcm.ini"
    sed -i -e '/\[summit-rcm\]/a allow_multiple_user_sessions: ${@bb.utils.contains('PACKAGECONFIG','multiple-user-sessions','True','False',d)}' "${D}${sysconfdir}/summit-rcm.ini"

    sed -i -e 's,^tools.sessions.on:.*,tools.sessions.on: ${@bb.utils.contains('PACKAGECONFIG','login-sessions','True','False',d)},' "${D}${sysconfdir}/summit-rcm.ini"

    sed -i -e '/^enable_client_auth/d' "${D}${sysconfdir}/summit-rcm.ini"
    sed -i -e '/\[summit-rcm\]/a enable_client_auth: ${@bb.utils.contains('PACKAGECONFIG','client-authentication','True','False',d)}' "${D}${sysconfdir}/summit-rcm.ini"

    if ${@bb.utils.contains('PACKAGECONFIG','client-authentication','true','false',d)}; then
        sed -i -e 's,^server.ssl_certificate_chain:.*,server.ssl_certificate_chain: \"${CA_CERT_CHAIN_PATH}\",' "${D}${sysconfdir}/summit-rcm.ini"
    fi

    sed -i -e '/\[summit-rcm\]/a serial_port: \"${SUMMIT_RCM_SERIAL_PORT}\"' "${D}${sysconfdir}/summit-rcm.ini"
    sed -i -e '/\[summit-rcm\]/a baud_rate: ${SUMMIT_RCM_BAUD_RATE}' "${D}${sysconfdir}/summit-rcm.ini"

    if ${@bb.utils.contains('DISTRO_FEATURES','systemd','true','false',d)}; then
        install -D -m 644 -t "${D}${systemd_system_unitdir}" "${S}/summit-rcm.service"
    fi
}
