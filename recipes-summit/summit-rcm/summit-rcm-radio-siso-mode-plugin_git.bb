SUMMARY = "Summit Remote Control Manager (RCM) Radio SISO Mode Plugin"
DESCRIPTION = "Enable support for temporarily changing the radio's SISO mode (for testing purposes only)"

require summit-rcm.inc

S = "${UNPACKDIR}/${BP}/summit_rcm/plugins/radio-siso-mode"

RDEPENDS:${PN} += "summit-rcm"

PACKAGECONFIG ?= "${SUMMIT_RCM_PROTOCOLS}"

PACKAGECONFIG[v2_routes] = "summit_rcm_radio_siso_mode/rest_api/v2/network"
PACKAGECONFIG[legacy_routes] = "summit_rcm_radio_siso_mode/rest_api/legacy"
PACKAGECONFIG[at_interface] = "summit_rcm_radio_siso_mode/at_interface/commands"

export SUMMIT_RCM_RADIO_SISO_MODE_PLUGIN_EXTRA_PACKAGES = "\
    summit_rcm_radio_siso_mode \
    summit_rcm_radio_siso_mode/services \
    ${PACKAGECONFIG_CONFARGS} \
    "
