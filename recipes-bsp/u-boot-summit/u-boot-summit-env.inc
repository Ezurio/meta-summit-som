UBOOT_INITIAL_ENV = "u-boot-initial-env"

DEPENDS += "u-boot-mkenvimage-native"

FILES:${PN}-env += "${sysconfdir}/fw_env*.config"

do_compile:append() {
    ENV_SIZE=$(sed -rn 's,^CONFIG_ENV_SIZE=(.*),\1,p' "${B}/.config")
    ENV_OFFSET=$(sed -rn 's,^CONFIG_ENV_OFFSET=(.*),\1,p' "${B}/.config")

    MKENVIMAGE_REDUND_OPT=""
    grep -qF "CONFIG_ENV_REDUNDANT=y" "${B}/.config" && MKENVIMAGE_REDUND_OPT="-r"

    mkenvimage -s "${ENV_SIZE}" ${MKENVIMAGE_REDUND_OPT} -o "${B}/${UBOOT_INITIAL_ENV}.bin" "${B}/${UBOOT_INITIAL_ENV}"

    echo "/dev/mmcblk${EMMC_DEVICE}boot0 ${ENV_OFFSET} ${ENV_SIZE}" > "${B}/fw_env_emmc-a.config"
    echo "/dev/mmcblk${EMMC_DEVICE}boot1 ${ENV_OFFSET} ${ENV_SIZE}" > "${B}/fw_env_emmc-b.config"
    echo "/boot/uboot.env 0 ${ENV_SIZE}" > "${B}/fw_env_sd.config"
    for i in a b; do
        echo "/dev/mtd:u-boot-env-$i 0x00000 ${ENV_SIZE} 0 1 1"
    done > "${B}/fw_env_flash.config"
    touch "${B}/fw_env.config"
}

do_install:append() {
    install -D -m 644 "${B}/${UBOOT_INITIAL_ENV}.bin" "${D}/boot/uboot.env"
    install -D -m 644 -t "${D}/${sysconfdir}" "${B}"/fw_env*.config
}

do_deploy:append() {
    install -D -m 644 "${B}/${UBOOT_INITIAL_ENV}.bin" "${DEPLOYDIR}/u-boot-${UBOOT_CONFIG}.env"
    ln -sf "u-boot-${UBOOT_CONFIG}.env" "${DEPLOYDIR}/uboot.env"
}
