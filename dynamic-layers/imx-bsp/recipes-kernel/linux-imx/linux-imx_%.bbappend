FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_URI:append:summitsom = " \
    file://0001-ts3a227-jack.patch \
    file://0006-sound_ts3a227_support.patch \
    file://0008-fsl_aud2htx-probe.patch \
    file://0010-imx-sdma.patch \
    file://0011-fsl-sai.patch \
    file://0012-fsl-easrc.patch \
    file://0013-lcdifv3.patch \
    file://0014-phy-fsl-samsung-hdmi.patch \
    file://0015-imx8mp-hdmi-pavi.patch \
    file://0019-dm-verity-partition-wait-fix.patch \
    file://dts \
    ${KERNEL_CONFIG_SUMMIT} \
    "

SRC_URI:append:imx8mp-summitsom = " \
    file://0003-gpio-add-gpio_of_helper.patch \
    file://0004-Introduce-the-BQ25790-charger-driver.patch \
    "

KERNEL_DTC_FLAGS:append:summitsom = " ${@' -@' if d.getVar('KERNEL_DEVICETREE').find('.dtbo') else ''}"

KERNEL_CONFIG_SUMMIT:mx8mp-generic-bsp = "file://summitsom_defconfig"
KERNEL_CONFIG_SUMMIT ?= ""

LOCALVERSION:summitsom = ""
SCMVERSION:summitsom = "n"

# Use our defconfig
IMX_KERNEL_CONFIG_AARCH64:summitsom = ""

# Remove kernel binary from rootfs
RRECOMMENDS:${KERNEL_PACKAGE_NAME}-base:summitsom = ""

python __anonymous() {
    if 'summitsom' in d.getVar('OVERRIDES', True).split(':'):
        d.setVarFlag('do_copy_defconfig', 'noexec', '1')
}

do_patch:append:summitsom () {
    cp -af -t "${S}/arch/arm64/boot" "${WORKDIR}/dts"
}

# Build SDMA firmware into kernel
DEPENDS:append:summitsom:mx8m-generic-bsp = " firmware-imx"
do_compile:prepend:summitsom:mx8m-generic-bsp () {
    cp -af -t "${S}" "${STAGING_LIBDIR}/firmware"
}
