FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_URI:append:summitsom = " \
    file://0011-lvds-codec-output-bridge.patch \ 
    file://0012-lvds-codec-frequency-limit.patch \ 
    file://0019-dm-verity-partition-wait-fix.patch \ 
    file://0047-media-ov5645-Add-support-for-streams.patch \ 
    file://0048-media-ov5640-digient-rc-delay-compensation.patch \ 
    file://0054-rtc-rv3028-fix-eeprom-device-tree-support.patch \ 
    file://0055-rtc-rv3028-fix-name-collisions.patch \ 
    file://0056-media-i2c-ov5645-Report-streams-using-frame-descript.patch \ 
    file://0058-sn65dsi83-ignore-pll-lock-failure.patch \ 
    file://0077-rtc-rv3028-skip-eeprom-update-if-value-unchanged.patch \ 
    file://0078-rtc-rv3028-add-backup-switchover-dsm-dt-property.patch \ 
    file://0079-fs_crypt-accept-trusted-keys-via-key_id.patch \ 
    file://0081-Lontium-LT9611C-UXD-UXE-LT2611UXD-UXE.patch \
    file://0086-usb-gadget-f_fs-add-capability-for-dfu-functional-descriptor.patch \
    file://0087-ASoC-mt8188-mt6359-headphone-switch-index.patch \
    file://0088-usb-mtu3-otg-device-role-first.patch \
    file://0089-drm-atomic-helper-drm_atomic_helper_shutdown-NULL.patch \
    file://0090-drm-mediatek-drm-do-not-crash-in-shutdown-if-not-bound.patch \
    file://0091-media-v4l2-subdev-support-implicit-stream-0.patch \
    file://0092-media-v4l2-subdev-s-stream-helper-non-routing.patch \
    file://0093-media-i2c-ov5645-Remove-software-reset-entry-from-ov.patch \
    file://0094-media-i2c-ov5645-report-frame-interval.patch \
    file://0095-drm-mediatek-use-dev_err_probe-for-RDMA.patch \
    ${@bb.utils.contains('MACHINE_FEATURES', 'optee', '', 'file://no-optee.cfg', d)} \
    ${KERNEL_CONFIG_SUMMIT} \
    "

SRC_URI:append:summitsom-initramfs = " \
    file://kernel-initramfs.cfg \
    "

# DTBs and overlays are built by summit-device-trees (virtual/dtb)
KERNEL_DEVICETREE:summitsom = ""

# Remove kernel binary from rootfs
RRECOMMENDS:${KERNEL_PACKAGE_NAME}-base:summitsom = ""

KERNEL_CONFIG_SUMMIT = "file://tungsten_defconfig"

# Use our defconfig
KBUILD_DEFCONFIG:summitsom = ""

python __anonymous() {
    if 'summitsom' in d.getVar('OVERRIDES', True).split(':'):
        d.setVarFlag('do_copy_defconfig', 'noexec', '1')
}
