SUMMARY = "Summit Remote Control Manager (RCM) Unauthenticated Plugin"
DESCRIPTION = "Add unauthenticated access support, for example, for factory \
reset and reboot endpoints. Due to security concerns, this \
should never be enabled unless requested by a customer, and \
preferably never enabled when Summit RCM is exposed on a \
public/external zone interface."

require summit-rcm.inc

S = "${WORKDIR}/git/summit_rcm/plugins/unauthenticated"

RDEPENDS:${PN} += "summit-rcm"

PACKAGECONFIG ?= "${SUMMIT_RCM_PROTOCOLS}"

PACKAGECONFIG[v2_routes] = "summit_rcm_unauthenticated/rest_api/v2/system"
PACKAGECONFIG[legacy_routes] = "summit_rcm_unauthenticated/rest_api/legacy"

export SUMMIT_RCM_UNAUTHENTICATED_PLUGIN_EXTRA_PACKAGES = "\
    summit_rcm_unauthenticated \
    summit_rcm_unauthenticated/services \
    summit_rcm_unauthenticated/middleware \
    ${PACKAGECONFIG_CONFARGS} \
    "
