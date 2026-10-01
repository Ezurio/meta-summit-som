SUMMARY = "Summit Remote Control Manager (Rust/Axum port)"
HOMEPAGE = "https://github.com/Ezurio/summit-rcm-rust"
DESCRIPTION = "Rust/Axum port of the Summit RCM remote configuration manager"
LICENSE = "Ezurio-Clause"
LIC_FILES_CHKSUM = "file://LICENSE.ezurio;md5=fd3dd0630b215465b6f50540642d5b93"

inherit cargo pkgconfig systemd summit-platform-version

# ---------------------------------------------------------------------------
# Source
# ---------------------------------------------------------------------------

SRC_URI = "${SUMMIT_EXTERNAL_GIT_URI}/summit-rcm-rust.git;${SUMMIT_EXTERNAL_GIT_SUFFIX}"
SRC_URI:summit-internal = "${SUMMIT_INTERNAL_GIT_URI}/cp_apps-summit-rcm-rust.git;${SUMMIT_INTERNAL_GIT_SUFFIX}"

SRC_URI:append = " \
    file://summit-rcm.service \
    file://ca.crt \
    file://server.crt \
    file://server.key \
"

# SSL certificates are reused from the summit-rcm recipe's files directory
FILESEXTRAPATHS:prepend := "${THISDIR}/files:${THISDIR}/../summit-rcm/files:"

# ---------------------------------------------------------------------------
# Cargo / vendoring
#
# The source tree ships a complete vendor/ directory.  Instruct the BitBake
# cargo infrastructure to use it directly and suppress any attempt to fetch
# from the network.
# ---------------------------------------------------------------------------

CARGO_VENDORING_DIRECTORY = "${S}/vendor"
CARGO_DISABLE_BITBAKE_VENDORING = "1"
CARGO_BUILD_FLAGS += "--offline --locked"

# Remap source paths embedded in panic/tracing metadata so shipped binaries
# do not expose absolute build-host paths.
RUSTFLAGS += "--remap-path-prefix=${S}=."
export CARGO_PROFILE_RELEASE_LTO = "true"
# swupdate's libswupdate headers, for the update plugin's bindgen/cc build script
export SWUPDATE_INCLUDE_DIR = "${STAGING_INCDIR}"

DEPENDS += "openssl"

# ---------------------------------------------------------------------------
# Feature flags → PACKAGECONFIG
#
# Each entry's first field is the cargo flag(s) appended via
# PACKAGECONFIG_CONFARGS when the feature is enabled.
# Second/third fields are additional build-time / runtime dependencies.
# Entries with empty first fields are INI-only boolean toggles (no Cargo
# feature; checked with bb.utils.contains in do_install).
# ---------------------------------------------------------------------------

# Interface features — select at least one
PACKAGECONFIG[api-v2]          = "--features api-v2,,"
PACKAGECONFIG[api-legacy]      = "--features api-legacy,,"
PACKAGECONFIG[at-interface]    = "--features at-interface,,"

# Plugin features
PACKAGECONFIG[date-time]       = "--features date-time,,"
PACKAGECONFIG[files]           = "--features files,,"
PACKAGECONFIG[login]           = "--features login,,"
PACKAGECONFIG[logs]            = "--features logs,,"
PACKAGECONFIG[network]         = "--features network,,"
PACKAGECONFIG[network-manager] = "--features network-manager,,"
PACKAGECONFIG[system]          = "--features system,,"
PACKAGECONFIG[update]          = "--features update,,summit-update swupdate"
PACKAGECONFIG[awm]             = "--features awm,,"
PACKAGECONFIG[cww]             = "--features cww,,"
PACKAGECONFIG[bluetooth]       = "--features bluetooth,,,bluez5"
PACKAGECONFIG[bluetooth-hid]   = "--features bluetooth-hid,,,bluez5"
PACKAGECONFIG[bluetooth-vsp]   = "--features bluetooth-vsp,,,bluez5"
PACKAGECONFIG[chrony]          = "--features chrony,,,chrony"
PACKAGECONFIG[fips]            = "--features fips,,"
PACKAGECONFIG[firewall]        = "--features firewall,,"
PACKAGECONFIG[log-forwarding]  = "--features log-forwarding,,,systemd-journal-remote"
PACKAGECONFIG[provisioning]    = "--features provisioning,,"
PACKAGECONFIG[radio-siso-mode] = "--features radio-siso-mode,,"
PACKAGECONFIG[stunnel]         = "--features stunnel,,,stunnel"
PACKAGECONFIG[unauthenticated] = "--features unauthenticated,,"

# REST API docs — swagger-ui bundles the Swagger UI assets at build time;
# api-docs generates the openapi.json at build time (or embeds it at runtime).
PACKAGECONFIG[swagger-ui]      = "--features swagger-ui,,"
PACKAGECONFIG[api-docs]        = "--features api-docs,,"

# INI-only boolean options (affect summit-rcm.ini but not the Cargo build)
PACKAGECONFIG[enable-sessions]                         = ",,,"
PACKAGECONFIG[allow-multiple-user-sessions]            = ",,,"
PACKAGECONFIG[client-authentication]                   = ",,,"
PACKAGECONFIG[disable-certificate-expiry-verification] = ",,,"
PACKAGECONFIG[client-pairing]                          = ",,,"
PACKAGECONFIG[log-routes-loaded]                       = ",,,"
PACKAGECONFIG[restrict-network-status]                 = ",,,"
PACKAGECONFIG[api-docs-root-redirect]                  = ",,,"

# Default feature set: v2 API + common plugins + session support
PACKAGECONFIG ??= " \
    api-v2 \
    date-time \
    files \
    login \
    logs \
    network \
    network-manager \
    system \
    enable-sessions \
    disable-certificate-expiry-verification \
"

# Always pass --no-default-features; individual features come from
# PACKAGECONFIG_CONFARGS which is appended to CARGO_BUILD_FLAGS by the
# cargo bbclass at build time.
CARGO_BUILD_FLAGS += "--no-default-features"

# ---------------------------------------------------------------------------
# Systemd
# ---------------------------------------------------------------------------

SYSTEMD_SERVICE:${PN} = "summit-rcm.service"
SYSTEMD_AUTO_ENABLE = "enable"

# Cargo release profile strips the binary; skip the already-stripped QA check.
INSANE_SKIP:${PN} = "already-stripped"

# ---------------------------------------------------------------------------
# Configurable defaults (override in local.conf or a .bbappend)
# ---------------------------------------------------------------------------

USERNAME ?= "root"
PASSWORD ?= "summit"

SUMMIT_RCM_HTTPS_PORT ?= "443"
SUMMIT_RCM_SSL_CERTIFICATE ?= "/etc/summit-rcm/ssl/server.crt"
SUMMIT_RCM_SSL_PRIVATE_KEY ?= "/etc/summit-rcm/ssl/server.key"
SUMMIT_RCM_SSL_CERTIFICATE_CHAIN ?= "/etc/summit-rcm/ssl/ca.crt"

SUMMIT_RCM_SERIAL_PORT ?= "/dev/ttyS2"
SUMMIT_RCM_BAUD_RATE ?= "3000000"

MANAGED_SOFTWARE_DEVICES ?= ""
UNMANAGED_HARDWARE_DEVICES ?= ""

SUMMIT_RCM_PAIRED_CLIENT_CERT_PATH ?= ""
SUMMIT_RCM_RODATA_CA_CERT_PATH ?= ""

# ---------------------------------------------------------------------------
# Configure: point crates-io at the vendored source mirror
# ---------------------------------------------------------------------------

do_configure:append() {
    cat >> "${CARGO_HOME}/config.toml" <<- EOF

	[source.crates-io]
	replace-with = "bitbake"
EOF
}

# ---------------------------------------------------------------------------
# Install
# ---------------------------------------------------------------------------

do_install:append() {
    # Binary
    install -D -m 0755 \
        "${B}/target/${CARGO_TARGET_SUBDIR}/summit-rcm" \
        "${D}${bindir}/summit-rcm"

    # SSL development certificates
    install -d "${D}${sysconfdir}/summit-rcm/ssl"
    install -m 0644 -t "${D}${sysconfdir}/summit-rcm/ssl" \
        "${UNPACKDIR}/server.key" \
        "${UNPACKDIR}/server.crt" \
        "${UNPACKDIR}/ca.crt"

    # Systemd service unit
    if ${@bb.utils.contains('DISTRO_FEATURES', 'systemd', 'true', 'false', d)}; then
        install -D -m 0644 -t "${D}${systemd_system_unitdir}" \
            "${UNPACKDIR}/summit-rcm.service"
    fi

    # Configuration file — generated from BitBake variables and PACKAGECONFIG
    {
        echo "[global]"
        echo "server.ssl_certificate: ${SUMMIT_RCM_SSL_CERTIFICATE}"
        echo "server.ssl_private_key: ${SUMMIT_RCM_SSL_PRIVATE_KEY}"
        echo "server.ssl_certificate_chain: ${SUMMIT_RCM_SSL_CERTIFICATE_CHAIN}"
        echo ""
        echo "[/]"
        echo "tools.sessions.on: ${@bb.utils.contains('PACKAGECONFIG', 'enable-sessions', 'true', 'false', d)}"
        echo "tools.sessions.secure: true"
        echo "tools.sessions.httponly: true"
        echo ""
        echo "[summit-rcm]"
        echo "default_username: ${USERNAME}"
        echo "default_password: ${PASSWORD}"
        echo "allow_multiple_user_sessions: ${@bb.utils.contains('PACKAGECONFIG', 'allow-multiple-user-sessions', 'true', 'false', d)}"
        echo "managed_software_devices: ${MANAGED_SOFTWARE_DEVICES}"
        echo "unmanaged_hardware_devices: ${UNMANAGED_HARDWARE_DEVICES}"
        echo "enable_client_auth: ${@bb.utils.contains('PACKAGECONFIG', 'client-authentication', 'true', 'false', d)}"
        echo "disable_certificate_expiry_verification: ${@bb.utils.contains('PACKAGECONFIG', 'disable-certificate-expiry-verification', 'true', 'false', d)}"
        echo "enable_client_pairing: ${@bb.utils.contains('PACKAGECONFIG', 'client-pairing', 'true', 'false', d)}"
        echo "paired_client_cert_path: ${SUMMIT_RCM_PAIRED_CLIENT_CERT_PATH}"
        echo "rodata_ca_cert_path: ${SUMMIT_RCM_RODATA_CA_CERT_PATH}"
        echo "log_routes_loaded: ${@bb.utils.contains('PACKAGECONFIG', 'log-routes-loaded', 'true', 'false', d)}"
        echo "network_status_restricted: ${@bb.utils.contains('PACKAGECONFIG', 'restrict-network-status', 'true', 'false', d)}"
        echo "socket_port: ${SUMMIT_RCM_HTTPS_PORT}"
        echo "rest_api_docs_root_redirect: ${@bb.utils.contains('PACKAGECONFIG', 'api-docs-root-redirect', 'true', 'false', d)}"
        echo "serial_port: ${SUMMIT_RCM_SERIAL_PORT}"
        echo "baud_rate: ${SUMMIT_RCM_BAUD_RATE}"
    } > "${D}${sysconfdir}/summit-rcm.ini"
}

CONFFILES:${PN} += "${sysconfdir}/summit-rcm.ini"

FILES:${PN} += " \
    ${bindir}/summit-rcm \
    ${sysconfdir}/summit-rcm \
    ${sysconfdir}/summit-rcm.ini \
"
