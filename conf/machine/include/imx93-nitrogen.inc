#@TYPE: Machine
#@NAME: Nitrogen i.MX93
#@SOC: i.MX93
#@DESCRIPTION: Machine configuration for Nitrogen i.MX93
#@MAINTAINER: Boris Krasnovskiy <Boris.Krasnovskiy@ezurio.com>

MACHINEOVERRIDES =. "mx93:summitsom:ahab:"

require conf/machine/include/imx-base.inc
require conf/machine/include/arm/armv8-2a/tune-cortexa55.inc

# for better sstate cache reuse
DEFAULTTUNE:mx93-generic-bsp = "armv8a-crc-crypto"

require conf/machine/include/u-boot-summit.inc
include ${@bb.utils.contains('BBFILE_COLLECTIONS', 'summit-radio', 'conf/machine/include/radio-stack.inc', '', d)}

MACHINE_FEATURES = "usbgadget usbhost alsa rtc optee screen touchscreen wifi bluetooth camera"
MACHINE_FEATURES:append:use-nxp-bsp = " dpdk ethos-u-firmware"
MACHINE_FEATURES:remove = "nxpaw693-sdio"

SOC_DEFAULT_IMAGE_FSTYPES = ""

IMAGE_BOOT_FILES = "uboot.env"
SWUPDATE_IMAGES = "flash.bin uboot.env"

UBOOT_PROVIDES_BOOT_CONTAINER = "1"
IMX_DEFAULT_BOOTLOADER:mx9-generic-bsp = "u-boot-imx-summit"
IMX_DEFAULT_UBOOTTOOLS:imx-generic-bsp = "u-boot-summit-tools"

DDR_FIRMWARE_VERSION = "202201"
DDR_FIRMWARE_NAME = " \
    lpddr4_dmem_1d_v${DDR_FIRMWARE_VERSION}.bin \
    lpddr4_dmem_2d_v${DDR_FIRMWARE_VERSION}.bin \
    lpddr4_imem_1d_v${DDR_FIRMWARE_VERSION}.bin \
    lpddr4_imem_2d_v${DDR_FIRMWARE_VERSION}.bin \
"

SPL_BINARY = "spl/u-boot-spl.bin"

ATF_PLATFORM = "imx93"

SERIAL_CONSOLES = "115200;ttyLP0"

LOADADDR = ""
UBOOT_SUFFIX = "img"
UBOOT_BINARY = "flash.bin"
UBOOT_MAKE_TARGET = "all"
IMX_BOOT_SEEK = "32"

MACHINE_EXTRA_RDEPENDS += "${MACHINE_FIRMWARE}"
EXTRA_IMAGEDEPENDS += "virtual/bootloader"

SOC_HAS_VIVANTE_KERNEL_DRIVER_SUPPORT:imxgpu = "0"

IMX_DEFAULT_BSP = "nxp"

MACHINE_EXTRA_RRECOMMENDS:remove = "imx-m33-demos"  

KERNEL_CLASSES = "kernel-fit-extra-artifacts"

KERNEL_IMAGETYPE = "Image"
KERNEL_IMAGETYPE:mx9-generic-bsp = "Image"
IMAGE_CLASSES += "dm-verity"

UBOOT_LOADADDRESS = "0x82000000"
UBOOT_ENTRYPOINT:mx93-generic-bsp = "0x82000000"
UBOOT_DTB_LOADADDRESS = "0x85000000"
UBOOT_DTBO_LOADADDRESS = "0x85080000"
UBOOT_RD_LOADADDRESS = "0x86000000"

FIT_KERNEL_COMP_ALG = "zstd"
FIT_KERNEL_COMP_ALG_EXTENSION = ".zst"

FIT_PAD_ALG = "pss"
FIT_HASH_ALG = "sha256"
FIT_SIGN_ALG = "rsa2048"

UBOOT_SIGN_KEYDIR = "${SIG_DATA_PATH}/keys"
SIG_DATA_PATH ?= "${SUMMIT_SOM_KEYS_DIR}/nitrogen/ahab"
UBOOT_SIGN_KEYNAME = "dev"
UBOOT_SIGN_ENABLE = "1"

UBOOT_ENV_OFFSET = "4128768"
EMMC_BOOT_OFFSET = "0"
EMMC_DEVICE = "0"
SD_DEVICE = "1"
