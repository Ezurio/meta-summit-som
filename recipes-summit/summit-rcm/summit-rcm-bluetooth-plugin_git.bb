SUMMARY = "Summit Remote Control Manager (RCM) Bluetooth Plugin"
DESCRIPTION = "Bluetooth plugin for Summit RCM"

require summit-rcm.inc

RDEPENDS:${PN} += "summit-rcm"

S = "${UNPACKDIR}/${BP}/summit_rcm/plugins/bluetooth"

PACKAGECONFIG ?= "${SUMMIT_RCM_PROTOCOLS}"

PACKAGECONFIG[hid] = "summit_rcm_bluetooth/hid,,,${PYTHON_PN}-pyudev"
PACKAGECONFIG[vsp] = "summit_rcm_bluetooth/vsp"
PACKAGECONFIG[v2_routes] = "summit_rcm_bluetooth/rest_api/v2/bluetooth"
PACKAGECONFIG[legacy_routes] = "summit_rcm_bluetooth/rest_api/legacy"

export SUMMIT_RCM_BLUETOOTH_PLUGIN_EXTRA_PACKAGES = "\
    summit_rcm_bluetooth \
    summit_rcm_bluetooth/services \
    ${PACKAGECONFIG_CONFARGS} \
    "
