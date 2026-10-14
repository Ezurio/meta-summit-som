SUMMARY = "Summit Remote Control Manager (RCM) Chrony Plugin"
DESCRIPTION = "Enable chrony wrapper for NTP configuration"

require summit-rcm.inc

S = "${UNPACKDIR}/${BP}/summit_rcm/plugins/chrony"

RDEPENDS:${PN} += "\
    summit-rcm \
    chrony \
    "

PACKAGECONFIG ?= "${SUMMIT_RCM_PROTOCOLS}"

PACKAGECONFIG[v2_routes] = "summit_rcm_chrony/rest_api/v2/system"
PACKAGECONFIG[legacy_routes] = "summit_rcm_chrony/rest_api/legacy"
PACKAGECONFIG[at_interface] = "summit_rcm_chrony/at_interface/commands"

export SUMMIT_RCM_CHRONY_PLUGIN_EXTRA_PACKAGES = "\
    summit_rcm_chrony \
    summit_rcm_chrony/services \
    ${PACKAGECONFIG_CONFARGS} \
    "
