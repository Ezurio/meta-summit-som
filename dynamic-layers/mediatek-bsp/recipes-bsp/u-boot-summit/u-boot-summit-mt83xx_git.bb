SUMMARY = "Summit U-Boot for Tungsten MT83XX"

require u-boot-summit-mt83xx.inc
require recipes-bsp/u-boot-summit/u-boot-summit-env.inc

PROVIDES += "u-boot"

# eMMC boot: TF-A packs BL31, OP-TEE and this u-boot.bin into the FIP; swupdate flashes bl2.img and fip.bin.
# uboot-sign needs u-boot.dtb to inject the FIT public key; it is not a prerequisite of u-boot.bin.
UBOOT_MAKE_TARGET = "u-boot.bin u-boot.dtb"
UBOOT_INITIAL_ENV = "u-boot-initial-env"

DEPENDS += "u-boot-mkenvimage-native"

do_install() {
    install -m 0644 -D -t ${D}/boot ${B}/u-boot.bin
}

do_deploy() {
    install -m 0644 -D -t ${DEPLOYDIR} ${B}/u-boot.bin
}
