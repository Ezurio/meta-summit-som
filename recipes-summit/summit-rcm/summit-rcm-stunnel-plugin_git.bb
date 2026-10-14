SUMMARY = "Summit Remote Control Manager (RCM) Stunnel Plugin"
DESCRIPTION = "Enable support to control stunnel"

require summit-rcm.inc

S = "${UNPACKDIR}/${BP}/summit_rcm/plugins/stunnel"

RDEPENDS:${PN} += "\
    summit-rcm \
    stunnel \
    "

PACKAGECONFIG ?= "${SUMMIT_RCM_PROTOCOLS}"

PACKAGECONFIG[v2_routes] = "summit_rcm_stunnel/rest_api/v2/network"
PACKAGECONFIG[legacy_routes] = "summit_rcm_stunnel/rest_api/legacy"

export SUMMIT_RCM_STUNNEL_PLUGIN_EXTRA_PACKAGES = "\
    summit_rcm_stunnel \
    summit_rcm_stunnel/services \
    ${PACKAGECONFIG_CONFARGS} \
    "
