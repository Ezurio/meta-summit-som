SUMMARY = "Summit SOM Radio Stack SOM Selected"
SECTION = "net/misc"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit packagegroup

SUMMIT_SOM_RADIO_TYPE ?= "none"

PACKAGECONFIG ?= "${SUMMIT_SOM_RADIO_TYPE}"

PACKAGECONFIG[none] = ""
PACKAGECONFIG[60-sdio-uart] = ",,,60-radio-firmware-sdio-uart ${RADIO_60_PACKAGES}"
PACKAGECONFIG[60-sdio-sdio] = ",,,60-radio-firmware-sdio-sdio ${RADIO_60_PACKAGES}"
PACKAGECONFIG[60-som8mp] = ",,,som8mp-radio-firmware ${RADIO_60_PACKAGES}"
PACKAGECONFIG[if513-div] = ",,,if513-sdio-div-firmware ${RADIO_IFX_PACKAGES}"
PACKAGECONFIG[if513-sa] = ",,,if513-sdio-sa-firmware ${RADIO_IFX_PACKAGES}"
PACKAGECONFIG[if573] = ",,,if573-sdio-firmware ${RADIO_IFX_PACKAGES}"
PACKAGECONFIG[nx61x-1216] = ",,,nx61x-firmware-1216-serdev ${RADIO_NX_PACKAGES}"
PACKAGECONFIG[nx61x-1218] = ",,,nx61x-firmware-1218-serdev ${RADIO_NX_PACKAGES}"
PACKAGECONFIG[ti351] = ",,,ti351-firmware ${RADIO_TI_PACKAGES}"
PACKAGECONFIG[dvk-combo] = ",,,${RADIO_COMBO_PACKAGES}"

RADIO_COMMON_PACKAGES = " \
    summit-supplicant \
    summit-supplicant-cli \
    summit-networkmanager \
    summit-networkmanager-wifi \
    summit-networkmanager-nmcli \
    summit-hostapd \
    "

RADIO_60_PACKAGES = "\
    kernel-module-60-backports \
    summit-adaptive-ww \
    ${@bb.utils.contains('COMBINED_FEATURES', 'bluetooth', 'summit-adaptive-bt summit-bt-uart-scripts-60', '', d)} \
    ${RADIO_COMMON_PACKAGES} \
    "

RADIO_COMBO_PACKAGES = " \
    kernel-module-combo-backports \
    60-radio-firmware-sdio-uart \
    60-radio-firmware-sdio-sdio \
    ${@bb.utils.contains('MACHINE_FEATURES', 'usbhost', '60-radio-firmware-usb-usb', '', d)} \
    ${@bb.utils.contains('MACHINE_FEATURES', 'pci', '60-radio-firmware-pcie-uart', '', d)} \
    lwb5plus-sdio-sa-firmware \
    lwb5plus-sdio-div-firmware \
    ${@bb.utils.contains('MACHINE_FEATURES', 'usbhost', 'lwb5plus-usb-div-firmware', '', d)} \
    ${@bb.utils.contains('MACHINE_FEATURES', 'usbhost', 'lwb5plus-usb-sa-firmware', '', d)} \
    if513-sdio-div-firmware \
    if513-sdio-sa-firmware \
    if573-sdio-firmware \
    ${@bb.utils.contains('MACHINE_FEATURES', 'pci', 'if573-pcie-firmware', '', d)} \
    nx61x-firmware-1216-serdev \
    nx61x-firmware-1218-serdev \
    ti351-firmware \
    summit-adaptive-ww \
    ${@bb.utils.contains('COMBINED_FEATURES', 'bluetooth', 'summit-adaptive-bt summit-bt-uart-scripts-60', '', d)} \
    ${RADIO_COMMON_PACKAGES} \
    "

RADIO_IFX_PACKAGES = " \
    kernel-module-lwb-if-backports \
    ${RADIO_COMMON_PACKAGES} \
    "

RADIO_NX_PACKAGES = " \
    kernel-module-nx-backports \
    ${RADIO_COMMON_PACKAGES} \
    "

RADIO_TI_PACKAGES = " \
    kernel-module-ti-backports \
    ${RADIO_COMMON_PACKAGES} \
    "
