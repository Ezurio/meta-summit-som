SUMMARY = "Summit Remote Control Manager (RCM) Log Forwarding Plugin"
DESCRIPTION = "Enable log forwarding support for Summit-RCM"

require summit-rcm.inc

S = "${UNPACKDIR}/${BP}/summit_rcm/plugins/log-forwarding"

RDEPENDS:${PN} += "\
    summit-rcm \
    systemd-journal-gatewayd \
    "

PACKAGECONFIG ?= "${SUMMIT_RCM_PROTOCOLS}"

PACKAGECONFIG[v2_routes] = "summit_rcm_log_forwarding/rest_api/v2/system"
PACKAGECONFIG[legacy_routes] = "summit_rcm_log_forwarding/rest_api/legacy"
PACKAGECONFIG[at_interface] = "summit_rcm_log_forwarding/at_interface/commands"

export SUMMIT_RCM_LOG_FORWARDING_PLUGIN_EXTRA_PACKAGES = "\
    summit_rcm_log_forwarding \
    summit_rcm_log_forwarding/services \
    ${PACKAGECONFIG_CONFARGS} \
    "
