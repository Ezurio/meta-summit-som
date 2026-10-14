SUMMARY = "Summit Remote Control Manager (RCM) Certificate Provisioning Plugin"
DESCRIPTION = "Enable certificate provisioning support for Summit-RCM"

require summit-rcm.inc

S = "${UNPACKDIR}/${BP}/summit_rcm/plugins/provisioning"

SRC_URI:append = "\
    file://ca.crt \
    file://server.crt \
    file://server.key \
    "

RDEPENDS:${PN} += "\
    summit-rcm \
    summit-initdata \
    "

PACKAGECONFIG ?= "${SUMMIT_RCM_PROTOCOLS}"

PACKAGECONFIG[v2_routes] = "summit_rcm_provisioning/rest_api/v2/system"
PACKAGECONFIG[legacy_routes] = "summit_rcm_provisioning/rest_api/legacy"

export SUMMIT_RCM_CERTIFICATE_PROVISIONING_PLUGIN_EXTRA_PACKAGES = "\
    summit_rcm_provisioning \
    summit_rcm_provisioning/services \
    summit_rcm_provisioning/middleware \
    ${PACKAGECONFIG_CONFARGS} \
    "

do_install:append() {
    install -D -m 644 "${UNPACKDIR}/server.crt" "${D}${sysconfdir}/summit-rcm/ssl/provisioning.crt"
    install -D -m 644 "${UNPACKDIR}/server.key" "${D}${sysconfdir}/summit-rcm/ssl/provisioning.key"
    install -D -m 644 "${UNPACKDIR}/ca.crt" "${D}${sysconfdir}/summit-rcm/ssl/provisioning.ca.crt"
}

do_install:append:summit-secure () {
    ln -sf /data/secret/fallback_timestamp "${D}${sysconfdir}/fallback_timestamp"

    mkdir -p "${D}${datadir}/factory/etc/secret/permanent/provisioning"
    ln -sf /data/secret/permanent/provisioning "${D}${sysconfdir}/summit-rcm/provisioning"
}

FILES:${PN} += "\
    ${datadir}/factory/etc/secret/permanent/provisioning \
    "
