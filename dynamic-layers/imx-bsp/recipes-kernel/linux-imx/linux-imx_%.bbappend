FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_URI:append:summitsom = " \
    file://0001-ts3a227-jack.patch \
    file://0006-sound_ts3a227_support.patch \
    file://0004-simple_card_ts3a227.patch \
    file://0011-lvds-codec-output-bridge.patch \
    file://0012-lvds-codec-frequency-limit.patch \
    file://0008-fsl_aud2htx-probe.patch \
    file://0010-imx-sdma.patch \
    file://0011-fsl-sai.patch \
    file://0012-fsl-easrc.patch \
    file://0013-lcdifv3.patch \
    file://0015-imx8mp-hdmi-pavi.patch \
    file://0019-dm-verity-partition-wait-fix.patch \
    file://0021-sn65dsi83-nxp-bridge-compat.patch \
    file://0022-dw_mipi_dsi-imx-eprobe-defer.patch \
    file://0048-media-ov5640-digient-rc-delay-compensation.patch \
    file://0054-rtc-rv3028-fix-eeprom-device-tree-support.patch \
    file://0055-rtc-rv3028-fix-name-collisions.patch \
    file://0056-media-i2c-ov5645-Report-streams-using-frame-descript.patch \
    file://0057-media-nxp-dwc-mipi-csi2.patch \
    file://0058-sn65dsi83-ignore-pll-lock-failure.patch \
    file://0059-imx-mipi-csis-enable-camera-link.patch \
    file://0060-sec_mipi_dsim-imx-probe.patch \
    file://0061-mxc-viv-disable-vg-fix.patch \
    file://0062-gpio-Add-GPIO-fanout-driver.patch \
    file://0063-phy-freescale-imx8mq-usb-set-vbus-depending-on-mode.patch \
    file://0064-media-imx-isi-cap-add-pivariety-workaround.patch \
    file://0065-media-imx-isi-cap-fix-subdev-stream-default-value.patch \
    file://0068-pinctrl-mcp23s08-add-wakeup-support.patch \
    file://0069-fs_crypt-dm_crypt-Restored-use-of-crypto-engines.patch \
    file://0070-imx93-clk-fracn-gppll-add-504mhz-rate.patch \
    file://0071-imx93-dsi-remove-clock-whitelist.patch \
    file://0072-media-imx8-mipi-csi2-sam-fix-s_power-null-deref.patch \
    file://0073-media-imx8-isi-cap-tolerate-optional-get_frame_inte.patch \
    file://0074-media-imx8-mipi-csi2-sam-quiet-phy-reset-no-sensor.patch \
    file://0075-media-imx7-media-csi-validate-pixelformat-against-s.patch \
    file://0076-media-imx7-media-csi-hold-a-cpu-latency-qos-request.patch \
    file://0077-rtc-rv3028-skip-eeprom-update-if-value-unchanged.patch \
    file://0078-rtc-rv3028-add-backup-switchover-dsm-dt-property.patch \
    file://0079-fs_crypt-accept-trusted-keys-via-key_id.patch \
    file://0080-media-nxp-dwc-mipi-csi2-fix-clock-lane-stopstate.patch \
    file://dts \
    ${KERNEL_CONFIG_SUMMIT} \
    "

SRC_URI:append:imx8mp-summitsom = " \
    file://0004-Introduce-the-BQ25790-charger-driver.patch \
    "

SRC_URI:append:summitsom-rescue = " \
    file://kernel-rescue.cfg \
    "

SRC_URI:append:summitsom-rescue-initramfs = " \
    file://kernel-rescue.cfg \
    file://kernel-initramfs.cfg \
    "

KERNEL_DTC_FLAGS:append:summitsom = " ${@' -@' if d.getVar('KERNEL_DEVICETREE').find('.dtbo') else ''}"

KERNEL_CONFIG_SUMMIT:mx8mm-generic-bsp = "file://nitrogen_imx8mm_defconfig"
KERNEL_CONFIG_SUMMIT:mx8mp-generic-bsp = "file://nitrogen_imx8mp_defconfig"
KERNEL_CONFIG_SUMMIT:mx91-generic-bsp = "file://nitrogen_imx91_defconfig"
KERNEL_CONFIG_SUMMIT:mx93-generic-bsp = "file://nitrogen_imx93_defconfig"
KERNEL_CONFIG_SUMMIT:mx95-generic-bsp = "file://nitrogen_imx95_defconfig"
KERNEL_CONFIG_SUMMIT:imx8mp-summitsom = "file://summitsom_defconfig"
KERNEL_CONFIG_SUMMIT ?= ""

LOCALVERSION:summitsom = ""
SCMVERSION:summitsom = "n"

# Use our defconfig
KBUILD_DEFCONFIG:summitsom = ""

# Remove kernel binary from rootfs
RRECOMMENDS:${KERNEL_PACKAGE_NAME}-base:summitsom = ""

python __anonymous() {
    if 'summitsom' in d.getVar('OVERRIDES', True).split(':'):
        d.setVarFlag('do_copy_defconfig', 'noexec', '1')
}

do_patch:append:summitsom () {
    cp -af -t "${S}/arch/arm64/boot" "${UNPACKDIR}/dts"
}

# Build SDMA firmware into kernel
DEPENDS:append:summitsom:mx8m-generic-bsp = " firmware-imx"
do_compile:prepend:summitsom:mx8m-generic-bsp () {
    cp -af -t "${S}" "${STAGING_LIBDIR}/firmware"
}
