FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

TFA_UBOOT:summitsom-genio = "0"

# Recipe-scoped: makes MTK TF-A build BL2 with TRUSTED_BOARD_BOOT; must match the FIP recipe.
DISTRO_FEATURES:append:summit-secure = " secure-boot"
DISTRO_FEATURES:append:summitsom-genio = " ${@' secure-boot' if d.getVar('SECURE_BOOT') == '1' else ''}"
# Download agent: BL2 only starts the SPL, which the BootROM already authenticated, so no TBBR.
DISTRO_FEATURES:remove:summitsom-usb-boot = "secure-boot"

SRC_URI:append:summitsom = " \
	file://0001-mt8188-summit-boot-partition-storage.patch \
	file://0002-mt8188-summit-pwrap-log-level.patch \
	file://0003-mt8188-summit-quiet-libdram-log.patch \
	file://0004-mt8188-summit-boot-flow.patch \
"

# ARMv8.2 (A78/A55) enables the CRC instructions that tf_crc32.c needs; the recipe strips the tune's -march.
EXTRA_OEMAKE:append:summitsom = " PLAT_AB_BOOT_ENABLE=0 ARM_ARCH_MINOR=2 UART_BAUDRATE=115200"

# ram-boot: download agent, BL2 copies the SPL appended after it into DRAM and enters it.
PACKAGECONFIG[ram-boot] = "SUMMIT_RAM_BOOT=1"
PACKAGECONFIG:append:summitsom-usb-boot = " ram-boot"

# The unsigned BL2 is the DDR loader of the download-agent payload, which U-Boot signs together with SPL.
do_deploy:append:summitsom-usb-boot() {
    install -m 0644 ${B}/${TFA_PLATFORM}/release/bl2.bin ${DEPLOYDIR}/bl2-raw.bin
}

# The FIP follows BL2 at FIP_OFFSET (bl2_plat_handle_pre_image_load() in the BL2 patch).
do_deploy:append:summitsom-genio() {
    [ $(stat -c %s ${DEPLOYDIR}/bl2.img) -le ${FIP_OFFSET} ] || bbfatal "bl2.img exceeds FIP offset ${FIP_OFFSET}"
}

# The MTK include forces do_deploy[nostamp]; its per-parse taint rebuilds U-Boot and the rootfs on every build.
python () {
    d.delVarFlag('do_deploy', 'nostamp')
}
