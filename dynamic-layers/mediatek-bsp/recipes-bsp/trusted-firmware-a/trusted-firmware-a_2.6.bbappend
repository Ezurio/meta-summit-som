FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_URI:append:summitsom = " file://0001-mt8188-summit-validate-aux-params.patch"

# eMMC boot: TF-A builds the FIP (and TBBR certificates with secure-boot) around U-Boot's BL33.
# The USB download agent keeps U-Boot binman and only needs bl31.bin.
TFA_UBOOT:summitsom-usb-boot = "0"
TFA_BUILD_TARGET:summitsom-usb-boot = "bl31"
TFA_INSTALL_TARGET:summitsom-usb-boot = "bl31"

# Recipe-scoped: makes MTK TF-A generate the TBBR certificates; must match the BL2 recipe.
DISTRO_FEATURES:append:summit-secure = " secure-boot"
DISTRO_FEATURES:append:mtk:summitsom-genio = " ${@' secure-boot' if d.getVar('SECURE_BOOT') == '1' else ''}"
# Download agent: no FIP; BL2 and SPL are authenticated by the BootROM and the rest by the FIT signature.
DISTRO_FEATURES:remove:summitsom-usb-boot = "secure-boot"

EXTRA_OEMAKE:append:summitsom-genio = " FIP_ALIGN=4096 UART_BAUDRATE=115200"
# Summit boots the headerless pager image, not MTK's default tee.bin.
EXTRA_OEMAKE:append:summitsom-genio = "${@bb.utils.contains('MACHINE_FEATURES', 'optee', ' BL32=${STAGING_DIR_TARGET}/${nonarch_base_libdir}/firmware/tee-pager_v2.bin', '', d)}"

# The BL2 patch reads at most 0x200000 bytes of FIP.
do_deploy:append:summitsom-genio() {
    if [ "${@bb.utils.contains('TFA_BUILD_TARGET', 'fip', '1', '', d)}" = "1" ]; then
        [ $(stat -c %s ${DEPLOYDIR}/fip.bin) -le 2097152 ] || bbfatal "fip.bin exceeds 0x200000"
    fi
}