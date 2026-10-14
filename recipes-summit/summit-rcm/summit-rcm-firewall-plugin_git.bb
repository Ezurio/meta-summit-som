SUMMARY = "Summit Remote Control Manager (RCM) Firewall Plugin"
DESCRIPTION = "Enable iptables firewall wrapper"

require summit-rcm.inc

S = "${UNPACKDIR}/${BP}/summit_rcm/plugins/firewall"

RDEPENDS:${PN} += "\
    summit-rcm \
    iptables \
    "

PACKAGECONFIG ?= "${SUMMIT_RCM_PROTOCOLS}"

PACKAGECONFIG[v2_routes] = "summit_rcm_firewall/rest_api/v2/network"
PACKAGECONFIG[legacy_routes] = "summit_rcm_firewall/rest_api/legacy"

export SUMMIT_RCM_FIREWALL_PLUGIN_EXTRA_PACKAGES = "\
    summit_rcm_firewall \
    summit_rcm_firewall/services \
    ${PACKAGECONFIG_CONFARGS} \
    "
