FILESEXTRAPATHS:prepend:summitsom := "${THISDIR}/${PN}-6.12:"

SRC_URI:append:summitsom = " \
    file://0001-ts3a227-jack.patch \
    file://0002-ts3a227-irq.patch \
    file://0004-simple_card_ts3a227.patch \
    file://0006-dp83867.patch \
    file://0007-gpio-pca953x-pullup.patch \
    file://0008-hwmon-ina2xx-add-support-for-ina232.patch \
    file://0009-dt-bindings-hwmon-ina2xx-add-ina232.patch \
    file://0011-lvds-codec-output-bridge.patch \
    file://0012-lvds-codec-frequency-limit.patch \
    file://0019-dm-verity-partition-wait-fix.patch \
    file://0020-gpio-pca953x-Add-support-for-TI-TCA6418-GPIO-chip.patch \
    file://0021-tps6287x-regulator.patch \
    file://0023-am67-add-peripherals.patch \
    file://0026-dp83867-irq.patch \
    file://0027-davinci-mdio-missing-cpu.patch \
    file://0030-tps65219-trap.patch \
    file://0040-k3-am62p-j722s-remove-duplicate-dphy0.patch \
    file://0045-mcan-suspend-crash.patch \
    file://0046-gpio-keys-resume-error.patch \
    file://0047-media-ov5645-Add-support-for-streams.patch \
    file://0048-media-ov5640-digient-rc-delay-compensation.patch \
    file://0049-gpio-pca953x-Add-support-for-level-triggered-interru.patch \
    file://0050-gpio-pca953x-use-regmap_update_bits-to-improve-perfo.patch \
    file://0051-Input-goodix-add-support-for-polling-mode.patch \
    file://0052-goodix-fix-lost-irqs.patch \
    file://0053-loadpin-Fixed-auto-enable-config.patch \
    file://0054-rv3028-fix-eeprom-device-tree-support.patch \
    file://0055-rtc-rv3028-fix-name-collisions.patch \
    file://0056-mcp23s08-allow-edge-trigger.patch \
    file://0057-cpsw-probe.patch \
    file://0058-drm-bridge-Add-simple-format-bridge-driver.patch \
    file://0059-gpio-Add-GPIO-fanout-driver.patch \
    file://0060-arm64-dts-ti-k3-j722s-main-Add-audio-refclk0-node.patch \
    file://0061-arm64-dts-ti-k3-j722s-main-fix-the-audio-refclk-sour.patch \
    file://0062-PENDING-mmc-sdhci_am654-AM62-Limit-DDR52-mode-to-40M.patch \
    file://0063-PENDING-mmc-sdhci_am654-Fix-DLL-FRQSEL.patch \
    file://0064-PENDING-mmc-sdhci_am654-Enable-DLL-based-on-mmc-actu.patch \
    file://0065-PENDING-mmc-sdhci_am654-Allow-defaults-for-DLL-prope.patch \
    file://0066-PENDING-mmc-sdhci_am654-Add-SDHCI_QUIRK_CAP_CLOCK_BA.patch \
    file://dts \
    "

KERNEL_DTC_FLAGS:append:summitsom = " ${@' -@' if d.getVar('KERNEL_DEVICETREE').find('.dtbo') else ''}"

KERNEL_CONFIG_SUMMIT:am62xx = "k3-am625-carbon_defconfig"
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
    cp -af -t "${S}/arch/arm64/boot" "${WORKDIR}/dts"
}

do_configure:prepend:summitsom() {
    cp -f "${WORKDIR}/${KERNEL_CONFIG_SUMMIT}" "${WORKDIR}/defconfig"
}
