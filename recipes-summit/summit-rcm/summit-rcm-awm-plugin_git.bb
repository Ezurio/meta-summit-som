SUMMARY = "Summit Remote Control Manager (RCM) Adaptive Worldwide Mode (AWM) Plugin"
DESCRIPTION = "AWM (Adaptive Worldwide Mode) configuration support to Summit RCM."

require summit-rcm.inc

S = "${UNPACKDIR}/${BP}/summit_rcm/plugins/awm"

RDEPENDS:${PN} += "summit-rcm"

ADAPTIVE_WW_CFG_FILE ?= ""

PACKAGECONFIG ?= "${SUMMIT_RCM_PROTOCOLS}"

PACKAGECONFIG[v2_routes] = "summit_rcm_awm/rest_api/v2/network"
PACKAGECONFIG[legacy_routes] = "summit_rcm_awm/rest_api/legacy"
PACKAGECONFIG[at_interface] = "summit_rcm_awm/at_interface/commands"

export SUMMIT_RCM_AWM_PLUGIN_EXTRA_PACKAGES = "\
    summit_rcm_awm \
    summit_rcm_awm/services \
    ${PACKAGECONFIG_CONFARGS} \
    "

do_install:append() {
    mkdir -p ${D}${sysconfdir}
    echo "[summit-rcm]\nawm_cfg: \"${ADAPTIVE_WW_CFG_FILE}\"" > "${D}${sysconfdir}/summit-rcm-awm.ini"
}
