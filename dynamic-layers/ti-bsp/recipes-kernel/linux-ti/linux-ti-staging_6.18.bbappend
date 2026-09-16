FILESEXTRAPATHS:prepend:summitsom := "${THISDIR}/${PN}-6.18:"

SRC_URI:append:summitsom = " \
    file://0001-ts3a227-jack.patch \
    file://0002-ts3a227-irq.patch \
    file://0004-simple_card_ts3a227.patch \
    file://0006-dp83867.patch \
    file://0008-hwmon-ina2xx-add-support-for-ina232.patch \
    file://0009-dt-bindings-hwmon-ina2xx-add-ina232.patch \
    file://0011-lvds-codec-output-bridge.patch \
    file://0012-lvds-codec-frequency-limit.patch \
    file://0019-dm-verity-partition-wait-fix.patch \
    file://0021-tps6287x-regulator.patch \
    file://0026-dp83867-irq.patch \
    file://0027-davinci-mdio-missing-cpu.patch \
    file://0046-gpio-keys-resume-error.patch \
    file://0048-media-ov5640-digient-rc-delay-compensation.patch \
    file://0052-goodix-fix-lost-irqs.patch \
    file://0054-rv3028-fix-eeprom-device-tree-support.patch \
    file://0056-mcp23s08-allow-edge-trigger.patch \
    file://0056-media-i2c-ov5645-Report-streams-using-frame-descript.patch \
    file://0057-cpsw-probe.patch \
    file://0058-drm-bridge-Add-simple-format-bridge-driver.patch \
    file://0059-gpio-Add-GPIO-fanout-driver.patch \
    file://0068-pinctrl-mcp23s08-add-wakeup-support.patch \
    file://0069-rtc-rv3028-skip-eeprom-update-if-value-unchanged.patch \
    file://0070-rtc-rv3028-add-backup-switchover-dsm-dt-property.patch \
    file://0071-fs_crypt-accept-trusted-keys-via-key_id.patch \
    file://0072-fs_crypt-dm_crypt-Restored-use-of-crypto-engines.patch \
    file://0073-Lontium-LT9611C-UXD-UXE-LT2611UXD-UXE.patch \
    file://0074-cpsw-keep-ale-in-nuss-module.patch \
    file://0075-usb-conn-gpio-fix-resume-reconnect-race.patch \
    file://0076-dwc3-am62-fix-host-mode-linestate-wake-race.patch \
    file://0077-usb-dwc3-run-gadget-disconnect-from-sleepable-suspen.patch \
    file://0078-usb-dwc3-gadget-Fix-use-after-free-in-dwc3_gadget_fr.patch \
    file://0079-usb-dwc3-clear-forceRM-when-issuing-EndTransfer.patch \
    file://dts \
    "

KERNEL_DTC_FLAGS:append:summitsom = " ${@' -@' if d.getVar('KERNEL_DEVICETREE').find('.dtbo') else ''}"

KERNEL_CONFIG_SUMMIT:am62xx = "k3-am625-carbon_defconfig"
KERNEL_CONFIG_SUMMIT:am62lxx = "k3-am62l-carbon_defconfig"
KERNEL_CONFIG_SUMMIT:j722s  = "k3-am675-carbon_defconfig"
KERNEL_CONFIG_SUMMIT ?= ""

KERNEL_DTBVENDORED:summitsom = "0"
KERNEL_DEFCONFIG:summitsom = "file://${KERNEL_CONFIG_SUMMIT}"

# Remove kernel binary from rootfs
RRECOMMENDS:${KERNEL_PACKAGE_NAME}-base:summitsom = ""

python do_patch:append:summitsom () {
    bb.build.exec_func('do_copy_dts', d)
}

do_copy_dts () {
    cp -af -t "${S}/arch/arm64/boot" "${UNPACKDIR}/dts"
}

do_configure:prepend:summitsom() {
    cp -f "${UNPACKDIR}/${KERNEL_CONFIG_SUMMIT}" "${UNPACKDIR}/defconfig"
}
