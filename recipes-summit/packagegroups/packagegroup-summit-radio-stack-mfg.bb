SUMMARY = "Summit SOM Radio Stack Mfg"
SECTION = "net/misc"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit packagegroup

SUMMIT_SOM_RADIO_TYPE ?= "none"

PACKAGECONFIG ?= "${SUMMIT_SOM_RADIO_TYPE}"

PACKAGECONFIG[none] = ""
PACKAGECONFIG[60-sdio-uart] = ",,,60-radio-firmware-sdio-uart ${RADIO_MFG_60_PACKAGES}"
PACKAGECONFIG[60-sdio-sdio] = ",,,60-radio-firmware-sdio-sdio ${RADIO_MFG_60_PACKAGES}"
PACKAGECONFIG[60-som8mp] = ",,,som8mp-radio-firmware ${RADIO_MFG_60_PACKAGES} ${RADIO_MFG_60_ADAPTIVE}"
PACKAGECONFIG[if513-div] = ",,,if513-sdio-div-firmware summit-regif513 ${RADIO_MFG_IFX_PACKAGES}"
PACKAGECONFIG[if513-sa] = ",,,if513-sdio-sa-firmware summit-regif513 ${RADIO_MFG_IFX_PACKAGES}"
PACKAGECONFIG[if573] = ",,,if573-sdio-firmware summit-regif573 ${RADIO_MFG_IFX_PACKAGES}"
PACKAGECONFIG[nx61x-1216] = ",,,nx61x-firmware-1216-serdev ${RADIO_MFG_NX_PACKAGES}"
PACKAGECONFIG[nx61x-1218] = ",,,nx61x-firmware-1218-serdev ${RADIO_MFG_NX_PACKAGES}"
PACKAGECONFIG[ti351] = ",,,ti351-firmware ${RADIO_MFG_TI_PACKAGES}"
PACKAGECONFIG[dvk-combo] = ",,,${RADIO_MFG_COMBO_PACKAGES}"

RADIO_MFG_COMMON_PACKAGES = " \
    summit-supplicant \
    summit-supplicant-cli \
    summit-networkmanager \
    summit-networkmanager-wifi \
    summit-networkmanager-nmcli \
    "

RADIO_MFG_60_ADAPTIVE = " \
    summit-adaptive-ww \
    ${@bb.utils.contains('COMBINED_FEATURES', 'bluetooth', 'summit-adaptive-bt summit-bt-uart-scripts-60', '', d)} \
    "

RADIO_MFG_60_PACKAGES = "\
    kernel-module-60-backports \
    summit-mfg60n \
    ${RADIO_MFG_COMMON_PACKAGES} \
    "

RADIO_MFG_IFX_PACKAGES = " \
    kernel-module-lwb-if-backports \
    ${RADIO_MFG_COMMON_PACKAGES} \
    "

RADIO_MFG_NX_PACKAGES = " \
    kernel-module-nx-backports \
    summit-mfg611 \
    ${RADIO_MFG_COMMON_PACKAGES} \
    "

RADIO_MFG_TI_PACKAGES = " \
    kernel-module-ti-backports \
    summit-regti351 \
    ${RADIO_MFG_COMMON_PACKAGES} \
    "

RADIO_MFG_COMBO_PACKAGES = " \
    kernel-module-combo-backports \
    if513-sdio-div-firmware \
    if513-sdio-sa-firmware \
    if573-sdio-firmware \
    nx61x-firmware-1216-serdev \
    nx61x-firmware-1218-serdev \
    ti351-firmware \
    summit-mfg611 \
    summit-regif513 \
    summit-regif573-firmware \
    summit-regti351 \
    ${RADIO_MFG_COMMON_PACKAGES} \
    "

RADIO_MFG_COMBO_PACKAGES:append:am62xx = "\
    60-radio-firmware-sdio-uart \
    60-radio-firmware-sdio-sdio \
    summit-mfg60n \
    ${RADIO_MFG_60_ADAPTIVE} \
    "
